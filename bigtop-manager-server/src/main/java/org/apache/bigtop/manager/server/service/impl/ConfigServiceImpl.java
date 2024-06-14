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

import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.entity.Cluster;
import org.apache.bigtop.manager.dao.entity.Service;
import org.apache.bigtop.manager.dao.entity.ServiceConfig;
import org.apache.bigtop.manager.dao.entity.TypeConfig;
import org.apache.bigtop.manager.dao.repository.ClusterRepository;
import org.apache.bigtop.manager.dao.repository.ServiceConfigRepository;
import org.apache.bigtop.manager.dao.repository.ServiceRepository;
import org.apache.bigtop.manager.dao.repository.TypeConfigRepository;
import org.apache.bigtop.manager.server.model.dto.PropertyDTO;
import org.apache.bigtop.manager.server.model.dto.TypeConfigDTO;
import org.apache.bigtop.manager.server.model.mapper.ServiceConfigMapper;
import org.apache.bigtop.manager.server.model.mapper.TypeConfigMapper;
import org.apache.bigtop.manager.server.model.vo.ServiceConfigVO;
import org.apache.bigtop.manager.server.service.ConfigService;

import org.springframework.data.domain.Sort;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.List;

@Slf4j
@org.springframework.stereotype.Service
public class ConfigServiceImpl implements ConfigService {

    @Resource
    private ClusterRepository clusterRepository;

    @Resource
    private ServiceRepository serviceRepository;

    @Resource
    private ServiceConfigRepository serviceConfigRepository;

    @Resource
    private TypeConfigRepository typeConfigRepository;

    @Override
    public List<ServiceConfigVO> list(Long clusterId) {
        Cluster cluster = clusterRepository.getReferenceById(clusterId);
        Sort sort = Sort.by(Sort.Direction.DESC, "version");
        List<ServiceConfig> list = serviceConfigRepository.findAllByCluster(cluster, sort);
        return ServiceConfigMapper.INSTANCE.fromEntity2VO(list);
    }

    @Override
    public List<ServiceConfigVO> latest(Long clusterId) {
        Cluster cluster = clusterRepository.getReferenceById(clusterId);
        List<ServiceConfig> list = serviceConfigRepository.findAllByClusterAndSelectedIsTrue(cluster);
        return ServiceConfigMapper.INSTANCE.fromEntity2VO(list);
    }

    @Override
    public void upsert(Long clusterId, Long serviceId, List<TypeConfigDTO> configs) {
        // Save configs
        Cluster cluster = clusterRepository.getReferenceById(clusterId);
        Service service = serviceRepository.getReferenceById(serviceId);
        ServiceConfig serviceCurrentConfig = findServiceCurrentConfig(cluster, service);
        if (serviceCurrentConfig == null) {
            // Add config for new service
            addServiceConfig(cluster, service, configs);
        } else {
            // Upsert config for existing service
            upsertServiceConfig(cluster, service, serviceCurrentConfig, configs);
        }
    }

    private ServiceConfig findServiceCurrentConfig(Cluster cluster, Service service) {
        return serviceConfigRepository.findByClusterAndServiceAndSelectedIsTrue(cluster, service);
    }

    private void upsertServiceConfig(
            Cluster cluster, Service service, ServiceConfig currentConfig, List<TypeConfigDTO> configs) {
        List<TypeConfigDTO> existConfigs = TypeConfigMapper.INSTANCE.fromEntity2DTO(currentConfig.getConfigs());
        if (shouldUpdateConfig(existConfigs, configs)) {
            // Unselect current config
            currentConfig.setSelected(false);
            serviceConfigRepository.save(currentConfig);

            // Create a new config
            String configDesc = "Update config for " + service.getServiceName();
            Integer version = currentConfig.getVersion() + 1;
            addServiceConfig(cluster, service, configs, configDesc, version);
        }
    }

    private Boolean shouldUpdateConfig(List<TypeConfigDTO> existConfigs, List<TypeConfigDTO> newConfigs) {
        if (existConfigs.size() != newConfigs.size()) {
            return true;
        }

        for (TypeConfigDTO newConfig : newConfigs) {
            for (TypeConfigDTO existConfig : existConfigs) {
                if (existConfig.getTypeName().equals(newConfig.getTypeName())) {
                    if (!existConfig.getProperties().equals(newConfig.getProperties())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void addServiceConfig(Cluster cluster, Service service, List<TypeConfigDTO> configs) {
        String configDesc = "Initial config for " + service.getServiceName();
        Integer version = 1;
        addServiceConfig(cluster, service, configs, configDesc, version);
    }

    private void addServiceConfig(
            Cluster cluster, Service service, List<TypeConfigDTO> configs, String configDesc, Integer version) {
        ServiceConfig serviceConfig = new ServiceConfig();
        serviceConfig.setCluster(cluster);
        serviceConfig.setService(service);
        serviceConfig.setConfigDesc(configDesc);
        serviceConfig.setVersion(version);
        serviceConfig.setSelected(true);
        serviceConfigRepository.save(serviceConfig);

        for (TypeConfigDTO typeConfigDTO : configs) {
            String typeName = typeConfigDTO.getTypeName();
            List<PropertyDTO> properties = typeConfigDTO.getProperties();
            TypeConfig typeConfig = new TypeConfig();
            typeConfig.setTypeName(typeName);
            typeConfig.setPropertiesJson(JsonUtils.writeAsString(properties));
            typeConfig.setServiceConfig(serviceConfig);
            typeConfigRepository.save(typeConfig);
        }
    }
}
