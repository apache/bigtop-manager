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
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.holder.SessionUserHolder;
import org.apache.bigtop.manager.server.model.converter.AuthPlatformConverter;
import org.apache.bigtop.manager.server.model.converter.ChatMessageConverter;
import org.apache.bigtop.manager.server.model.converter.ChatThreadConverter;
import org.apache.bigtop.manager.server.model.converter.PlatformConverter;
import org.apache.bigtop.manager.server.model.dto.AuthPlatformDTO;
import org.apache.bigtop.manager.server.model.vo.AuthPlatformVO;
import org.apache.bigtop.manager.server.model.vo.ChatMessageVO;
import org.apache.bigtop.manager.server.model.vo.ChatThreadVO;
import org.apache.bigtop.manager.server.model.vo.PlatformAuthCredentialVO;
import org.apache.bigtop.manager.server.model.vo.PlatformVO;
import org.apache.bigtop.manager.server.service.ChatbotService;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
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

    public AIAssistantFactory getAIAssistantFactory() {
        if (aiAssistantFactory == null) {
            aiAssistantFactory =
                    new GeneralAssistantFactory(new PersistentChatMemoryStore(chatThreadDao, chatMessageDao));
        }
        return aiAssistantFactory;
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

    private Boolean testAuthorization(String platformName, String model, Map<String, String> credentials) {
        AIAssistant aiAssistant = getAIAssistantFactory()
                .create(getPlatformType(platformName), getAIAssistantConfig(model, credentials, null));
        try {
            return aiAssistant.test();
        } catch (Exception e) {
            throw new ApiException(ApiExceptionEnum.CREDIT_INCORRECT, e.getMessage());
        }
    }

    @Override
    public List<PlatformVO> platforms() {
        List<PlatformPO> platformPOs = platformDao.findAll();
        return PlatformConverter.INSTANCE.fromPO2VO(platformPOs);
    }

    @Override
    public List<PlatformAuthCredentialVO> platformsAuthCredentials(Long platformId) {
        PlatformPO platformPO = platformDao.findByPlatformId(platformId);
        if (platformPO == null) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_FOUND);
        }
        List<PlatformAuthCredentialVO> platformAuthCredentialVOs = new ArrayList<>();
        for (String key : platformPO.getCredential().keySet()) {
            PlatformAuthCredentialVO platformAuthCredentialVO =
                    new PlatformAuthCredentialVO(key, platformPO.getCredential().get(key));
            platformAuthCredentialVOs.add(platformAuthCredentialVO);
        }
        return platformAuthCredentialVOs;
    }

    @Override
    public List<AuthPlatformVO> authorizedPlatforms() {
        List<AuthPlatformVO> authorizedPlatforms = new ArrayList<>();
        List<AuthPlatformPO> authPlatformPOList = authPlatformDao.findAll();
        for (AuthPlatformPO authPlatformPO : authPlatformPOList) {
            if (authPlatformPO.getIsDeleted()) {
                continue;
            }

            PlatformPO platformPO = platformDao.findById(authPlatformPO.getPlatformId());
            authorizedPlatforms.add(AuthPlatformConverter.INSTANCE.fromPO2VO(authPlatformPO, platformPO));
        }

        return authorizedPlatforms;
    }

    @Override
    public AuthPlatformVO addAuthorizedPlatform(AuthPlatformDTO authPlatformDTO) {
        PlatformPO platformPO = platformDao.findByPlatformId(authPlatformDTO.getPlatformId());
        if (platformPO == null) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_FOUND);
        }
        Map<String, String> credentialSet = getStringMap(authPlatformDTO, platformPO);
        List<String> models = List.of(platformPO.getSupportModels().split(","));
        if (models.isEmpty()) {
            throw new ApiException(ApiExceptionEnum.MODEL_NOT_SUPPORTED);
        }

        if (!testAuthorization(platformPO.getName(), models.get(0), credentialSet)) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_FOUND);
        }

        AuthPlatformPO authPlatformPO = new AuthPlatformPO();
        authPlatformPO.setCredentials(credentialSet);
        authPlatformPO.setPlatformId(platformPO.getId());

        authPlatformDao.saveWithCredentials(authPlatformPO);
        AuthPlatformVO authPlatformVO = AuthPlatformConverter.INSTANCE.fromPO2VO(authPlatformPO, platformPO);
        authPlatformVO.setSupportModels(platformPO.getSupportModels());
        authPlatformVO.setPlatformName(platformPO.getName());
        return authPlatformVO;
    }

    private static @NotNull Map<String, String> getStringMap(AuthPlatformDTO authPlatformDTO, PlatformPO platformPO) {
        if (platformPO == null) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_FOUND);
        }
        Map<String, String> credentialNeed = platformPO.getCredential();
        Map<String, String> credentialGet = authPlatformDTO.getAuthCredentials();
        Map<String, String> credentialSet = new HashMap<>();
        for (String key : credentialNeed.keySet()) {
            if (!credentialGet.containsKey(key)) {
                throw new ApiException(ApiExceptionEnum.CREDIT_INCORRECT);
            }
            credentialSet.put(key, credentialGet.get(key));
        }
        return credentialSet;
    }

    @Override
    public boolean deleteAuthorizedPlatform(Long authId) {
        AuthPlatformPO authPlatformPO = authPlatformDao.findByAuthId(authId);
        if (authPlatformPO == null) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_AUTHORIZED);
        }

        authPlatformPO.setIsDeleted(true);
        authPlatformDao.partialUpdateById(authPlatformPO);

        List<ChatThreadPO> chatThreadPOS = chatThreadDao.findAllByAuthId(authPlatformPO.getId());
        for (ChatThreadPO chatThreadPO : chatThreadPOS) {
            chatThreadPO.setIsDeleted(true);
            chatThreadDao.partialUpdateById(chatThreadPO);
            List<ChatMessagePO> chatMessagePOS = chatMessageDao.findAllByThreadId(chatThreadPO.getId());
            for (ChatMessagePO chatMessagePO : chatMessagePOS) {
                chatMessagePO.setIsDeleted(true);
                chatMessageDao.partialUpdateById(chatMessagePO);
            }
        }

        return true;
    }

    @Override
    public ChatThreadVO createChatThreads(Long authId, String model) {
        AuthPlatformPO authPlatformPO = authPlatformDao.findByAuthId(authId);
        if (authPlatformPO == null) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_AUTHORIZED);
        }
        Long userId = SessionUserHolder.getUserId();
        PlatformPO platformPO = platformDao.findByPlatformId(authPlatformPO.getPlatformId());
        List<String> supportModels = List.of(platformPO.getSupportModels().split(","));
        if (!supportModels.contains(model)) {
            throw new ApiException(ApiExceptionEnum.MODEL_NOT_SUPPORTED);
        }
        ChatThreadPO chatThreadPO = new ChatThreadPO();
        chatThreadPO.setUserId(userId);
        chatThreadPO.setModel(model);
        chatThreadPO.setAuthId(authPlatformPO.getId());
        chatThreadPO.setPlatformId(authPlatformPO.getPlatformId());

        AIAssistant aiAssistant = buildAIAssistant(
                platformPO.getName(), chatThreadPO.getModel(), authPlatformPO.getCredentials(), null, null);
        Map<String, String> threadInfo = aiAssistant.createThread();
        chatThreadPO.setThreadInfo(threadInfo);
        chatThreadDao.saveWithThreadInfo(chatThreadPO);
        return ChatThreadConverter.INSTANCE.fromPO2VO(chatThreadPO);
    }

    @Override
    public boolean deleteChatThreads(Long authId, Long threadId) {
        ChatThreadPO chatThreadPO = chatThreadDao.findById(threadId);
        if (chatThreadPO == null) {
            throw new ApiException(ApiExceptionEnum.CHAT_THREAD_NOT_FOUND);
        }

        chatThreadPO.setIsDeleted(true);
        chatThreadDao.partialUpdateById(chatThreadPO);
        List<ChatMessagePO> chatMessagePOS = chatMessageDao.findAllByThreadId(threadId);
        for (ChatMessagePO chatMessagePO : chatMessagePOS) {
            chatMessagePO.setIsDeleted(true);
            chatMessageDao.partialUpdateById(chatMessagePO);
        }

        return true;
    }

    @Override
    public List<ChatThreadVO> getAllChatThreads(Long authId, String model) {
        Long userId = SessionUserHolder.getUserId();
        List<ChatThreadPO> chatThreadPOS = chatThreadDao.findAllByAuthIdAndUserId(authId, userId);
        List<ChatThreadVO> chatThreads = new ArrayList<>();
        for (ChatThreadPO chatThreadPO : chatThreadPOS) {
            ChatThreadVO chatThreadVO = ChatThreadConverter.INSTANCE.fromPO2VO(chatThreadPO);
            if (chatThreadVO.getModel().equals(model)) {
                chatThreads.add(chatThreadVO);
            }
        }
        return chatThreads;
    }

    @Override
    public SseEmitter talk(Long authId, Long threadId, String message) {
        ChatThreadPO chatThreadPO = chatThreadDao.findByThreadId(threadId);
        Long userId = SessionUserHolder.getUserId();
        if (!Objects.equals(userId, chatThreadPO.getUserId())) {
            throw new ApiException(ApiExceptionEnum.CHAT_THREAD_NOT_FOUND);
        }
        AuthPlatformPO authPlatformPO = authPlatformDao.findByAuthId(authId);
        if (authPlatformPO == null) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_AUTHORIZED);
        }

        PlatformPO platformPO = platformDao.findById(authPlatformPO.getPlatformId());
        AIAssistant aiAssistant = buildAIAssistant(
                platformPO.getName(),
                chatThreadPO.getModel(),
                authPlatformPO.getCredentials(),
                chatThreadPO.getId(),
                chatThreadPO.getThreadInfo());
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
    public List<ChatMessageVO> history(Long authId, Long threadId) {
        List<ChatMessageVO> chatMessages = new ArrayList<>();
        ChatThreadPO chatThreadPO = chatThreadDao.findByThreadId(threadId);
        if (chatThreadPO == null) {
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
}
