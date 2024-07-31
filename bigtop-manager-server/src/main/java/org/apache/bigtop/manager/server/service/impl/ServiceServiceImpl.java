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
import org.apache.bigtop.manager.dao.po.ServicePO;
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
        Map<Long, List<HostComponentPO>> serviceIdToHostComponent =
                hostComponentRepository.findAllByComponentPOClusterId(clusterId).stream()
                        .collect(Collectors.groupingBy(hostComponent ->
                                hostComponent.getComponentPO().getServicePO().getId()));

        for (Map.Entry<Long, List<HostComponentPO>> entry : serviceIdToHostComponent.entrySet()) {
            List<HostComponentPO> hostComponentPOList = entry.getValue();
            ServicePO servicePO = hostComponentPOList.get(0).getComponentPO().getServicePO();
            ServiceVO serviceVO = ServiceConverter.INSTANCE.fromEntity2VO(servicePO);
            serviceVO.setQuickLinks(new ArrayList<>());

            boolean isHealthy = true;
            boolean isClient = true;
            for (HostComponentPO hostComponentPO : hostComponentPOList) {
                ComponentPO componentPO = hostComponentPO.getComponentPO();

                String quickLink = componentPO.getQuickLink();
                if (StringUtils.isNotBlank(quickLink)) {
                    QuickLinkVO quickLinkVO = resolveQuickLink(hostComponentPO, quickLink);
                    serviceVO.getQuickLinks().add(quickLinkVO);
                }

                String category = componentPO.getCategory();
                if (!category.equalsIgnoreCase(ComponentCategories.CLIENT)) {
                    isClient = false;
                }

                MaintainState expectedState = category.equalsIgnoreCase(ComponentCategories.CLIENT)
                        ? MaintainState.INSTALLED
                        : MaintainState.STARTED;
                if (!hostComponentPO.getState().equals(expectedState)) {
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
        ServicePO servicePO = serviceRepository.findById(id).orElse(new ServicePO());
        return ServiceConverter.INSTANCE.fromEntity2VO(servicePO);
    }

    private QuickLinkVO resolveQuickLink(HostComponentPO hostComponentPO, String quickLinkJson) {
        QuickLinkVO quickLinkVO = new QuickLinkVO();

        QuickLinkDTO quickLinkDTO = JsonUtils.readFromString(quickLinkJson, QuickLinkDTO.class);
        quickLinkVO.setDisplayName(quickLinkDTO.getDisplayName());

        ComponentPO componentPO = hostComponentPO.getComponentPO();
        ClusterPO clusterPO = componentPO.getClusterPO();
        HostPO hostPO = hostComponentPO.getHostPO();
        ServicePO servicePO = componentPO.getServicePO();
        ServiceConfigPO serviceConfigPO =
                serviceConfigRepository.findByClusterPOAndServicePOAndSelectedIsTrue(clusterPO, servicePO);
        List<TypeConfigPO> typeConfigPOList = serviceConfigPO.getConfigs();

        // Use HTTP for now, need to handle https in the future
        for (TypeConfigPO typeConfigPO : typeConfigPOList) {
            TypeConfigDTO typeConfigDTO = TypeConfigConverter.INSTANCE.fromEntity2DTO(typeConfigPO);
            for (PropertyDTO propertyDTO : typeConfigDTO.getProperties()) {
                if (propertyDTO.getName().equals(quickLinkDTO.getHttpPortProperty())) {
                    String port = propertyDTO.getValue().contains(":")
                            ? propertyDTO.getValue().split(":")[1]
                            : propertyDTO.getValue();
                    String url = "http://" + hostPO.getHostname() + ":" + port;
                    quickLinkVO.setUrl(url);
                    return quickLinkVO;
                }
            }
        }

        String url = "http://" + hostPO.getHostname() + ":" + quickLinkDTO.getHttpPortDefault();
        quickLinkVO.setUrl(url);
        return quickLinkVO;
    }
}
