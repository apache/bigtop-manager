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

import org.apache.bigtop.manager.dao.po.TypeConfigPO;
import org.apache.bigtop.manager.server.config.MapStructSharedConfig;
import org.apache.bigtop.manager.server.model.dto.TypeConfigDTO;
import org.apache.bigtop.manager.server.model.vo.TypeConfigVO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(
        uses = {ConverterTool.class},
        config = MapStructSharedConfig.class)
public interface TypeConfigConverter {

    TypeConfigConverter INSTANCE = Mappers.getMapper(TypeConfigConverter.class);

    @Mapping(target = "properties", source = "propertiesJson", qualifiedByName = "json2PropertyDTOList")
    TypeConfigDTO fromPO2DTO(TypeConfigPO typeConfigPO);

    List<TypeConfigDTO> fromPO2DTO(List<TypeConfigPO> typeConfigPOS);

    TypeConfigVO fromDTO2VO(TypeConfigDTO typeConfigDTO);

    List<TypeConfigVO> fromDTO2VO(List<TypeConfigDTO> typeConfigDTOList);

    @Mapping(target = "properties", source = "propertiesJson", qualifiedByName = "json2PropertyVOList")
    TypeConfigVO fromPO2VO(TypeConfigPO typeConfigPO);

    @Named("fromEntity2VO")
    List<TypeConfigVO> fromPO2VO(List<TypeConfigPO> typeConfigPOS);
}
