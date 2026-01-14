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
package org.apache.bigtop.manager.server.service.impl;

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.po.ServiceConfigPO;
import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.dao.query.ComponentQuery;
import org.apache.bigtop.manager.dao.repository.ComponentDao;
import org.apache.bigtop.manager.dao.repository.ServiceConfigDao;
import org.apache.bigtop.manager.dao.repository.ServiceDao;
import org.apache.bigtop.manager.server.enums.CommandLevel;
import org.apache.bigtop.manager.server.exception.ServerException;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.dto.PropertyDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceConfigDTO;
import org.apache.bigtop.manager.server.model.dto.command.ComponentCommandDTO;
import org.apache.bigtop.manager.server.model.dto.command.ServiceCommandDTO;
import org.apache.bigtop.manager.server.model.req.EnableHdfsHaReq;
import org.apache.bigtop.manager.server.service.CommandService;
import org.apache.bigtop.manager.server.service.HdfsHaService;
import org.apache.bigtop.manager.server.utils.StackConfigUtils;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HdfsHaServiceImpl implements HdfsHaService {

    private static final String ZOOKEEPER_SERVICE_NAME = "zookeeper";

    @Resource
    private ServiceDao serviceDao;

    @Resource
    private ServiceConfigDao serviceConfigDao;

    @Resource
    private ComponentDao componentDao;

    @Resource
    private CommandService commandService;

    @Override
    @Transactional
    public org.apache.bigtop.manager.server.model.vo.CommandVO buildEnableHdfsHaCommand(
            Long clusterId, Long serviceId, EnableHdfsHaReq req) {
        // 0. Validate prerequisites (components exist, ZK is available, etc.)
        validatePrerequisites(clusterId, serviceId, req);

        // 1. Write HA configurations to core-site.xml and hdfs-site.xml
        writeHaConfiguration(clusterId, serviceId, req);

        // 2. Orchestrate a sequence of commands to enable HA
        // The commandService.command() is asynchronous. The JobScheduler will execute them sequentially.

        // Stage 1: Start JournalNodes first
        submitStartJournalNodes(clusterId, req.getJournalNodeHosts());

        // Stage 2: Start Active NameNode
        submitStartComponent(clusterId, "namenode", req.getActiveNameNodeHost());

        // Stage 3: Initialize Active NameNode
        submitCustomCommand(clusterId, "namenode", req.getActiveNameNodeHost(), "initializeSharedEdits");

        // Stage 4: Format ZKFC on Active NameNode host
        submitCustomCommand(clusterId, "zkfc", req.getActiveNameNodeHost(), "formatZk");

        // Stage 5: Start Standby NameNode (NameNodeScript.start() may bootstrap standby automatically)
        submitStartComponent(clusterId, "namenode", req.getStandbyNameNodeHost());

        // Stage 6: Start all ZKFCs
        submitStartComponent(clusterId, "zkfc", req.getZkfcHosts());

        // Final Stage: Trigger a service-level CONFIGURE to apply settings to all other components (e.g., DataNodes)
        return submitFinalConfigure(clusterId, serviceId);
    }

    private void validatePrerequisites(Long clusterId, Long serviceId, EnableHdfsHaReq req) {
        if (clusterId == null) {
            throw new ServerException("clusterId is required");
        }
        if (serviceId == null) {
            throw new ServerException("serviceId is required");
        }
        if (req == null) {
            throw new ServerException("request body is required");
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

        // Verify required components exist on specified hosts
        assertComponentExists(clusterId, "namenode", req.getActiveNameNodeHost());
        assertComponentExists(clusterId, "namenode", req.getStandbyNameNodeHost());

        for (String jnHost : req.getJournalNodeHosts()) {
            assertComponentExists(clusterId, "journalnode", jnHost);
        }

        // zkfc should exist on active host at least (as we run formatZk there)
        assertComponentExists(clusterId, "zkfc", req.getActiveNameNodeHost());

        // Validate ZK quorum can be generated (must for automatic failover)
        String zk = buildZkAddress(clusterId, req);
        if (StringUtils.isBlank(zk)) {
            throw new ServerException("Failed to build ha.zookeeper.quorum, please check zookeeper hosts/service and components");
        }
    }

    private void assertComponentExists(Long clusterId, String componentName, String hostname) {
        ComponentQuery q = ComponentQuery.builder()
                .clusterId(clusterId)
                .name(componentName)
                .hostname(hostname)
                .build();
        List<ComponentPO> list = componentDao.findByQuery(q);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServerException(
                    "Component not found in DB: component=" + componentName + ", host=" + hostname + ", clusterId=" + clusterId);
        }
    }

    private void writeHaConfiguration(Long clusterId, Long serviceId, EnableHdfsHaReq req) {
        // Update core-site.xml
        Map<String, String> coreSiteUpdates = buildCoreSiteUpdates(clusterId, req);
        upsertServiceConfigProperties(clusterId, serviceId, "core-site", coreSiteUpdates);

        // Update hdfs-site.xml
        Map<String, String> hdfsSiteUpdates = buildHdfsSiteUpdates(req);
        upsertServiceConfigProperties(clusterId, serviceId, "hdfs-site", hdfsSiteUpdates);
    }

    private Map<String, String> buildCoreSiteUpdates(Long clusterId, EnableHdfsHaReq req) {
        Map<String, String> m = new HashMap<>();
        m.put("fs.defaultFS", "hdfs://" + req.getNameservice());

        String zkAddress = buildZkAddress(clusterId, req);
        if (StringUtils.isNotBlank(zkAddress)) {
            m.put("ha.zookeeper.quorum", zkAddress);
        }
        return m;
    }

    private Map<String, String> buildHdfsSiteUpdates(EnableHdfsHaReq req) {
        String nameservice = req.getNameservice();
        String nn1Host = req.getActiveNameNodeHost();
        String nn2Host = req.getStandbyNameNodeHost();

        if (CollectionUtils.isEmpty(req.getJournalNodeHosts()) || req.getJournalNodeHosts().size() < 3) {
            throw new ServerException("JournalNode hosts must be provided and have at least 3 nodes.");
        }
        String journalQuorum = req.getJournalNodeHosts().stream().map(h -> h + ":8485").collect(Collectors.joining(";"));

        Map<String, String> m = new HashMap<>();
        m.put("dfs.nameservices", nameservice);
        m.put("dfs.ha.namenodes." + nameservice, "nn1,nn2");
        m.put("dfs.namenode.rpc-address." + nameservice + ".nn1", nn1Host + ":8020");
        m.put("dfs.namenode.rpc-address." + nameservice + ".nn2", nn2Host + ":8020");
        m.put("dfs.namenode.http-address." + nameservice + ".nn1", nn1Host + ":9870");
        m.put("dfs.namenode.http-address." + nameservice + ".nn2", nn2Host + ":9870");
        m.put("dfs.namenode.shared.edits.dir", "qjournal://" + journalQuorum + "/" + nameservice);
        m.put(
                "dfs.client.failover.proxy.provider." + nameservice,
                "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
        m.put("dfs.ha.automatic-failover.enabled", "true");
        m.put("dfs.ha.fencing.methods", "shell(/bin/true)");

        // Clean up single-node keys
        m.put("__delete__.dfs.namenode.rpc-address", "");
        m.put("__delete__.dfs.namenode.http-address", "");
        m.put("__delete__.dfs.namenode.https-address", "");

        return m;
    }

    private void submitStartJournalNodes(Long clusterId, List<String> hosts) {
        submitStartComponent(clusterId, "journalnode", hosts);
    }

    private void submitStartComponent(Long clusterId, String componentName, String host) {
        submitStartComponent(clusterId, componentName, List.of(host));
    }

    private void submitStartComponent(Long clusterId, String componentName, List<String> hosts) {
        CommandDTO commandDTO = new CommandDTO();
        commandDTO.setClusterId(clusterId);
        commandDTO.setCommand(Command.START);
        commandDTO.setCommandLevel(CommandLevel.COMPONENT);

        ComponentCommandDTO componentCommand = new ComponentCommandDTO();
        componentCommand.setComponentName(componentName);
        componentCommand.setHostnames(hosts);
        commandDTO.setComponentCommands(List.of(componentCommand));

        commandService.command(commandDTO);
    }

    private void submitCustomCommand(Long clusterId, String componentName, String host, String customCommand) {
        CommandDTO commandDTO = new CommandDTO();
        commandDTO.setClusterId(clusterId);
        commandDTO.setCommand(Command.CUSTOM);
        commandDTO.setCustomCommand(customCommand);
        commandDTO.setCommandLevel(CommandLevel.COMPONENT);

        ComponentCommandDTO componentCommand = new ComponentCommandDTO();
        componentCommand.setComponentName(componentName);
        componentCommand.setHostnames(List.of(host));
        commandDTO.setComponentCommands(List.of(componentCommand));

        commandService.command(commandDTO);
    }

    private org.apache.bigtop.manager.server.model.vo.CommandVO submitFinalConfigure(Long clusterId, Long serviceId) {
        ServicePO servicePO = serviceDao.findById(serviceId);

        CommandDTO commandDTO = new CommandDTO();
        commandDTO.setClusterId(clusterId);
        commandDTO.setCommand(Command.CONFIGURE);
        commandDTO.setCommandLevel(CommandLevel.SERVICE);

        ServiceCommandDTO serviceCommand = new ServiceCommandDTO();
        serviceCommand.setServiceName(servicePO.getName());
        List<ServiceConfigDTO> mergedConfigs = mergeStackAndDbConfigs(serviceId, servicePO.getName());
        serviceCommand.setConfigs(mergedConfigs);
        commandDTO.setServiceCommands(List.of(serviceCommand));

        return commandService.command(commandDTO);
    }

    private String buildZkAddress(Long clusterId, EnableHdfsHaReq req) {
        // Preferred: zookeeperHosts from request
        if (CollectionUtils.isNotEmpty(req.getZookeeperHosts())) {
            return req.getZookeeperHosts().stream()
                    .filter(StringUtils::isNotBlank)
                    .map(h -> h.trim() + ":2181")
                    .distinct()
                    .collect(Collectors.joining(","));
        }

        Long zookeeperServiceId = req.getZookeeperServiceId();
        if (zookeeperServiceId == null) {
            return "";
        }

        ServicePO zkService = serviceDao.findById(zookeeperServiceId);
        if (zkService == null || !ZOOKEEPER_SERVICE_NAME.equalsIgnoreCase(zkService.getName())) {
            throw new ServerException("zookeeperServiceId must point to a valid Zookeeper service.");
        }

        ServiceConfigPO zooCfg = serviceConfigDao.findByServiceIdAndName(zookeeperServiceId, "zoo.cfg");
        if (zooCfg == null || StringUtils.isBlank(zooCfg.getPropertiesJson())) {
            throw new ServerException("zoo.cfg not found or empty for zookeeper service: " + zookeeperServiceId);
        }

        Map<String, Object> props = JsonUtils.readFromString(zooCfg.getPropertiesJson());
        String clientPort = props.getOrDefault("clientPort", "2181").toString().trim();

        ComponentQuery query = ComponentQuery.builder().serviceId(zookeeperServiceId).name("zookeeper_server").build();
        List<String> zkHosts = componentDao.findByQuery(query).stream()
                .map(ComponentPO::getHostname)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();

        if (CollectionUtils.isEmpty(zkHosts)) {
            return "";
        }

        return zkHosts.stream().map(h -> h.trim() + ":" + clientPort).collect(Collectors.joining(","));
    }

    private void upsertServiceConfigProperties(Long clusterId, Long serviceId, String configName, Map<String, String> updates) {
        ServiceConfigPO po = serviceConfigDao.findByServiceIdAndName(serviceId, configName);
        if (po == null) {
            po = new ServiceConfigPO();
            po.setClusterId(clusterId);
            po.setServiceId(serviceId);
            po.setName(configName);
            po.setPropertiesJson("{}");
            serviceConfigDao.save(po);
            po = serviceConfigDao.findByServiceIdAndName(serviceId, configName);
        }

        Map<String, Object> props = new HashMap<>();
        if (StringUtils.isNotBlank(po.getPropertiesJson())) {
            props.putAll(JsonUtils.readFromString(po.getPropertiesJson()));
        }

        for (Map.Entry<String, String> e : updates.entrySet()) {
            String k = e.getKey();
            if (k != null && k.startsWith("__delete__.")) {
                props.remove(k.substring("__delete__.".length()));
            } else {
                props.put(k, e.getValue());
            }
        }
        po.setPropertiesJson(JsonUtils.writeAsString(props));
        serviceConfigDao.partialUpdateByIds(List.of(po));
    }

    private List<ServiceConfigDTO> mergeStackAndDbConfigs(Long serviceId, String serviceName) {
        List<ServiceConfigPO> dbConfigs = serviceConfigDao.findByServiceId(serviceId);
        List<ServiceConfigDTO> stackConfigs = StackUtils.SERVICE_CONFIG_MAP.get(serviceName);
        if (stackConfigs == null) {
            stackConfigs = List.of();
        }

        List<ServiceConfigDTO> dbConfigsDTO = new ArrayList<>();
        for (ServiceConfigPO po : dbConfigs) {
            ServiceConfigDTO dto = new ServiceConfigDTO();
            dto.setId(po.getId());
            dto.setName(po.getName());

            Map<String, Object> props = StringUtils.isBlank(po.getPropertiesJson())
                    ? Map.of()
                    : JsonUtils.readFromString(po.getPropertiesJson());

            List<PropertyDTO> propertyDTOS = new ArrayList<>();
            for (Map.Entry<String, Object> e : props.entrySet()) {
                PropertyDTO p = new PropertyDTO();
                p.setName(e.getKey());
                p.setValue(e.getValue() == null ? null : e.getValue().toString());
                propertyDTOS.add(p);
            }
            dto.setProperties(propertyDTOS);
            dbConfigsDTO.add(dto);
        }

        return StackConfigUtils.mergeServiceConfigs(stackConfigs, dbConfigsDTO);
    }
}
