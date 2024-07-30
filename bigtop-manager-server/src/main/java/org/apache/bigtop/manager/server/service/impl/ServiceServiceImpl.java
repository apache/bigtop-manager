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

import org.apache.bigtop.manager.common.constants.ComponentCategories;
import org.apache.bigtop.manager.common.enums.MaintainState;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.po.*;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.repository.HostComponentRepository;
import org.apache.bigtop.manager.dao.repository.ServiceConfigRepository;
import org.apache.bigtop.manager.dao.repository.ServiceRepository;
import org.apache.bigtop.manager.server.model.converter.ServiceConverter;
import org.apache.bigtop.manager.server.model.converter.TypeConfigConverter;
import org.apache.bigtop.manager.server.model.dto.PropertyDTO;
import org.apache.bigtop.manager.server.model.dto.QuickLinkDTO;
import org.apache.bigtop.manager.server.model.dto.TypeConfigDTO;
import org.apache.bigtop.manager.server.model.vo.QuickLinkVO;
import org.apache.bigtop.manager.server.model.vo.ServiceVO;
import org.apache.bigtop.manager.server.service.ServiceService;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@org.springframework.stereotype.Service
public class ServiceServiceImpl implements ServiceService {

    @Resource
    private ServiceRepository serviceRepository;

    @Resource
    private HostComponentRepository hostComponentRepository;

    @Resource
    private ServiceConfigRepository serviceConfigRepository;

    @Override
    public List<ServiceVO> list(Long clusterId) {
        List<ServiceVO> res = new ArrayList<>();
        List<HostComponent> hostComponentList = hostComponentRepository.findAllByComponentClusterId(clusterId);
        Map<Long, List<HostComponent>> serviceIdToHostComponent = hostComponentList.stream()
                .collect(Collectors.groupingBy(hostComponent ->
                        hostComponent.getComponentPO().getService().getId()));

        for (Map.Entry<Long, List<HostComponent>> entry : serviceIdToHostComponent.entrySet()) {
            List<HostComponent> hostComponents = entry.getValue();
            Service service = hostComponents.get(0).getComponentPO().getService();
            ServiceVO serviceVO = ServiceConverter.INSTANCE.fromEntity2VO(service);
            serviceVO.setQuickLinks(new ArrayList<>());

            boolean isHealthy = true;
            boolean isClient = true;
            for (HostComponent hostComponent : hostComponents) {
                ComponentPO componentPO = hostComponent.getComponentPO();

                String quickLink = componentPO.getQuickLink();
                if (StringUtils.isNotBlank(quickLink)) {
                    QuickLinkVO quickLinkVO = resolveQuickLink(hostComponent, quickLink);
                    serviceVO.getQuickLinks().add(quickLinkVO);
                }

                String category = componentPO.getCategory();
                if (!category.equalsIgnoreCase(ComponentCategories.CLIENT)) {
                    isClient = false;
                }

                MaintainState expectedState = category.equalsIgnoreCase(ComponentCategories.CLIENT)
                        ? MaintainState.INSTALLED
                        : MaintainState.STARTED;
                if (!hostComponent.getState().equals(expectedState)) {
                    isHealthy = false;
                }
            }

            serviceVO.setIsClient(isClient);
            serviceVO.setIsHealthy(isHealthy);
            res.add(serviceVO);
        }

        return res;
    }

    @Override
    public ServiceVO get(Long id) {
        Service service = serviceRepository.findById(id).orElse(new Service());
        return ServiceConverter.INSTANCE.fromEntity2VO(service);
    }

    private QuickLinkVO resolveQuickLink(HostComponent hostComponent, String quickLinkJson) {
        QuickLinkVO quickLinkVO = new QuickLinkVO();

        QuickLinkDTO quickLinkDTO = JsonUtils.readFromString(quickLinkJson, QuickLinkDTO.class);
        quickLinkVO.setDisplayName(quickLinkDTO.getDisplayName());

        ComponentPO componentPO = hostComponent.getComponentPO();
        ClusterPO clusterPO = componentPO.getClusterPO();
        Host host = hostComponent.getHost();
        Service service = componentPO.getService();
        ServiceConfig serviceConfig =
                serviceConfigRepository.findByClusterAndServiceAndSelectedIsTrue(clusterPO, service);
        List<TypeConfig> typeConfigs = serviceConfig.getConfigs();

        // Use HTTP for now, need to handle https in the future
        for (TypeConfig typeConfig : typeConfigs) {
            TypeConfigDTO typeConfigDTO = TypeConfigConverter.INSTANCE.fromEntity2DTO(typeConfig);
            for (PropertyDTO propertyDTO : typeConfigDTO.getProperties()) {
                if (propertyDTO.getName().equals(quickLinkDTO.getHttpPortProperty())) {
                    String port = propertyDTO.getValue().contains(":")
                            ? propertyDTO.getValue().split(":")[1]
                            : propertyDTO.getValue();
                    String url = "http://" + host.getHostname() + ":" + port;
                    quickLinkVO.setUrl(url);
                    return quickLinkVO;
                }
            }
        }

        String url = "http://" + host.getHostname() + ":" + quickLinkDTO.getHttpPortDefault();
        quickLinkVO.setUrl(url);
        return quickLinkVO;
    }
}
