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

import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.server.config.MapStructSharedConfig;
import org.apache.bigtop.manager.server.model.dto.HostDTO;
import org.apache.bigtop.manager.server.model.req.HostReq;
import org.apache.bigtop.manager.server.model.vo.HostVO;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(config = MapStructSharedConfig.class)
public interface HostConverter {

    HostConverter INSTANCE = Mappers.getMapper(HostConverter.class);

    HostDTO fromReq2DTO(HostReq hostReq);

    HostVO fromPO2VO(HostPO hostPO);

    List<HostVO> fromPO2VO(List<HostPO> hostPOList);

    HostPO fromDTO2POWithoutHostname(HostDTO hostDTO);

    default List<HostPO> fromDTO2POList(HostDTO hostDTO) {
        return hostDTO.getHostnames().stream()
                .map(hostname -> {
                    HostPO hostPO = fromDTO2POWithoutHostname(hostDTO);
                    hostPO.setHostname(hostname);
                    return hostPO;
                })
                .collect(Collectors.toList());
    }
}
