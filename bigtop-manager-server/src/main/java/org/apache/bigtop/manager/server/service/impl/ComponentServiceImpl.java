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

import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.repository.ComponentRepository;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.model.converter.ComponentConverter;
import org.apache.bigtop.manager.server.model.vo.ComponentVO;
import org.apache.bigtop.manager.server.service.ComponentService;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ComponentServiceImpl implements ComponentService {

    @Resource
    private ComponentRepository componentRepository;

    @Override
    public List<ComponentVO> list(Long clusterId) {
        List<ComponentVO> componentVOList = new ArrayList<>();
        componentRepository.findAllByClusterPOId(clusterId).forEach(component -> {
            ComponentVO componentVO = ComponentConverter.INSTANCE.fromEntity2VO(component);
            componentVOList.add(componentVO);
        });

        return componentVOList;
    }

    @Override
    public ComponentVO get(Long id) {
        ComponentPO componentPO = componentRepository
                .findById(id)
                .orElseThrow(() -> new ApiException(ApiExceptionEnum.COMPONENT_NOT_FOUND));
        return ComponentConverter.INSTANCE.fromEntity2VO(componentPO);
    }
}
