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
package org.apache.bigtop.manager.server.model.mapper;

import org.apache.bigtop.manager.dao.entity.HostComponent;
import org.apache.bigtop.manager.server.model.vo.HostComponentVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface HostComponentMapper {

    HostComponentMapper INSTANCE = Mappers.getMapper(HostComponentMapper.class);

    @Mapping(target = "componentName", source = "component.componentName")
    @Mapping(target = "displayName", source = "component.displayName")
    @Mapping(target = "category", source = "component.category")
    @Mapping(target = "serviceName", source = "component.service.serviceName")
    @Mapping(target = "clusterName", source = "component.cluster.clusterName")
    @Mapping(target = "hostname", source = "host.hostname")
    HostComponentVO fromEntity2VO(HostComponent hostComponent);

    List<HostComponentVO> fromEntity2VO(List<HostComponent> hostComponents);


}
