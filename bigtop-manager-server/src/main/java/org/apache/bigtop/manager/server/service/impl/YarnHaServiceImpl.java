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
import org.apache.bigtop.manager.server.model.req.EnableYarnRmHaReq;
import org.apache.bigtop.manager.server.service.YarnHaService;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class YarnHaServiceImpl implements YarnHaService {

    private static final String HADOOP_SERVICE_NAME = "hadoop";
    private static final String ZOOKEEPER_SERVICE_NAME = "zookeeper";

    @Resource
    private ServiceDao serviceDao;

    @Resource
    private ServiceConfigDao serviceConfigDao;

    @Resource
    private ComponentDao componentDao;

    @Override
    @Transactional
    public CommandDTO buildEnableYarnRmHaCommand(Long clusterId, Long serviceId, EnableYarnRmHaReq req) {
        ServicePO servicePO = serviceDao.findById(serviceId);
        if (servicePO == null) {
            throw new ServerException("Service not found: " + serviceId);
        }
        if (!HADOOP_SERVICE_NAME.equalsIgnoreCase(servicePO.getName())) {
            throw new ServerException(
                    "enable-yarn-rm-ha only supports service 'hadoop', but got: " + servicePO.getName());
        }

        Map<String, String> yarnSiteUpdates = buildYarnSiteUpdates(clusterId, serviceId, req);
        upsertServiceConfigProperties(clusterId, serviceId, "yarn-site", yarnSiteUpdates);

        CommandDTO commandDTO = new CommandDTO();
        commandDTO.setClusterId(clusterId);
        commandDTO.setCommandLevel(CommandLevel.SERVICE);
        commandDTO.setCommand(Command.ENABLE_YARN_RM_HA);

        ComponentCommandDTO rmCmd = new ComponentCommandDTO();
        rmCmd.setComponentName("resourcemanager");
        rmCmd.setHostnames(List.of(req.getActiveResourceManagerHost(), req.getStandbyResourceManagerHost()));
        commandDTO.setComponentCommands(List.of(rmCmd));

        return commandDTO;
    }

    private Map<String, String> buildYarnSiteUpdates(Long clusterId, Long serviceId, EnableYarnRmHaReq req) {
        if (StringUtils.isBlank(req.getActiveResourceManagerHost())
                || StringUtils.isBlank(req.getStandbyResourceManagerHost())) {
            throw new ServerException("active/standby resourcemanager host must not be blank");
        }
        if (CollectionUtils.isEmpty(req.getRmIds()) || req.getRmIds().size() < 2) {
            throw new ServerException("rmIds must be provided and contain at least 2 ids");
        }

        String rm1Id = req.getRmIds().get(0);
        String rm2Id = req.getRmIds().get(1);

        Map<String, String> m = new HashMap<>();
        m.put("yarn.resourcemanager.ha.enabled", "true");
        m.put("yarn.resourcemanager.ha.rm-ids", String.join(",", req.getRmIds()));
        m.put("yarn.resourcemanager.cluster-id", req.getYarnClusterId());

        // hostname.rmX
        m.put("yarn.resourcemanager.hostname." + rm1Id, req.getActiveResourceManagerHost());
        m.put("yarn.resourcemanager.hostname." + rm2Id, req.getStandbyResourceManagerHost());

        int webappPort = resolvePortFromExistingKey(serviceId, "yarn-site", "yarn.resourcemanager.webapp.address", 8088);
        m.put("yarn.resourcemanager.webapp.address." + rm1Id, req.getActiveResourceManagerHost() + ":" + webappPort);
        m.put("yarn.resourcemanager.webapp.address." + rm2Id, req.getStandbyResourceManagerHost() + ":" + webappPort);

        int rmAddressPort = resolvePortFromExistingKey(serviceId, "yarn-site", "yarn.resourcemanager.address", 8032);
        int rmAdminPort = resolvePortFromExistingKey(serviceId, "yarn-site", "yarn.resourcemanager.admin.address", 8033);
        int rmRtPort = resolvePortFromExistingKey(serviceId, "yarn-site", "yarn.resourcemanager.resource-tracker.address", 8031);
        int rmSchedulerPort = resolvePortFromExistingKey(serviceId, "yarn-site", "yarn.resourcemanager.scheduler.address", 8030);

        m.put("yarn.resourcemanager.address." + rm1Id, req.getActiveResourceManagerHost() + ":" + rmAddressPort);
        m.put("yarn.resourcemanager.address." + rm2Id, req.getStandbyResourceManagerHost() + ":" + rmAddressPort);

        m.put("yarn.resourcemanager.admin.address." + rm1Id, req.getActiveResourceManagerHost() + ":" + rmAdminPort);
        m.put("yarn.resourcemanager.admin.address." + rm2Id, req.getStandbyResourceManagerHost() + ":" + rmAdminPort);

        m.put("yarn.resourcemanager.resource-tracker.address." + rm1Id, req.getActiveResourceManagerHost() + ":" + rmRtPort);
        m.put("yarn.resourcemanager.resource-tracker.address." + rm2Id, req.getStandbyResourceManagerHost() + ":" + rmRtPort);

        m.put("yarn.resourcemanager.scheduler.address." + rm1Id, req.getActiveResourceManagerHost() + ":" + rmSchedulerPort);
        m.put("yarn.resourcemanager.scheduler.address." + rm2Id, req.getStandbyResourceManagerHost() + ":" + rmSchedulerPort);

        String zkAddress = buildZkAddress(clusterId, req);
        if (StringUtils.isNotBlank(zkAddress)) {
            m.put("yarn.resourcemanager.zk-address", zkAddress);
        }

        m.put("__delete__.yarn.resourcemanager.hostname", "");
        m.put("__delete__.yarn.resourcemanager.address", "");
        m.put("__delete__.yarn.resourcemanager.admin.address", "");
        m.put("__delete__.yarn.resourcemanager.resource-tracker.address", "");
        m.put("__delete__.yarn.resourcemanager.scheduler.address", "");
        m.put("__delete__.yarn.resourcemanager.webapp.address", "");
        m.put("__delete__.yarn.resourcemanager.webapp.https.address", "");
        return m;
    }

    private int resolvePortFromExistingKey(Long serviceId, String configName, String key, int defaultPort) {
        try {
            Map<String, String> existing = getExistingConfigAsMap(serviceId, configName);
            String value = existing.get(key);
            if (StringUtils.isBlank(value) || !value.contains(":")) {
                return defaultPort;
            }
            String portStr = value.split(":")[1].trim();
                    return Integer.parseInt(portStr);
        } catch (Exception ignored) {
        }
        return defaultPort;
    }

    private Map<String, String> getExistingConfigAsMap(Long serviceId, String configName) {
        Map<String, String> map = new HashMap<>();
        try {
            ServiceConfigPO cfg = serviceConfigDao.findByServiceIdAndName(serviceId, configName);
            if (cfg == null || StringUtils.isBlank(cfg.getPropertiesJson())) {
                return map;
            }
            List<PropertyDTO> properties = JsonUtils.readFromString(cfg.getPropertiesJson(), new TypeReference<>() {});
            for (PropertyDTO p : properties) {
                if (p.getName() != null && p.getValue() != null) {
                    map.put(p.getName(), p.getValue().toString());
                }
            }
        } catch (Exception ignored) {
        }
        return map;
    }

    private String buildZkAddress(Long clusterId, EnableYarnRmHaReq req) {
        // Preferred: zookeeperHosts from request
        if (CollectionUtils.isNotEmpty(req.getZookeeperHosts())) {
            return req.getZookeeperHosts().stream()
                    .filter(StringUtils::isNotBlank)
                    .map(h -> h.trim() + ":2181") // Assume default port 2181
                    .distinct()
                    .collect(Collectors.joining(","));
        }

        // Fallback: zookeeperServiceId
        Long zookeeperServiceId = req.getZookeeperServiceId();
        if (zookeeperServiceId == null) {
            return "";
        }

        ServicePO zkService = serviceDao.findById(zookeeperServiceId);
        if (zkService == null) {
            throw new ServerException("zookeeper service not found: " + zookeeperServiceId);
        }
        if (!ZOOKEEPER_SERVICE_NAME.equalsIgnoreCase(zkService.getName())) {
            throw new ServerException(
                    "zookeeperServiceId must point to service 'zookeeper', but got: " + zkService.getName());
        }

        ServiceConfigPO zooCfg = serviceConfigDao.findByServiceIdAndName(zookeeperServiceId, "zoo.cfg");
        if (zooCfg == null || StringUtils.isBlank(zooCfg.getPropertiesJson())) {
            return "";
        }

        List<PropertyDTO> props = JsonUtils.readFromString(zooCfg.getPropertiesJson(), new TypeReference<>() {});
        String clientPort = "2181";
        for (PropertyDTO prop : props) {
            if ("clientPort".equals(prop.getName())) {
                clientPort = prop.getValue();
                break;
            }
        }

        ComponentQuery query = ComponentQuery.builder()
                .serviceId(zookeeperServiceId)
                .name("zookeeper_server")
                .build();
        List<String> zkHosts = componentDao.findByQuery(query).stream()
                .map(x -> x.getHostname())
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();

        if (CollectionUtils.isEmpty(zkHosts)) {
            return "";
        }

        String finalClientPort = clientPort;
        return zkHosts.stream().map(h -> h.trim() + ":" + finalClientPort).collect(Collectors.joining(","));
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
