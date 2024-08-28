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

import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.apache.bigtop.manager.ai.assistant.GeneralAssistantFactory;
import org.apache.bigtop.manager.ai.assistant.provider.AIAssistantConfig;
import org.apache.bigtop.manager.ai.assistant.store.PersistentChatMemoryStore;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.core.factory.AIAssistantFactory;
import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;
import org.apache.bigtop.manager.dao.po.ChatMessagePO;
import org.apache.bigtop.manager.dao.po.ChatThreadPO;
import org.apache.bigtop.manager.dao.po.PlatformAuthorizedPO;
import org.apache.bigtop.manager.dao.po.PlatformPO;
import org.apache.bigtop.manager.dao.po.UserPO;
import org.apache.bigtop.manager.dao.repository.ChatMessageRepository;
import org.apache.bigtop.manager.dao.repository.ChatThreadRepository;
import org.apache.bigtop.manager.dao.repository.PlatformAuthorizedRepository;
import org.apache.bigtop.manager.dao.repository.PlatformRepository;
import org.apache.bigtop.manager.dao.repository.UserRepository;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.enums.LocaleKeys;
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
import org.apache.bigtop.manager.server.utils.MessageSourceUtils;
import org.apache.bigtop.manager.ai.core.AbstractAIAssistant;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import reactor.core.publisher.Flux;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class AIChatServiceImpl implements AIChatService {
    @Resource
    private PlatformRepository platformRepository;

    @Resource
    private PlatformAuthorizedRepository platformAuthorizedRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private ChatThreadRepository chatThreadRepository;

    @Resource
    private ChatMessageRepository chatMessageRepository;

    private AIAssistantFactory aiAssistantFactory;

    private PersistentChatMemoryStore persistentChatMemoryStore;
    private final AIAssistantFactory aiTestFactory = new GeneralAssistantFactory();

    public AIAssistantFactory getAiAssistantFactory() {
        if (aiAssistantFactory == null) {
            aiAssistantFactory = new GeneralAssistantFactory(
                    new PersistentChatMemoryStore(chatThreadRepository, chatMessageRepository));
        }
        return aiAssistantFactory;
    }

    private AIAssistantConfig.Builder getAIAssistantConfigBuilder(PlatformAuthorizedDTO platformAuthorizedDTO) {
        AIAssistantConfig.Builder aiAssistantConfigBuilder = AIAssistantConfig.builder();
        for (String key : platformAuthorizedDTO.getCredentials().keySet()) {
            aiAssistantConfigBuilder = aiAssistantConfigBuilder.set(
                    key, platformAuthorizedDTO.getCredentials().get(key));
        }
        aiAssistantConfigBuilder = aiAssistantConfigBuilder.set("baseUrl", platformAuthorizedDTO.getBaseUrl());
        aiAssistantConfigBuilder = aiAssistantConfigBuilder.set("memoryLen", "10");
        aiAssistantConfigBuilder = aiAssistantConfigBuilder.set("modelName", platformAuthorizedDTO.getModel());
        return aiAssistantConfigBuilder;
    }

    private AIAssistant buildAIAssistant(PlatformAuthorizedDTO platformAuthorizedDTO, Long threadId) {
        AIAssistant aiAssistant = null;
        AIAssistantConfig.Builder aiAssistantConfigBuilder = getAIAssistantConfigBuilder(platformAuthorizedDTO);
        aiAssistant = getAiAssistantFactory().create(platformAuthorizedDTO.getPlatformName().toLowerCase(),aiAssistantConfigBuilder.build(),threadId);
        return aiAssistant;
    }

    private Boolean testAuthorization(PlatformAuthorizedDTO platformAuthorizedDTO) {
        AIAssistantConfig.Builder aiAssistantConfigBuilder = getAIAssistantConfigBuilder(platformAuthorizedDTO);
        AIAssistant aiAssistant = aiTestFactory.create(platformAuthorizedDTO.getPlatformName().toLowerCase(),aiAssistantConfigBuilder.build());
        if (aiAssistant == null) {
            return false;
        }
        try {
            aiAssistant.ask("Answer one word.");
        } catch (Exception e) {
            throw new ApiException(ApiExceptionEnum.CREDIT_INCORRECT, e.getMessage());
        }
        return true;
    }

    @Override
    public List<PlatformVO> platforms() {
        List<PlatformPO> platformPOs = platformRepository.findAll();
        List<PlatformVO> platforms = new ArrayList<>();
        for (PlatformPO platformPO : platformPOs) {
            platforms.add(PlatformConverter.INSTANCE.fromPO2VO(platformPO));
        }
        return platforms;
    }

    @Override
    public List<PlatformAuthorizedVO> authorizedPlatforms() {
        List<PlatformAuthorizedVO> authorizedPlatforms = new ArrayList<>();
        Long userId = SessionUserHolder.getUserId();
        UserPO userPO =
                userRepository.findById(userId).orElseThrow(() -> new ApiException(ApiExceptionEnum.NEED_LOGIN));

        List<PlatformAuthorizedPO> authorizedPlatformPOs = platformAuthorizedRepository.findAllByUserPO(userPO);
        for (PlatformAuthorizedPO authorizedPlatformPO : authorizedPlatformPOs) {
            authorizedPlatforms.add(PlatformAuthorizedConverter.INSTANCE.fromPO2VO(authorizedPlatformPO));
        }
        return authorizedPlatforms;
    }

    @Override
    public PlatformAuthorizedVO addAuthorizedPlatform(PlatformDTO platformDTO) {
        Optional<PlatformPO> optionalPlatform = platformRepository.findById(platformDTO.getPlatformId());
        PlatformPO platformPO =
                optionalPlatform.orElseThrow(() -> new ApiException(ApiExceptionEnum.PLATFORM_NOT_FOUND));
        Map<String, String> credentialNeed = platformPO.getCredential();
        Map<String, String> credentialGet = platformDTO.getAuthCredentials();
        Map<String, String> credentialSet = new HashMap<>();
        for (String key : credentialNeed.keySet()) {
            if (!credentialGet.containsKey(key)) {
                throw new ApiException(ApiExceptionEnum.CREDIT_INCORRECT);
            }
            credentialSet.put(key, credentialGet.get(key));
        }

        PlatformAuthorizedDTO platformAuthorizedDTO = new PlatformAuthorizedDTO();
        platformAuthorizedDTO.setPlatformName(platformPO.getName());
        platformAuthorizedDTO.setCredentials(credentialSet);
        platformAuthorizedDTO.setBaseUrl(platformPO.getApiUrl());
        platformAuthorizedDTO.setModel(platformPO.getSupportModels().split(",")[0]);

        if (!testAuthorization(platformAuthorizedDTO)) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_FOUND);
        }

        Long userId = SessionUserHolder.getUserId();
        UserPO userPO =
                userRepository.findById(userId).orElseThrow(() -> new ApiException(ApiExceptionEnum.NEED_LOGIN));
        PlatformAuthorizedPO platformAuthorizedPO = new PlatformAuthorizedPO();
        platformAuthorizedPO.setCredentials(credentialSet);
        platformAuthorizedPO.setPlatformPO(platformPO);
        platformAuthorizedPO.setUserPO(userPO);

        platformAuthorizedRepository.save(platformAuthorizedPO);
        PlatformAuthorizedVO platformAuthorizedVO =
                PlatformAuthorizedConverter.INSTANCE.fromPO2VO(platformAuthorizedPO);
        platformAuthorizedVO.setSupportModels(platformPO.getSupportModels());
        platformAuthorizedVO.setPlatformName(platformPO.getName());
        return platformAuthorizedVO;
    }

    @Override
    public List<PlatformAuthCredentialVO> platformsAuthCredential(Long platformId) {
        PlatformPO platformPO = platformRepository
                .findById(platformId)
                .orElseThrow(() -> new ApiException(ApiExceptionEnum.PLATFORM_NOT_FOUND));
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
        Long userId = SessionUserHolder.getUserId();
        UserPO userPO =
                userRepository.findById(userId).orElseThrow(() -> new ApiException(ApiExceptionEnum.NEED_LOGIN));
        List<PlatformAuthorizedPO> authorizedPlatformPOs = platformAuthorizedRepository.findAllByUserPO(userPO);
        for (PlatformAuthorizedPO authorizedPlatformPO : authorizedPlatformPOs) {
            if (authorizedPlatformPO.getId().equals(platformId)) {
                platformAuthorizedRepository.deleteById(authorizedPlatformPO.getId());
                return true;
            }
        }
        throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_FOUND);
    }

    @Override
    public ChatThreadVO createChatThreads(Long platformId, String model) {
        PlatformAuthorizedPO platformAuthorizedPO = platformAuthorizedRepository
                .findById(platformId)
                .orElseThrow(() -> new ApiException(ApiExceptionEnum.PLATFORM_NOT_AUTHORIZED));
        Long userId = SessionUserHolder.getUserId();
        UserPO userPO = userRepository.findById(userId).orElse(null);
        if (userPO == null
                || !Objects.equals(userId, platformAuthorizedPO.getUserPO().getId())) {
            throw new ApiException(ApiExceptionEnum.PERMISSION_DENIED);
        }
        PlatformPO platformPO = platformRepository
                .findById(platformAuthorizedPO.getPlatformPO().getId())
                .orElseThrow(() -> new ApiException(ApiExceptionEnum.PLATFORM_NOT_FOUND));
        List<String> support_models = List.of(platformPO.getSupportModels().split(","));
        if (!support_models.contains(model)) {
            throw new ApiException(ApiExceptionEnum.MODEL_NOT_SUPPORTED);
        }
        ChatThreadPO chatThreadPO = new ChatThreadPO();
        chatThreadPO.setUserPO(userPO);
        chatThreadPO.setModel(model);
        chatThreadPO.setPlatformAuthorizedPO(platformAuthorizedPO);
        chatThreadRepository.save(chatThreadPO);
        return ChatThreadConverter.INSTANCE.fromPO2VO(chatThreadPO);
    }

    @Override
    public boolean deleteChatThreads(Long platformId, Long threadId) {
        Long userId = SessionUserHolder.getUserId();
        UserPO userPO =
                userRepository.findById(userId).orElseThrow(() -> new ApiException(ApiExceptionEnum.NEED_LOGIN));
        List<ChatThreadPO> chatThreadPOS = chatThreadRepository.findAllByUserPO(userPO);
        for (ChatThreadPO chatThreadPO : chatThreadPOS) {
            if (chatThreadPO.getId().equals(threadId)
                    && chatThreadPO.getPlatformAuthorizedPO().getId().equals(platformId)) {
                chatThreadRepository.deleteById(threadId);
                return true;
            }
        }
        throw new ApiException(ApiExceptionEnum.CHAT_THREAD_NOT_FOUND);
    }

    @Override
    public List<ChatThreadVO> getAllChatThreads(Long platformId, String model) {
        PlatformAuthorizedPO platformAuthorizedPO = platformAuthorizedRepository
                .findById(platformId)
                .orElseThrow(() -> new ApiException(ApiExceptionEnum.PLATFORM_NOT_AUTHORIZED));
        List<ChatThreadPO> chatThreadPOS = chatThreadRepository.findAllByPlatformAuthorizedPO(platformAuthorizedPO);
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
        ChatThreadPO chatThreadPO = chatThreadRepository.findById(threadId).orElse(null);
        if (chatThreadPO == null) {
            throw new ApiException(ApiExceptionEnum.CHAT_THREAD_NOT_FOUND);
        }
        Long userId = SessionUserHolder.getUserId();
        UserPO userPO = userRepository.findById(userId).orElse(null);
        if (userPO == null || !Objects.equals(userId, chatThreadPO.getUserPO().getId())) {
            throw new ApiException(ApiExceptionEnum.CHAT_THREAD_NOT_FOUND);
        }
        PlatformAuthorizedPO platformAuthorizedPO =
                platformAuthorizedRepository.findById(platformId).orElse(null);
        if (platformAuthorizedPO == null || !Objects.equals(platformAuthorizedPO.getId(), platformId)) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_AUTHORIZED);
        }
        PlatformPO platformPO = platformRepository
                .findById(platformAuthorizedPO.getPlatformPO().getId())
                .orElseThrow(() -> new ApiException(ApiExceptionEnum.PLATFORM_NOT_FOUND));

        PlatformAuthorizedDTO platformAuthorizedDTO = new PlatformAuthorizedDTO();
        platformAuthorizedDTO.setPlatformName(platformPO.getName());
        platformAuthorizedDTO.setCredentials(platformAuthorizedPO.getCredentials());
        platformAuthorizedDTO.setBaseUrl(platformPO.getApiUrl());
        platformAuthorizedDTO.setModel(chatThreadPO.getModel());
        AIAssistant aiAssistant = buildAIAssistant(platformAuthorizedDTO, chatThreadPO.getId());
        if (aiAssistant == null) {
            throw new ApiException(ApiExceptionEnum.CREDIT_INCORRECT);
        }
        Flux<String> stringFlux = aiAssistant.streamAsk(message);

        SseEmitter emitter = new SseEmitter();
        stringFlux.subscribe(
                data -> {
                    try {
                        emitter.send(SseEmitter.event().name("message").data(data));
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError,
                emitter::complete);
        return emitter;
    }

    @Override
    public List<ChatMessageVO> history(Long platformId, Long threadId) {
        List<ChatMessageVO> chatMessages = new ArrayList<>();
        ChatThreadPO chatThreadPO = chatThreadRepository
                .findById(threadId)
                .orElseThrow(() -> new ApiException(ApiExceptionEnum.CHAT_THREAD_NOT_FOUND));
        List<ChatMessagePO> chatMessagePOs = chatMessageRepository.findAllByChatThreadPO(chatThreadPO);
        for (ChatMessagePO chatMessagePO : chatMessagePOs) {
            ChatMessageVO chatMessageVO = ChatMessageConverter.INSTANCE.fromPO2VO(chatMessagePO);
            if (chatMessageVO.getSender().equals("User")
                    || chatMessageVO.getSender().equals("AI")) {
                chatMessages.add(chatMessageVO);
            }
        }
        return chatMessages;
    }
}
