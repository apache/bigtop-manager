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
package org.apache.bigtop.manager.server.model.converter;

import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.server.config.MapStructSharedConfig;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.StackDTO;
import org.apache.bigtop.manager.server.model.vo.ServiceVO;
import org.apache.bigtop.manager.server.stack.model.ServiceModel;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        uses = {ComponentConverter.class, ConverterTool.class},
        config = MapStructSharedConfig.class)
public interface ServiceConverter {

    ServiceConverter INSTANCE = Mappers.getMapper(ServiceConverter.class);

    @Mapping(target = "packageSpecifics", source = "packageSpecifics", qualifiedByName = "obj2Json")
    @Mapping(target = "requiredServices", source = "requiredServices", qualifiedByName = "obj2Json")
    @Mapping(target = "clusterId", expression = "java(clusterPO.getId())")
    @Mapping(target = "components", ignore = true)
    ServicePO fromDTO2PO(ServiceDTO serviceDTO, @Context ClusterPO clusterPO);

    ServiceVO fromDTO2VO(ServiceDTO serviceDTO);

    List<ServiceVO> fromDTO2VO(List<ServiceDTO> serviceDTOList);

    @Mapping(target = "serviceGroup", source = "stackDTO.userGroup")
    ServiceVO fromDTO2VO(ServiceDTO serviceDTO, StackDTO stackDTO);

    default List<ServiceVO> fromDTO2VO(List<ServiceDTO> serviceDTOList, StackDTO stackDTO) {
        return serviceDTOList.stream()
                .map(serviceDTO -> fromDTO2VO(serviceDTO, stackDTO))
                .collect(Collectors.toList());
    }

    @Mapping(target = "serviceName", source = "name")
    @Mapping(target = "serviceDesc", source = "desc")
    @Mapping(target = "serviceVersion", source = "version")
    @Mapping(target = "serviceUser", source = "user")
    ServiceDTO fromModel2DTO(ServiceModel serviceModel);

    @Mapping(target = "requiredServices", source = "requiredServices", qualifiedByName = "json2List")
    @Mapping(target = "serviceGroup", source = "userGroup")
    ServiceVO fromPO2VO(ServicePO servicePO);

    List<ServiceVO> fromPO2VO(List<ServicePO> servicePOList);
}
