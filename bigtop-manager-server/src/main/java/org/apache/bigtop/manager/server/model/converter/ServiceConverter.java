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

import org.apache.bigtop.manager.dao.po.Cluster;
import org.apache.bigtop.manager.dao.po.Service;
import org.apache.bigtop.manager.server.config.MapStructSharedConfig;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.vo.ServiceVO;
import org.apache.bigtop.manager.server.stack.model.ServiceModel;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(
        uses = {ComponentConverter.class, ConverterTool.class},
        config = MapStructSharedConfig.class)
public interface ServiceConverter {

    ServiceConverter INSTANCE = Mappers.getMapper(ServiceConverter.class);

    @Mapping(target = "osSpecifics", source = "osSpecifics", qualifiedByName = "obj2Json")
    @Mapping(target = "requiredServices", source = "requiredServices", qualifiedByName = "obj2Json")
    @Mapping(target = "cluster", expression = "java(cluster)")
    Service fromDTO2Entity(ServiceDTO serviceDTO, @Context Cluster cluster);

    ServiceVO fromDTO2VO(ServiceDTO serviceDTO);

    List<ServiceVO> fromDTO2VO(List<ServiceDTO> serviceDTOList);

    @Mapping(target = "serviceName", source = "name")
    @Mapping(target = "serviceDesc", source = "desc")
    @Mapping(target = "serviceVersion", source = "version")
    @Mapping(target = "serviceUser", source = "user")
    @Mapping(target = "serviceGroup", source = "group")
    ServiceDTO fromModel2DTO(ServiceModel serviceModel);

    @Mapping(target = "requiredServices", source = "requiredServices", qualifiedByName = "json2List")
    @Mapping(target = "clusterName", source = "cluster.clusterName")
    ServiceVO fromEntity2VO(Service service);

    List<ServiceVO> fromEntity2VO(List<Service> services);
}
