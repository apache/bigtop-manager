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
package org.apache.bigtop.manager.server.service;

import org.apache.bigtop.manager.server.model.dto.PlatformDTO;
import org.apache.bigtop.manager.server.model.vo.ChatMessageVO;
import org.apache.bigtop.manager.server.model.vo.ChatThreadVO;
import org.apache.bigtop.manager.server.model.vo.PlatformAuthCredentialVO;
import org.apache.bigtop.manager.server.model.vo.PlatformAuthorizedVO;
import org.apache.bigtop.manager.server.model.vo.PlatformVO;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface AIChatService {
    List<PlatformVO> platforms();

    List<PlatformAuthorizedVO> authorizedPlatforms();

    PlatformVO addAuthorizedPlatform(PlatformDTO platformDTO);

    List<PlatformAuthCredentialVO> platformsAuthCredential(Long platformId);

    int deleteAuthorizedPlatform(Long platformId);

    ChatThreadVO createChatThreads(Long platformId, String model);

    int deleteChatThreads(Long platformId, Long threadId);

    List<ChatThreadVO> getAllChatThreads(Long platformId, String model);

    SseEmitter talk(Long platformId, Long threadId, String message);

    List<ChatMessageVO> history(Long platformId, Long threadId);
}
