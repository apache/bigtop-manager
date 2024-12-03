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

import org.apache.bigtop.manager.dao.po.AuthPlatformPO;
import org.apache.bigtop.manager.dao.po.ChatThreadPO;
import org.apache.bigtop.manager.dao.po.PlatformPO;
import org.apache.bigtop.manager.server.config.MapStructSharedConfig;
import org.apache.bigtop.manager.server.model.dto.ChatThreadDTO;
import org.apache.bigtop.manager.server.model.req.ChatbotThreadReq;
import org.apache.bigtop.manager.server.model.vo.ChatThreadVO;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(
        uses = {ConverterTool.class},
        config = MapStructSharedConfig.class)
public interface ChatThreadConverter {
    ChatThreadConverter INSTANCE = Mappers.getMapper(ChatThreadConverter.class);

    @Mapping(source = "id", target = "threadId")
    @Mapping(target = "model", expression = "java(authPlatformPO.getModel())")
    @Mapping(target = "platformName", expression = "java(platformPO.getName())")
    ChatThreadVO fromPO2VO(
            ChatThreadPO platformAuthorizedPO, @Context AuthPlatformPO authPlatformPO, @Context PlatformPO platformPO);

    ChatThreadPO fromDTO2PO(ChatThreadDTO chatThreadDTO);

    ChatThreadDTO fromPO2DTO(ChatThreadPO chatThreadPO);

    ChatThreadDTO fromReq2DTO(ChatbotThreadReq chatbotThreadReq);
}
