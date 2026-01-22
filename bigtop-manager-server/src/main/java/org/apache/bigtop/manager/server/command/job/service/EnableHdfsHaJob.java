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
import org.apache.bigtop.manager.server.command.helper.ComponentStageHelper;
import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.command.stage.ComponentCustomStage;
import org.apache.bigtop.manager.server.command.stage.StageContext;
import org.apache.bigtop.manager.server.exception.ServerException;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.dto.command.ComponentCommandDTO;
import org.apache.bigtop.manager.server.model.req.EnableHdfsHaReq;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

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
public class EnableHdfsHaJob extends AbstractServiceJob {

    private static final String CUSTOM_COMMAND_PREFIX = "enableHdfsHa:";

    public EnableHdfsHaJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    protected void createStages() {
        CommandDTO commandDTO = jobContext.getCommandDTO();

        EnableHdfsHaReq req = parseReq(commandDTO);
        validateReq(req);

        Map<String, List<String>> componentHostsMap = getComponentHostsMap();

        // 0) Ensure required HA components are installed and running
        // Install/prepare JournalNodes first
        Map<String, List<String>> jn = pick(componentHostsMap, "journalnode");
        stages.addAll(ComponentStageHelper.createComponentStages(jn, Command.ADD, commandDTO));
        stages.addAll(ComponentStageHelper.createComponentStages(jn, Command.CONFIGURE, commandDTO));
        stages.addAll(ComponentStageHelper.createComponentStages(jn, Command.START, commandDTO));

        // Install/prepare ZKFC on selected hosts (do not start here, will start after formatting)
        Map<String, List<String>> zkfcHosts = pick(componentHostsMap, "zkfc");
        stages.addAll(ComponentStageHelper.createComponentStages(zkfcHosts, Command.ADD, commandDTO));
        stages.addAll(ComponentStageHelper.createComponentStages(zkfcHosts, Command.CONFIGURE, commandDTO));

        // 1) Start Active NameNode
        Map<String, List<String>> activeNN = Map.of("namenode", List.of(req.getActiveNameNodeHost()));
        stages.addAll(ComponentStageHelper.createComponentStages(activeNN, Command.START, commandDTO));

        // 2) Custom: initializeSharedEdits on Active NameNode
        stages.add(new ComponentCustomStage(
                createStageContext("namenode", List.of(req.getActiveNameNodeHost()), commandDTO),
                "initializeSharedEdits"));

        // 3) Custom: formatZk on Active NameNode host, component=zkfc
        stages.add(new ComponentCustomStage(
                createStageContext("zkfc", List.of(req.getActiveNameNodeHost()), commandDTO),
                "formatZk"));

        // 4) Start Standby NameNode
        Map<String, List<String>> standbyNN = Map.of("namenode", List.of(req.getStandbyNameNodeHost()));
        stages.addAll(ComponentStageHelper.createComponentStages(standbyNN, Command.START, commandDTO));

        // 5) Start ZKFC(s)
        if (CollectionUtils.isNotEmpty(req.getZkfcHosts())) {
            Map<String, List<String>> zkfc = Map.of("zkfc", req.getZkfcHosts());
            stages.addAll(ComponentStageHelper.createComponentStages(zkfc, Command.START, commandDTO));
        }

        // 6) Restart DataNode(s) - chosen option 2
        Map<String, List<String>> dn = pick(componentHostsMap, "datanode");
        stages.addAll(ComponentStageHelper.createComponentStages(dn, Command.RESTART, commandDTO));

        // 7) Configure YARN components only (no restart)
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
        return ccs.stream().collect(Collectors.toMap(
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
            throw new ServerException("EnableHdfsHaJob requires customCommand with prefix '" + CUSTOM_COMMAND_PREFIX + "'");
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
        if (CollectionUtils.isEmpty(req.getJournalNodeHosts()) || req.getJournalNodeHosts().size() < 3) {
            throw new ServerException("journalNodeHosts must be provided and have at least 3 nodes");
        }
        if (CollectionUtils.isEmpty(req.getZkfcHosts())) {
            throw new ServerException("zkfcHosts must not be empty");
        }
    }

    private StageContext createStageContext(String componentName, List<String> hostnames, CommandDTO commandDTO) {
        StageContext stageContext = StageContext.fromCommandDTO(commandDTO);
        stageContext.setHostnames(hostnames);
        stageContext.setServiceName("hadoop");
        stageContext.setComponentName(componentName);
        return stageContext;
    }
}
