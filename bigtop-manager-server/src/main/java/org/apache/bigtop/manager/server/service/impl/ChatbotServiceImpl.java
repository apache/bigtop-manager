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
import org.apache.bigtop.manager.ai.assistant.store.PersistentChatMemoryStore;
import org.apache.bigtop.manager.ai.core.enums.MessageType;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.core.factory.AIAssistantFactory;
import org.apache.bigtop.manager.dao.po.AuthPlatformPO;
import org.apache.bigtop.manager.dao.po.ChatMessagePO;
import org.apache.bigtop.manager.dao.po.ChatThreadPO;
import org.apache.bigtop.manager.dao.po.PlatformPO;
import org.apache.bigtop.manager.dao.repository.AuthPlatformDao;
import org.apache.bigtop.manager.dao.repository.ChatMessageDao;
import org.apache.bigtop.manager.dao.repository.ChatThreadDao;
import org.apache.bigtop.manager.dao.repository.PlatformDao;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.enums.AuthPlatformStatus;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.holder.SessionUserHolder;
import org.apache.bigtop.manager.server.model.converter.AuthPlatformConverter;
import org.apache.bigtop.manager.server.model.converter.ChatMessageConverter;
import org.apache.bigtop.manager.server.model.converter.ChatThreadConverter;
import org.apache.bigtop.manager.server.model.dto.AuthPlatformDTO;
import org.apache.bigtop.manager.server.model.dto.ChatThreadDTO;
import org.apache.bigtop.manager.server.model.vo.ChatMessageVO;
import org.apache.bigtop.manager.server.model.vo.ChatThreadVO;
import org.apache.bigtop.manager.server.service.ChatbotService;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class ChatbotServiceImpl implements ChatbotService {
    @Resource
    private PlatformDao platformDao;

    @Resource
    private AuthPlatformDao authPlatformDao;

    @Resource
    private ChatThreadDao chatThreadDao;

    @Resource
    private ChatMessageDao chatMessageDao;

    private AIAssistantFactory aiAssistantFactory;

    private static final int CHAT_THREAD_NAME_LENGTH = 100;

    public static String getNameFromMessage(String input) {
        if (input == null || input.length() <= CHAT_THREAD_NAME_LENGTH) {
            return input;
        } else {
            return input.substring(0, CHAT_THREAD_NAME_LENGTH);
        }
    }

    public AIAssistantFactory getAIAssistantFactory() {
        if (aiAssistantFactory == null) {
            aiAssistantFactory =
                    new GeneralAssistantFactory(new PersistentChatMemoryStore(chatThreadDao, chatMessageDao));
        }
        return aiAssistantFactory;
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

    private AIAssistantConfig getAIAssistantConfig(
            String model, Map<String, String> credentials, Map<String, String> configs) {
        return AIAssistantConfig.builder()
                .setModel(model)
                .setLanguage(LocaleContextHolder.getLocale().toString())
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
            Long threadId,
            Map<String, String> configs) {
        return getAIAssistantFactory()
                .create(getPlatformType(platformName), getAIAssistantConfig(model, credentials, configs), threadId);
    }

    @Override
    public ChatThreadVO createChatThread(ChatThreadDTO chatThreadDTO) {
        AuthPlatformPO authPlatformPO = getActiveAuthPlatform();
        if (authPlatformPO == null || authPlatformPO.getIsDeleted()) {
            throw new ApiException(ApiExceptionEnum.NO_PLATFORM_IN_USE);
        }
        AuthPlatformDTO authPlatformDTO = AuthPlatformConverter.INSTANCE.fromPO2DTO(authPlatformPO);
        Long userId = SessionUserHolder.getUserId();
        PlatformPO platformPO = platformDao.findById(authPlatformPO.getPlatformId());

        chatThreadDTO.setPlatformId(platformPO.getId());
        chatThreadDTO.setAuthId(authPlatformPO.getId());

        AIAssistant aiAssistant = buildAIAssistant(
                platformPO.getName(), authPlatformDTO.getModel(), authPlatformDTO.getAuthCredentials(), null, null);
        Map<String, String> threadInfo = aiAssistant.createThread();
        chatThreadDTO.setThreadInfo(threadInfo);
        ChatThreadPO chatThreadPO = ChatThreadConverter.INSTANCE.fromDTO2PO(chatThreadDTO);
        chatThreadPO.setUserId(userId);
        chatThreadDao.save(chatThreadPO);
        return ChatThreadConverter.INSTANCE.fromPO2VO(chatThreadPO);
    }

    @Override
    public boolean deleteChatThread(Long threadId) {
        ChatThreadPO chatThreadPO = chatThreadDao.findById(threadId);
        if (chatThreadPO == null || chatThreadPO.getIsDeleted()) {
            throw new ApiException(ApiExceptionEnum.CHAT_THREAD_NOT_FOUND);
        }

        chatThreadPO.setIsDeleted(true);
        chatThreadDao.partialUpdateById(chatThreadPO);
        List<ChatMessagePO> chatMessagePOS = chatMessageDao.findAllByThreadId(threadId);
        chatMessagePOS.forEach(chatMessagePO -> chatMessagePO.setIsDeleted(true));
        chatMessageDao.partialUpdateByIds(chatMessagePOS);

        return true;
    }

    @Override
    public List<ChatThreadVO> getAllChatThreads() {
        AuthPlatformPO authPlatformPO = getActiveAuthPlatform();
        if (authPlatformPO == null) {
            throw new ApiException(ApiExceptionEnum.NO_PLATFORM_IN_USE);
        }
        Long authId = authPlatformPO.getId();
        Long userId = SessionUserHolder.getUserId();
        List<ChatThreadPO> chatThreadPOS = chatThreadDao.findAllByAuthIdAndUserId(authId, userId);
        List<ChatThreadVO> chatThreads = new ArrayList<>();
        for (ChatThreadPO chatThreadPO : chatThreadPOS) {
            if (chatThreadPO.getIsDeleted()) {
                continue;
            }
            ChatThreadVO chatThreadVO = ChatThreadConverter.INSTANCE.fromPO2VO(chatThreadPO);
            chatThreads.add(chatThreadVO);
        }
        return chatThreads;
    }

    @Override
    public SseEmitter talk(Long threadId, String message) {
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

        if (chatThreadPO.getName() == null) {
            chatThreadPO.setName(getNameFromMessage(message));
            chatThreadDao.partialUpdateById(chatThreadPO);
        }

        AuthPlatformDTO authPlatformDTO = AuthPlatformConverter.INSTANCE.fromPO2DTO(authPlatformPO);
        ChatThreadDTO chatThreadDTO = ChatThreadConverter.INSTANCE.fromPO2DTO(chatThreadPO);
        PlatformPO platformPO = platformDao.findById(authPlatformPO.getPlatformId());
        AIAssistant aiAssistant = buildAIAssistant(
                platformPO.getName(),
                authPlatformDTO.getModel(),
                authPlatformDTO.getAuthCredentials(),
                chatThreadPO.getId(),
                chatThreadDTO.getThreadInfo());
        Flux<String> stringFlux = aiAssistant.streamAsk(message);

        SseEmitter emitter = new SseEmitter();
        stringFlux.subscribe(
                s -> {
                    try {
                        emitter.send(s);
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                },
                Throwable::printStackTrace,
                emitter::complete);

        emitter.onTimeout(emitter::complete);
        return emitter;
    }

    @Override
    public List<ChatMessageVO> history(Long threadId) {
        List<ChatMessageVO> chatMessages = new ArrayList<>();
        ChatThreadPO chatThreadPO = chatThreadDao.findById(threadId);
        if (chatThreadPO == null || chatThreadPO.getIsDeleted()) {
            throw new ApiException(ApiExceptionEnum.CHAT_THREAD_NOT_FOUND);
        }
        Long userId = SessionUserHolder.getUserId();
        if (!chatThreadPO.getUserId().equals(userId)) {
            throw new ApiException(ApiExceptionEnum.PERMISSION_DENIED);
        }
        List<ChatMessagePO> chatMessagePOs = chatMessageDao.findAllByThreadId(threadId);
        for (ChatMessagePO chatMessagePO : chatMessagePOs) {
            ChatMessageVO chatMessageVO = ChatMessageConverter.INSTANCE.fromPO2VO(chatMessagePO);
            MessageType sender = chatMessageVO.getSender();
            if (sender == null) {
                continue;
            }
            if (sender.equals(MessageType.USER) || sender.equals(MessageType.AI)) {
                chatMessages.add(chatMessageVO);
            }
        }
        return chatMessages;
    }

    @Override
    public ChatThreadVO updateChatThread(ChatThreadDTO chatThreadDTO) {
        ChatThreadPO chatThreadPO = chatThreadDao.findById(chatThreadDTO.getId());
        if (chatThreadPO == null || chatThreadPO.getIsDeleted()) {
            throw new ApiException(ApiExceptionEnum.CHAT_THREAD_NOT_FOUND);
        }
        Long userId = SessionUserHolder.getUserId();
        if (!chatThreadPO.getUserId().equals(userId)) {
            throw new ApiException(ApiExceptionEnum.PERMISSION_DENIED);
        }

        chatThreadPO.setName(chatThreadDTO.getName());
        chatThreadDao.partialUpdateById(chatThreadPO);

        return ChatThreadConverter.INSTANCE.fromPO2VO(chatThreadPO);
    }

    @Override
    public ChatThreadVO getChatThread(Long threadId) {
        ChatThreadPO chatThreadPO = chatThreadDao.findById(threadId);
        if (chatThreadPO == null || chatThreadPO.getIsDeleted()) {
            throw new ApiException(ApiExceptionEnum.CHAT_THREAD_NOT_FOUND);
        }
        Long userId = SessionUserHolder.getUserId();
        if (!chatThreadPO.getUserId().equals(userId)) {
            throw new ApiException(ApiExceptionEnum.PERMISSION_DENIED);
        }
        return ChatThreadConverter.INSTANCE.fromPO2VO(chatThreadPO);
    }
}
