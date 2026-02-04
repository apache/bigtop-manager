/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.bigtop.manager.server.command.job.service;

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.server.command.helper.ComponentStageHelper;
import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.command.stage.ComponentCustomStage;
import org.apache.bigtop.manager.server.command.stage.StageContext;
import org.apache.bigtop.manager.server.command.stage.WaitPortStage;
import org.apache.bigtop.manager.server.command.stage.WaitUrlStage;
import org.apache.bigtop.manager.server.exception.ServerException;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.dto.command.ComponentCommandDTO;
import org.apache.bigtop.manager.server.model.req.EnableHdfsHaReq;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Enable HDFS HA job.
 *
 * 约束：
 * - HDFS 相关组件：执行 ADD/CONFIGURE/START/CUSTOM/RESTART（journalnode/namenode/zkfc/datanode）
 * - YARN 相关组件：仅执行 CONFIGURE（不 STOP/START），避免影响线上 YARN 任务
 */
@Slf4j
public class EnableHdfsHaJob extends AbstractServiceJob {

    private static final String HADOOP_SERVICE_NAME = "hadoop";
    private static final String NAMENODE_COMPONENT_NAME = "namenode";
    private static final String SECONDARY_NAMENODE_COMPONENT_NAME = "secondarynamenode";

    private static final String CUSTOM_COMMAND_PREFIX = "enableHdfsHa:";

    private static String getCodeSource(Class<?> clazz) {
        try {
            if (clazz == null
                    || clazz.getProtectionDomain() == null
                    || clazz.getProtectionDomain().getCodeSource() == null) {
                return "null";
            }
            return String.valueOf(clazz.getProtectionDomain().getCodeSource().getLocation());
        } catch (Exception e) {
            return "error:" + e.getMessage();
        }
    }

    public EnableHdfsHaJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    protected void createStages() {
        CommandDTO commandDTO = jobContext.getCommandDTO();
        EnableHdfsHaReq req = parseReq(commandDTO);
        validateReq(req);

        Map<String, List<String>> componentHostsMap = getComponentHostsMap();
        Map<String, List<String>> activeNN = Map.of(NAMENODE_COMPONENT_NAME, List.of(req.getActiveNameNodeHost()));
        Map<String, List<String>> standbyNN = Map.of(NAMENODE_COMPONENT_NAME, List.of(req.getStandbyNameNodeHost()));

        // 1. Prepare JournalNodes and ZKFCs (install/configure)
        Map<String, List<String>> jn = pick(componentHostsMap, "journalnode");
        stages.addAll(ComponentStageHelper.createComponentStages(jn, Command.ADD, commandDTO));
        stages.addAll(ComponentStageHelper.createComponentStages(jn, Command.CONFIGURE, commandDTO));
        stages.addAll(ComponentStageHelper.createComponentStages(jn, Command.START, commandDTO));
        stages.add(new WaitPortStage(
                createStageContext("journalnode", req.getJournalNodeHosts(), commandDTO),
                req.getJournalNodeHosts(),
                8485,
                10 * 60_000L,
                1000L));

        Map<String, List<String>> zkfcHosts = pick(componentHostsMap, "zkfc");
        stages.addAll(ComponentStageHelper.createComponentStages(zkfcHosts, Command.ADD, commandDTO));
        stages.addAll(ComponentStageHelper.createComponentStages(zkfcHosts, Command.CONFIGURE, commandDTO));

        // 2. Initialize Active NameNode (NN1)
        stages.addAll(ComponentStageHelper.createComponentStages(activeNN, Command.STOP, commandDTO));
        stages.add(new ComponentCustomStage(
                createStageContext("namenode", List.of(req.getActiveNameNodeHost()), commandDTO),
                "initializeSharedEdits"));
        stages.add(new ComponentCustomStage(
                createStageContext("zkfc", List.of(req.getActiveNameNodeHost()), commandDTO), "formatZk"));

        // 3. Start Active NameNode and its ZKFC, then wait for it to become active
        stages.addAll(ComponentStageHelper.createComponentStages(activeNN, Command.START, commandDTO));
        stages.addAll(ComponentStageHelper.createComponentStages(
                Map.of("zkfc", List.of(req.getActiveNameNodeHost())), Command.START, commandDTO));
        stages.add(new WaitUrlStage(
                createStageContext("namenode", List.of(req.getActiveNameNodeHost()), commandDTO),
                List.of(req.getActiveNameNodeHost()),
                "http://{host}:9870/jmx?qry=Hadoop:service=NameNode,name=NameNodeStatus",
                "active",
                10 * 60_000L,
                3000L));

        // 4. Initialize and Start Standby NameNode (NN2) and its ZKFC
        stages.addAll(ComponentStageHelper.createComponentStages(standbyNN, Command.ADD, commandDTO));
        stages.addAll(ComponentStageHelper.createComponentStages(standbyNN, Command.CONFIGURE, commandDTO));
        stages.add(new ComponentCustomStage(
                createStageContext("namenode", List.of(req.getStandbyNameNodeHost()), commandDTO), "bootstrapStandby"));
        stages.addAll(ComponentStageHelper.createComponentStages(standbyNN, Command.START, commandDTO));
        stages.addAll(ComponentStageHelper.createComponentStages(
                Map.of("zkfc", List.of(req.getStandbyNameNodeHost())), Command.START, commandDTO));

        // 5. Finalize
        // Convert secondarynamenode on standby host to standby namenode
        removeSecondaryNameNode(req.getStandbyNameNodeHost());

        // Ensure standby host has namenode component record
        ensureStandbyNameNodeComponent(req.getStandbyNameNodeHost());

        Map<String, List<String>> dn = pick(componentHostsMap, "datanode");
        stages.addAll(ComponentStageHelper.createComponentStages(dn, Command.RESTART, commandDTO));

        Map<String, List<String>> yarn = pick(componentHostsMap, "resourcemanager", "nodemanager", "history_server");
        stages.addAll(ComponentStageHelper.createComponentStages(yarn, Command.CONFIGURE, commandDTO));

        if (stages.isEmpty()) {
            throw new IllegalStateException("EnableHdfsHaJob has no stages to execute");
        }
    }

    @Override
    public String getName() {
        return "Enable HDFS HA";
    }

    @Override
    protected Map<String, List<String>> getComponentHostsMap() {
        List<ComponentCommandDTO> ccs = jobContext.getCommandDTO().getComponentCommands();
        if (ccs == null) {
            return new HashMap<>();
        }
        return ccs.stream()
                .collect(Collectors.toMap(
                        cc -> cc.getComponentName().toLowerCase(),
                        cc -> cc.getHostnames() == null ? List.of() : cc.getHostnames(),
                        (a, b) -> a));
    }

    private static Map<String, List<String>> pick(Map<String, List<String>> all, String... componentNames) {
        Map<String, List<String>> m = new HashMap<>();
        for (String name : componentNames) {
            List<String> hosts = all.get(name);
            if (hosts != null && !hosts.isEmpty()) {
                m.put(name, hosts);
            }
        }
        return m;
    }

    private EnableHdfsHaReq parseReq(CommandDTO commandDTO) {
        String cc = commandDTO.getCustomCommand();
        if (StringUtils.isBlank(cc) || !cc.startsWith(CUSTOM_COMMAND_PREFIX)) {
            throw new ServerException(
                    "EnableHdfsHaJob requires customCommand with prefix '" + CUSTOM_COMMAND_PREFIX + "'");
        }
        String json = cc.substring(CUSTOM_COMMAND_PREFIX.length());
        if (StringUtils.isBlank(json)) {
            throw new ServerException("EnableHdfsHaReq payload is empty in customCommand");
        }
        try {
            return JsonUtils.readFromString(json, EnableHdfsHaReq.class);
        } catch (Exception e) {
            throw new ServerException("Failed to parse EnableHdfsHaReq from customCommand: " + e.getMessage());
        }
    }

    private void validateReq(EnableHdfsHaReq req) {
        if (req == null) {
            throw new ServerException("request is required");
        }
        if (StringUtils.isBlank(req.getActiveNameNodeHost()) || StringUtils.isBlank(req.getStandbyNameNodeHost())) {
            throw new ServerException("activeNameNodeHost/standbyNameNodeHost must not be blank");
        }
        if (req.getActiveNameNodeHost().equalsIgnoreCase(req.getStandbyNameNodeHost())) {
            throw new ServerException("activeNameNodeHost and standbyNameNodeHost must be different");
        }
        if (StringUtils.isBlank(req.getNameservice())) {
            throw new ServerException("nameservice must not be blank");
        }
        if (CollectionUtils.isEmpty(req.getJournalNodeHosts())
                || req.getJournalNodeHosts().size() < 3) {
            throw new ServerException("journalNodeHosts must be provided and have at least 3 nodes");
        }
        if (CollectionUtils.isEmpty(req.getZkfcHosts())) {
            throw new ServerException("zkfcHosts must not be empty");
        }
    }

    private StageContext createStageContext(String componentName, List<String> hostnames, CommandDTO commandDTO) {
        StageContext stageContext = StageContext.fromCommandDTO(commandDTO);
        stageContext.setHostnames(hostnames);
        stageContext.setServiceName(HADOOP_SERVICE_NAME);
        stageContext.setComponentName(componentName);
        return stageContext;
    }

    private void removeSecondaryNameNode(String hostname) {
        log.info("Attempting to remove Secondary NameNode on host: {}", hostname);
        ComponentPO secondaryNameNode = componentDao.findByNameAndHostname(SECONDARY_NAMENODE_COMPONENT_NAME, hostname);
        if (secondaryNameNode != null) {
            log.info(
                    "Found Secondary NameNode component with ID {} on host {}. Deleting it.",
                    secondaryNameNode.getId(),
                    hostname);
            componentDao.deleteById(secondaryNameNode.getId());
        } else {
            log.info("No Secondary NameNode component found on host {}. Nothing to remove.", hostname);
        }
    }

    private void ensureStandbyNameNodeComponent(String hostname) {
        log.info("Ensuring NameNode component exists on standby host: {}", hostname);
        ComponentPO nameNode = componentDao.findByNameAndHostname(NAMENODE_COMPONENT_NAME, hostname);
        if (nameNode == null) {
            log.info("NameNode component not found on standby host {}. Creating a new entry.", hostname);
            Long clusterId = jobContext.getCommandDTO().getClusterId();
            HostPO hostPO = hostDao.findByHostname(hostname);
            if (hostPO == null) {
                throw new ServerException("Host not found in database: " + hostname);
            }
            ServicePO servicePO = serviceDao.findByClusterIdAndName(clusterId, HADOOP_SERVICE_NAME);
            if (servicePO == null) {
                throw new ServerException("Service 'hadoop' not found for clusterId: " + clusterId);
            }

            ComponentPO standbyNameNodePO = new ComponentPO();
            standbyNameNodePO.setName(NAMENODE_COMPONENT_NAME);
            standbyNameNodePO.setDisplayName("NameNode");
            standbyNameNodePO.setClusterId(clusterId);
            standbyNameNodePO.setHostId(hostPO.getId());
            standbyNameNodePO.setServiceId(servicePO.getId());
            standbyNameNodePO.setStatus(org.apache.bigtop.manager.server.enums.HealthyStatusEnum.UNKNOWN.getCode());
            componentDao.save(standbyNameNodePO);
            log.info("Successfully created NameNode component entry for standby host {}.", hostname);
        } else {
            log.info("NameNode component already exists on standby host {}. Nothing to do.", hostname);
        }
    }
}
