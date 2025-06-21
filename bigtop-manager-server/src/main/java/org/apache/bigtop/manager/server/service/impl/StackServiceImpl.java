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

import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.dao.repository.ClusterDao;
import org.apache.bigtop.manager.dao.repository.ServiceDao;
import org.apache.bigtop.manager.server.model.converter.ClusterConverter;
import org.apache.bigtop.manager.server.model.converter.ServiceConverter;
import org.apache.bigtop.manager.server.model.converter.StackConverter;
import org.apache.bigtop.manager.server.model.dto.PropertyDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceConfigDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.StackDTO;
import org.apache.bigtop.manager.server.model.vo.ClusterVO;
import org.apache.bigtop.manager.server.model.vo.ServiceClusterVO;
import org.apache.bigtop.manager.server.model.vo.StackVO;
import org.apache.bigtop.manager.server.service.StackService;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.apache.commons.collections4.CollectionUtils;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StackServiceImpl implements StackService {

    @Resource
    private ClusterDao clusterDao;

    @Resource
    private ServiceDao serviceDao;

    @Override
    public List<StackVO> list() {
        return list(true);
    }

    @Override
    public List<ServiceClusterVO> serviceClusters() {
        List<ServiceClusterVO> res = new ArrayList<>();
        // ID - ClusterVO map
        Map<Long, ClusterVO> clusterVOMap = ClusterConverter.INSTANCE.fromPO2VO(clusterDao.findAll()).stream()
                .collect(Collectors.toMap(ClusterVO::getId, Function.identity()));
        // Name - ServicePO map
        Map<String, List<ServicePO>> servicePONameMap =
                serviceDao.findAll().stream().collect(Collectors.groupingBy(ServicePO::getName));
        List<ServiceDTO> serviceDTOList = StackUtils.getAllStacks().stream()
                .map(StackUtils::getServiceDTOList)
                .flatMap(List::stream)
                .toList();
        for (ServiceDTO serviceDTO : serviceDTOList) {
            List<ClusterVO> clusterVOList = new ArrayList<>();
            List<ServicePO> servicePOList = servicePONameMap.get(serviceDTO.getName());
            if (!CollectionUtils.isEmpty(servicePOList)) {
                for (ServicePO servicePO : servicePOList) {
                    ClusterVO clusterVO = clusterVOMap.get(servicePO.getClusterId());
                    clusterVOList.add(clusterVO);
                }
            }

            res.add(new ServiceClusterVO(serviceDTO.getName(), clusterVOList));
        }

        return res;
    }

    @Tool(description = "List supported stacks")
    public List<StackVO> listStacks() {
        return list(false);
    }

    public List<StackVO> list(Boolean showContent) {
        List<StackVO> stackVOList = new ArrayList<>();
        for (Map.Entry<StackDTO, List<ServiceDTO>> entry : StackUtils.STACK_SERVICE_MAP.entrySet()) {
            StackDTO stackDTO = entry.getKey();
            List<ServiceDTO> serviceDTOList = entry.getValue();
            for (ServiceDTO serviceDTO : serviceDTOList) {
                List<ServiceConfigDTO> serviceConfigDTOs = StackUtils.SERVICE_CONFIG_MAP.get(serviceDTO.getName());
                if (!showContent) {
                    for (ServiceConfigDTO serviceConfigDTO : serviceConfigDTOs) {
                        for (PropertyDTO propertyDTO : serviceConfigDTO.getProperties()) {
                            if (propertyDTO.getName().equals("content")) {
                                propertyDTO.setValue(null);
                            }
                        }
                    }
                }
                serviceDTO.setConfigs(StackUtils.SERVICE_CONFIG_MAP.get(serviceDTO.getName()));
            }

            StackVO stackVO = StackConverter.INSTANCE.fromDTO2VO(stackDTO);
            stackVO.setServices(ServiceConverter.INSTANCE.fromDTO2VO(serviceDTOList));
            stackVOList.add(stackVO);
        }
        return stackVOList;
    }
}
