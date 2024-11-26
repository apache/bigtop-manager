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

import org.apache.bigtop.manager.dao.po.ServiceConfigPO;
import org.apache.bigtop.manager.server.config.MapStructSharedConfig;
import org.apache.bigtop.manager.server.model.dto.ServiceConfigDTO;
import org.apache.bigtop.manager.server.model.vo.ServiceConfigVO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(
        uses = {ConverterTool.class},
        config = MapStructSharedConfig.class)
public interface ServiceConfigConverter {

    ServiceConfigConverter INSTANCE = Mappers.getMapper(ServiceConfigConverter.class);

    @Mapping(target = "properties", source = "propertiesJson", qualifiedByName = "json2PropertyDTOList")
    ServiceConfigDTO fromPO2DTO(ServiceConfigPO serviceConfigPO);

    List<ServiceConfigDTO> fromPO2DTO(List<ServiceConfigPO> serviceConfigPOList);

    @Mapping(target = "propertiesJson", source = "properties", qualifiedByName = "obj2Json")
    ServiceConfigPO fromDTO2PO(ServiceConfigDTO serviceConfigDTO);

    List<ServiceConfigPO> fromDTO2PO(List<ServiceConfigDTO> serviceConfigDTOList);

    @Mapping(target = "properties", source = "propertiesJson", qualifiedByName = "json2PropertyVOList")
    ServiceConfigVO fromPO2VO(ServiceConfigPO serviceConfigPO);

    List<ServiceConfigVO> fromPO2VO(List<ServiceConfigPO> serviceConfigPOList);
}
