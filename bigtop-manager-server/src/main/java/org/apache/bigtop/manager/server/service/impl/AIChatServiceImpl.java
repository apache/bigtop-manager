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
import org.apache.bigtop.manager.ai.core.enums.MessageSender;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.core.factory.AIAssistantFactory;
import org.apache.bigtop.manager.dao.po.ChatMessagePO;
import org.apache.bigtop.manager.dao.po.ChatThreadPO;
import org.apache.bigtop.manager.dao.po.PlatformAuthorizedPO;
import org.apache.bigtop.manager.dao.po.PlatformPO;
import org.apache.bigtop.manager.dao.repository.ChatMessageDao;
import org.apache.bigtop.manager.dao.repository.ChatThreadDao;
import org.apache.bigtop.manager.dao.repository.PlatformAuthorizedDao;
import org.apache.bigtop.manager.dao.repository.PlatformDao;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.holder.SessionUserHolder;
import org.apache.bigtop.manager.server.model.converter.ChatMessageConverter;
import org.apache.bigtop.manager.server.model.converter.ChatThreadConverter;
import org.apache.bigtop.manager.server.model.converter.PlatformAuthorizedConverter;
import org.apache.bigtop.manager.server.model.converter.PlatformConverter;
import org.apache.bigtop.manager.server.model.dto.PlatformAuthorizedDTO;
import org.apache.bigtop.manager.server.model.dto.PlatformDTO;
import org.apache.bigtop.manager.server.model.vo.ChatMessageVO;
import org.apache.bigtop.manager.server.model.vo.ChatThreadVO;
import org.apache.bigtop.manager.server.model.vo.PlatformAuthCredentialVO;
import org.apache.bigtop.manager.server.model.vo.PlatformAuthorizedVO;
import org.apache.bigtop.manager.server.model.vo.PlatformVO;
import org.apache.bigtop.manager.server.service.AIChatService;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import reactor.core.publisher.Flux;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AIChatServiceImpl implements AIChatService {
    @Resource
    private PlatformDao platformDao;

    @Resource
    private PlatformAuthorizedDao platformAuthorizedDao;

    @Resource
    private ChatThreadDao chatThreadDao;

    @Resource
    private ChatMessageDao chatMessageDao;

    private AIAssistantFactory aiAssistantFactory;

    private final AIAssistantFactory aiTestFactory = new GeneralAssistantFactory();

    public AIAssistantFactory getAiAssistantFactory() {
        if (aiAssistantFactory == null) {
            aiAssistantFactory =
                    new GeneralAssistantFactory(new PersistentChatMemoryStore(chatThreadDao, chatMessageDao));
        }
        return aiAssistantFactory;
    }

    private AIAssistantConfig getAIAssistantConfig(PlatformAuthorizedDTO platformAuthorizedDTO) {
        return AIAssistantConfig.builder()
                .setModel(platformAuthorizedDTO.getModel())
                .addCredentials(platformAuthorizedDTO.getCredentials())
                .build();
    }

    private PlatformType getPlatformType(String platformName) {
        return PlatformType.getPlatformType(platformName.toLowerCase());
    }

    private AIAssistant buildAIAssistant(PlatformAuthorizedDTO platformAuthorizedDTO, Long threadId) {
        return getAiAssistantFactory()
                .create(
                        getPlatformType(platformAuthorizedDTO.getPlatformName()),
                        getAIAssistantConfig(platformAuthorizedDTO),
                        threadId);
    }

    private Boolean testAuthorization(PlatformAuthorizedDTO platformAuthorizedDTO) {
        AIAssistant aiAssistant = aiTestFactory.create(
                getPlatformType(platformAuthorizedDTO.getPlatformName()), getAIAssistantConfig(platformAuthorizedDTO));
        try {
            aiAssistant.ask("1+1=");
        } catch (Exception e) {
            throw new ApiException(ApiExceptionEnum.CREDIT_INCORRECT, e.getMessage());
        }

        return true;
    }

    @Override
    public List<PlatformVO> platforms() {
        List<PlatformPO> platformPOs = platformDao.findAll();
        List<PlatformVO> platforms = new ArrayList<>();
        for (PlatformPO platformPO : platformPOs) {
            platforms.add(PlatformConverter.INSTANCE.fromPO2VO(platformPO));
        }
        return platforms;
    }

    @Override
    public List<PlatformAuthorizedVO> authorizedPlatforms() {
        List<PlatformAuthorizedVO> authorizedPlatforms = new ArrayList<>();
        List<PlatformAuthorizedPO> authorizedPlatformPOs = platformAuthorizedDao.findAll();
        for (PlatformAuthorizedPO authorizedPlatformPO : authorizedPlatformPOs) {
            PlatformPO platformPO = platformDao.findById(authorizedPlatformPO.getPlatformId());
            authorizedPlatforms.add(PlatformAuthorizedConverter.INSTANCE.fromPO2VO(authorizedPlatformPO, platformPO));
        }

        return authorizedPlatforms;
    }

    @Override
    public PlatformAuthorizedVO addAuthorizedPlatform(PlatformDTO platformDTO) {
        PlatformPO platformPO = platformDao.findByPlatformId(platformDTO.getPlatformId());
        if (platformPO == null) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_FOUND);
        }
        Map<String, String> credentialSet = getStringMap(platformDTO, platformPO);
        List<String> models = List.of(platformPO.getSupportModels().split(","));
        if (models.isEmpty()) {
            throw new ApiException(ApiExceptionEnum.MODEL_NOT_SUPPORTED);
        }
        PlatformAuthorizedDTO platformAuthorizedDTO =
                new PlatformAuthorizedDTO(platformPO.getName(), credentialSet, models.get(0));

        if (!testAuthorization(platformAuthorizedDTO)) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_FOUND);
        }

        PlatformAuthorizedPO platformAuthorizedPO = new PlatformAuthorizedPO();
        platformAuthorizedPO.setCredentials(credentialSet);
        platformAuthorizedPO.setPlatformId(platformPO.getId());

        platformAuthorizedDao.saveWithCredentials(platformAuthorizedPO);
        PlatformAuthorizedVO platformAuthorizedVO =
                PlatformAuthorizedConverter.INSTANCE.fromPO2VO(platformAuthorizedPO, platformPO);
        platformAuthorizedVO.setSupportModels(platformPO.getSupportModels());
        platformAuthorizedVO.setPlatformName(platformPO.getName());
        return platformAuthorizedVO;
    }

    private static @NotNull Map<String, String> getStringMap(PlatformDTO platformDTO, PlatformPO platformPO) {
        if (platformPO == null) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_FOUND);
        }
        Map<String, String> credentialNeed = platformPO.getCredential();
        Map<String, String> credentialGet = platformDTO.getAuthCredentials();
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
    public List<PlatformAuthCredentialVO> platformsAuthCredential(Long platformId) {
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
    public boolean deleteAuthorizedPlatform(Long platformId) {
        List<PlatformAuthorizedPO> authorizedPlatformPOs = platformAuthorizedDao.findAll();
        for (PlatformAuthorizedPO authorizedPlatformPO : authorizedPlatformPOs) {
            if (authorizedPlatformPO.getId().equals(platformId)) {
                platformAuthorizedDao.deleteById(authorizedPlatformPO.getId());
                return true;
            }
        }

        throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_FOUND);
    }

    @Override
    public ChatThreadVO createChatThreads(Long platformId, String model) {
        PlatformAuthorizedPO platformAuthorizedPO = platformAuthorizedDao.findByPlatformId(platformId);
        if (platformAuthorizedPO == null) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_FOUND);
        }
        Long userId = SessionUserHolder.getUserId();
        PlatformPO platformPO = platformDao.findByPlatformId(platformAuthorizedPO.getPlatformId());
        List<String> supportModels = List.of(platformPO.getSupportModels().split(","));
        if (!supportModels.contains(model)) {
            throw new ApiException(ApiExceptionEnum.MODEL_NOT_SUPPORTED);
        }
        ChatThreadPO chatThreadPO = new ChatThreadPO();
        chatThreadPO.setUserId(userId);
        chatThreadPO.setModel(model);
        chatThreadPO.setPlatformId(platformAuthorizedPO.getId());
        chatThreadDao.save(chatThreadPO);
        return ChatThreadConverter.INSTANCE.fromPO2VO(chatThreadPO);
    }

    @Override
    public boolean deleteChatThreads(Long platformId, Long threadId) {
        Long userId = SessionUserHolder.getUserId();
        List<ChatThreadPO> chatThreadPOS = chatThreadDao.findAllByUserId(userId);
        for (ChatThreadPO chatThreadPO : chatThreadPOS) {
            if (chatThreadPO.getId().equals(threadId)
                    && chatThreadPO.getPlatformId().equals(platformId)) {
                chatThreadDao.deleteById(threadId);
                return true;
            }
        }
        throw new ApiException(ApiExceptionEnum.CHAT_THREAD_NOT_FOUND);
    }

    @Override
    public List<ChatThreadVO> getAllChatThreads(Long platformId, String model) {
        Long userId = SessionUserHolder.getUserId();
        List<ChatThreadPO> chatThreadPOS = chatThreadDao.findAllByPlatformAuthorizedIdAndUserId(platformId, userId);
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
    public SseEmitter talk(Long platformId, Long threadId, String message) {
        ChatThreadPO chatThreadPO = chatThreadDao.findById(threadId);
        Long userId = SessionUserHolder.getUserId();
        if (chatThreadPO == null || !Objects.equals(userId, chatThreadPO.getUserId())) {
            throw new ApiException(ApiExceptionEnum.CHAT_THREAD_NOT_FOUND);
        }
        PlatformAuthorizedPO platformAuthorizedPO = platformAuthorizedDao.findByPlatformId(platformId);
        if (platformAuthorizedPO == null) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_AUTHORIZED);
        }

        PlatformPO platformPO = platformDao.findById(platformAuthorizedPO.getPlatformId());
        PlatformAuthorizedDTO platformAuthorizedDTO = new PlatformAuthorizedDTO(
                platformPO.getName(), platformAuthorizedPO.getCredentials(), chatThreadPO.getModel());
        AIAssistant aiAssistant = buildAIAssistant(platformAuthorizedDTO, chatThreadPO.getId());
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
    public List<ChatMessageVO> history(Long platformId, Long threadId) {
        List<ChatMessageVO> chatMessages = new ArrayList<>();
        ChatThreadPO chatThreadPO = chatThreadDao.findById(threadId);
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
            MessageSender sender = chatMessageVO.getSender();
            if (sender == null) {
                continue;
            }
            if (sender.equals(MessageSender.USER) || sender.equals(MessageSender.AI)) {
                chatMessages.add(chatMessageVO);
            }
        }
        return chatMessages;
    }
}
