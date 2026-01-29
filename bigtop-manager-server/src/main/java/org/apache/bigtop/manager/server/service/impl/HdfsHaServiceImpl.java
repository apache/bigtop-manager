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

import com.fasterxml.jackson.core.type.TypeReference;
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
import org.apache.bigtop.manager.server.model.dto.command.ComponentCommandDTO;
import org.apache.bigtop.manager.server.model.req.EnableHdfsHaReq;
import org.apache.bigtop.manager.server.service.CommandService;
import org.apache.bigtop.manager.server.service.HdfsHaService;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
    public org.apache.bigtop.manager.server.model.vo.CommandVO buildEnableHdfsHaCommand(
            Long clusterId, Long serviceId, EnableHdfsHaReq req) {
        // 0. Validate prerequisites (components exist, ZK is available, etc.)
        validatePrerequisites(clusterId, serviceId, req);

        // 1. Write HA configurations to core-site.xml and hdfs-site.xml
        writeHaConfigurationWithRetry(clusterId, serviceId, req);

        // 2. Trigger a single service-level job (EnableHdfsHaJob) which orchestrates stages internally.
        // IMPORTANT: Do NOT use SERVICE CONFIGURE here, otherwise it will restart all hadoop components (including YARN).
        CommandDTO commandDTO = new CommandDTO();
        commandDTO.setClusterId(clusterId);
        commandDTO.setCommandLevel(CommandLevel.SERVICE);
        commandDTO.setCommand(Command.ENABLE_HDFS_HA);

        // Embed request payload into customCommand for the service job to consume.
        commandDTO.setCustomCommand("enableHdfsHa:" + JsonUtils.writeAsString(req));

        // Component host map used by EnableHdfsHaJob
        List<ComponentCommandDTO> componentCommands = new ArrayList<>();

        // HDFS components
        componentCommands.add(componentCommand("journalnode", req.getJournalNodeHosts()));
        componentCommands.add(componentCommand("namenode", List.of(req.getActiveNameNodeHost(), req.getStandbyNameNodeHost())));
        componentCommands.add(componentCommand("zkfc", req.getZkfcHosts()));

        // DataNode hosts: restart to pick up HA config
        List<String> datanodeHosts = getHostsByComponent(clusterId, "datanode");
        if (CollectionUtils.isNotEmpty(datanodeHosts)) {
            componentCommands.add(componentCommand("datanode", datanodeHosts));
        }

        // YARN components: configure only (EnableHdfsHaJob will not stop/start them)
        List<String> rmHosts = getHostsByComponent(clusterId, "resourcemanager");
        if (CollectionUtils.isNotEmpty(rmHosts)) {
            componentCommands.add(componentCommand("resourcemanager", rmHosts));
        }
        List<String> nmHosts = getHostsByComponent(clusterId, "nodemanager");
        if (CollectionUtils.isNotEmpty(nmHosts)) {
            componentCommands.add(componentCommand("nodemanager", nmHosts));
        }
        List<String> hsHosts = getHostsByComponent(clusterId, "history_server");
        if (CollectionUtils.isNotEmpty(hsHosts)) {
            componentCommands.add(componentCommand("history_server", hsHosts));
        }

        commandDTO.setComponentCommands(componentCommands);

        return commandService.command(commandDTO);
    }

    private void writeHaConfigurationWithRetry(Long clusterId, Long serviceId, EnableHdfsHaReq req) {
        int maxAttempts = 3;
        long sleepMs = 200;
        CannotAcquireLockException last = null;
        for (int i = 1; i <= maxAttempts; i++) {
            try {
                writeHaConfigurationInNewTx(clusterId, serviceId, req);
                return;
            } catch (CannotAcquireLockException e) {
                last = e;
                if (i == maxAttempts) {
                    break;
                }
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
                sleepMs *= 2;
            }
        }
        throw last;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void writeHaConfigurationInNewTx(Long clusterId, Long serviceId, EnableHdfsHaReq req) {
        writeHaConfiguration(clusterId, serviceId, req);
    }

    private ComponentCommandDTO componentCommand(String name, List<String> hosts) {
        ComponentCommandDTO cc = new ComponentCommandDTO();
        cc.setComponentName(name);
        cc.setHostnames(hosts == null ? List.of() : hosts);
        return cc;
    }

    private List<String> getHostsByComponent(Long clusterId, String componentName) {
        ComponentQuery q = ComponentQuery.builder()
                .clusterId(clusterId)
                .name(componentName)
                .build();
        List<ComponentPO> list = componentDao.findByQuery(q);
        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }
        return list.stream().map(ComponentPO::getHostname).filter(StringUtils::isNotBlank).distinct().toList();
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
        // NameNode is required before enabling HA.
        assertComponentExists(clusterId, "namenode", req.getActiveNameNodeHost());
        assertComponentExists(clusterId, "namenode", req.getStandbyNameNodeHost());

        // journalnode/zkfc may not exist before enable HA, because this action will ADD/install them.

        // Validate ZK quorum can be generated (must for automatic failover)
        String zk = buildZkAddress(clusterId, req);
        if (StringUtils.isBlank(zk)) {
            throw new ServerException(
                    "Failed to build ha.zookeeper.quorum, please check zookeeper hosts/service and components");
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

    private static final String CUSTOM_COMMAND_PREFIX = "enableHdfsHa:";

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

    private void upsertServiceConfigProperties(
            Long clusterId, Long serviceId, String configName, Map<String, String> updates) {
        ServiceConfigPO po = serviceConfigDao.findByServiceIdAndName(serviceId, configName);
        if (po == null) {
            po = new ServiceConfigPO();
            po.setClusterId(clusterId);
            po.setServiceId(serviceId);
            po.setName(configName);
            po.setPropertiesJson("[]"); // Initialize with empty JSON array
            serviceConfigDao.save(po);
            po = serviceConfigDao.findByServiceIdAndName(serviceId, configName);
        }

        List<PropertyDTO> properties = new ArrayList<>();
        if (StringUtils.isNotBlank(po.getPropertiesJson())) {
            properties.addAll(JsonUtils.readFromString(po.getPropertiesJson(), new TypeReference<>() {}));
        }

        Map<String, PropertyDTO> propsMap =
                properties.stream().collect(Collectors.toMap(PropertyDTO::getName, Function.identity(), (a, b) -> b));

        for (Map.Entry<String, String> e : updates.entrySet()) {
            String k = e.getKey();
            if (k != null && k.startsWith("__delete__.")) {
                propsMap.remove(k.substring("__delete__.".length()));
            } else {
                PropertyDTO prop = propsMap.getOrDefault(k, new PropertyDTO());
                prop.setName(k);
                prop.setValue(e.getValue());
                propsMap.put(k, prop);
            }
        }
        po.setPropertiesJson(JsonUtils.writeAsString(new ArrayList<>(propsMap.values())));
        serviceConfigDao.partialUpdateByIds(List.of(po));
    }
}
