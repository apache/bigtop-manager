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

import org.apache.bigtop.manager.ai.assistant.GeneralAssistantFactory;
import org.apache.bigtop.manager.ai.assistant.provider.AIAssistantConfig;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.dao.po.AuthPlatformPO;
import org.apache.bigtop.manager.dao.po.ChatThreadPO;
import org.apache.bigtop.manager.dao.po.PlatformPO;
import org.apache.bigtop.manager.dao.repository.AuthPlatformDao;
import org.apache.bigtop.manager.dao.repository.ChatMessageDao;
import org.apache.bigtop.manager.dao.repository.ChatThreadDao;
import org.apache.bigtop.manager.dao.repository.PlatformDao;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.enums.AuthPlatformStatus;
import org.apache.bigtop.manager.server.enums.ChatbotCommand;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.holder.SessionUserHolder;
import org.apache.bigtop.manager.server.model.converter.AuthPlatformConverter;
import org.apache.bigtop.manager.server.model.converter.ChatThreadConverter;
import org.apache.bigtop.manager.server.model.dto.AuthPlatformDTO;
import org.apache.bigtop.manager.server.model.dto.ChatThreadDTO;
import org.apache.bigtop.manager.server.service.LLMAgentService;
import org.apache.bigtop.manager.server.tools.AgentToolsProvider;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LLMAgentServiceImpl implements LLMAgentService {
    @Resource
    private PlatformDao platformDao;

    @Resource
    private AuthPlatformDao authPlatformDao;

    @Resource
    private ChatThreadDao chatThreadDao;

    @Resource
    private ChatMessageDao chatMessageDao;

    private AIAssistantConfig getAIAssistantConfig(
            String model, Map<String, String> credentials, Map<String, String> configs) {
        return AIAssistantConfig.builder()
                .setModel(model)
                .addCredentials(credentials)
                .addConfigs(configs)
                .build();
    }

    private PlatformType getPlatformType(String platformName) {
        return PlatformType.getPlatformType(platformName.toLowerCase());
    }

    private AIAssistant buildAIAssistant(
            String platformName,
            String model,
            Map<String, String> credentials,
            Map<String, String> configs,
            ChatbotCommand command) {
        GeneralAssistantFactory assistantFactory = new GeneralAssistantFactory();
        return assistantFactory.createWithTools(
                getPlatformType(platformName),
                getAIAssistantConfig(model, credentials, configs),
                new AgentToolsProvider(command));
    }

    private AuthPlatformPO getActiveAuthPlatform() {
        List<AuthPlatformPO> authPlatformPOS = authPlatformDao.findAll();
        for (AuthPlatformPO authPlatformPO : authPlatformPOS) {
            if (AuthPlatformStatus.isActive(authPlatformPO.getStatus())) {
                return authPlatformPO;
            }
        }
        return null;
    }

    @Override
    public List<String> getChatbotCommands() {
        return ChatbotCommand.getAllCommands();
    }

    @Override
    public SseEmitter talk(Long threadId, ChatbotCommand command, String message) {
        ChatThreadPO chatThreadPO = chatThreadDao.findById(threadId);
        Long userId = SessionUserHolder.getUserId();
        if (!Objects.equals(userId, chatThreadPO.getUserId()) || chatThreadPO.getIsDeleted()) {
            throw new ApiException(ApiExceptionEnum.CHAT_THREAD_NOT_FOUND);
        }
        AuthPlatformPO authPlatformPO = getActiveAuthPlatform();
        if (authPlatformPO == null
                || authPlatformPO.getIsDeleted()
                || !authPlatformPO.getId().equals(chatThreadPO.getAuthId())) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_IN_USE);
        }

        AuthPlatformDTO authPlatformDTO = AuthPlatformConverter.INSTANCE.fromPO2DTO(authPlatformPO);
        ChatThreadDTO chatThreadDTO = ChatThreadConverter.INSTANCE.fromPO2DTO(chatThreadPO);
        PlatformPO platformPO = platformDao.findById(authPlatformPO.getPlatformId());
        AIAssistant aiAssistant = buildAIAssistant(
                platformPO.getName(),
                authPlatformDTO.getModel(),
                authPlatformDTO.getAuthCredentials(),
                chatThreadDTO.getThreadInfo(),
                command);

        String result = aiAssistant.ask(message);
        SseEmitter emitter = new SseEmitter(30_000L);
        try {
            emitter.send(result);
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }

        emitter.onCompletion(() -> {
            System.out.println("Data has been sent, performing post-send actions.");
        });
        return emitter;
    }
}
