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

import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.StackDTO;
import org.apache.bigtop.manager.server.model.dto.TypeConfigDTO;
import org.apache.bigtop.manager.server.model.converter.ComponentConverter;
import org.apache.bigtop.manager.server.model.converter.ServiceConverter;
import org.apache.bigtop.manager.server.model.converter.StackConverter;
import org.apache.bigtop.manager.server.model.converter.TypeConfigConverter;
import org.apache.bigtop.manager.server.model.vo.ServiceComponentVO;
import org.apache.bigtop.manager.server.model.vo.ServiceConfigVO;
import org.apache.bigtop.manager.server.model.vo.StackVO;
import org.apache.bigtop.manager.server.service.StackService;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.apache.commons.lang3.tuple.ImmutablePair;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StackServiceImpl implements StackService {

    @Override
    public List<StackVO> list() {
        List<StackVO> stackVOList = new ArrayList<>();
        Map<String, ImmutablePair<StackDTO, List<ServiceDTO>>> stackKeyMap = StackUtils.getStackKeyMap();

        for (ImmutablePair<StackDTO, List<ServiceDTO>> pair : stackKeyMap.values()) {
            StackDTO stackDTO = pair.left;
            List<ServiceDTO> serviceDTOList = pair.right;

            StackVO stackVO = StackConverter.INSTANCE.fromDTO2VO(stackDTO);
            stackVO.setServices(ServiceConverter.INSTANCE.fromDTO2VO(serviceDTOList));
            stackVOList.add(stackVO);
        }

        return stackVOList;
    }

    @Override
    public List<ServiceComponentVO> components(String stackName, String stackVersion) {
        List<ServiceComponentVO> list = new ArrayList<>();

        ImmutablePair<StackDTO, List<ServiceDTO>> pair =
                StackUtils.getStackKeyMap().get(StackUtils.fullStackName(stackName, stackVersion));
        if (pair == null) {
            throw new ApiException(ApiExceptionEnum.STACK_NOT_FOUND);
        }

        List<ServiceDTO> serviceDTOList = pair.right;
        for (ServiceDTO serviceDTO : serviceDTOList) {
            ServiceComponentVO element = new ServiceComponentVO();
            element.setServiceName(serviceDTO.getServiceName());
            element.setComponents(ComponentConverter.INSTANCE.fromDTO2VO(serviceDTO.getComponents()));
            list.add(element);
        }

        return list;
    }

    @Override
    public List<ServiceConfigVO> configurations(String stackName, String stackVersion) {
        List<ServiceConfigVO> list = new ArrayList<>();
        Map<String, Map<String, List<TypeConfigDTO>>> stackConfigMap = StackUtils.getStackConfigMap();
        Map<String, List<TypeConfigDTO>> serviceConfigMap =
                stackConfigMap.get(StackUtils.fullStackName(stackName, stackVersion));

        for (Map.Entry<String, List<TypeConfigDTO>> entry : serviceConfigMap.entrySet()) {
            ServiceConfigVO element = new ServiceConfigVO();
            element.setServiceName(entry.getKey());
            element.setConfigs(TypeConfigConverter.INSTANCE.fromDTO2VO(entry.getValue()));
            list.add(element);
        }

        return list;
    }
}
