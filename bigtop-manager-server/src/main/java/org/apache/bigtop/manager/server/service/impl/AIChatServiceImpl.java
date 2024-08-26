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

import dev.langchain4j.model.openai.OpenAiChatModelName;
import jakarta.annotation.Resource;
import org.apache.bigtop.manager.ai.assistant.GeneralAssistantFactory;
import org.apache.bigtop.manager.ai.assistant.provider.AIAssistantConfig;
import org.apache.bigtop.manager.ai.core.enums.PlatformType;
import org.apache.bigtop.manager.common.utils.DateUtils;
import org.apache.bigtop.manager.dao.po.ChatThreadPO;
import org.apache.bigtop.manager.dao.po.PlatformAuthorizedPO;
import org.apache.bigtop.manager.dao.po.PlatformPO;
import org.apache.bigtop.manager.dao.po.UserPO;
import org.apache.bigtop.manager.dao.repository.ChatThreadRepository;
import org.apache.bigtop.manager.dao.repository.PlatformAuthorizedRepository;
import org.apache.bigtop.manager.dao.repository.PlatformRepository;
import org.apache.bigtop.manager.dao.repository.UserRepository;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.holder.SessionUserHolder;
import org.apache.bigtop.manager.server.model.converter.ChatThreadConverter;
import org.apache.bigtop.manager.server.model.converter.PlatformAuthorizedConverter;
import org.apache.bigtop.manager.server.model.converter.PlatformConverter;
import org.apache.bigtop.manager.server.model.dto.PlatformDTO;
import org.apache.bigtop.manager.server.model.vo.ChatMessageVO;
import org.apache.bigtop.manager.server.model.vo.ChatThreadVO;
import org.apache.bigtop.manager.server.model.vo.PlatformAuthCredentialVO;
import org.apache.bigtop.manager.server.model.vo.PlatformAuthorizedVO;
import org.apache.bigtop.manager.server.model.vo.PlatformVO;
import org.apache.bigtop.manager.server.service.AIChatService;
import org.apache.bigtop.manager.ai.core.factory.AIAssistant;
import org.apache.bigtop.manager.ai.core.factory.AIAssistantFactory;
import org.apache.bigtop.manager.ai.core.provider.AIAssistantConfigProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.*;

@Slf4j
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

    private final AIAssistantFactory aiAssistantFactory = new GeneralAssistantFactory();

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
        UserPO userPO = userRepository.findById(userId).orElseThrow(() -> new ApiException(ApiExceptionEnum.NEED_LOGIN));

        List<PlatformAuthorizedPO> authorizedPlatformPOs = platformAuthorizedRepository.findAllByUserPO(userPO);
        for (PlatformAuthorizedPO authorizedPlatformPO : authorizedPlatformPOs) {
            authorizedPlatforms.add(PlatformAuthorizedConverter.INSTANCE.fromPO2VO(authorizedPlatformPO));
        }
        return authorizedPlatforms;
    }

    @Override
    public PlatformVO addAuthorizedPlatform(PlatformDTO platformDTO) {
        Optional<PlatformPO> optionalPlatform =  platformRepository.findById(platformDTO.getPlatformId());
        PlatformPO platformPO = optionalPlatform.orElse(null);
        if (platformPO == null) {
            return null;
        }
        Map<String,String> credentialNeed = platformPO.getCredential();
        Map<String,String> credentialGet =platformDTO.getAuthCredentials();
        Map<String,String> credentialSet = new HashMap<>();
        for (String key : credentialNeed.keySet()) {
            if (!credentialGet.containsKey(key)) {
                return null;
            }
            credentialSet.put(key, credentialGet.get(key));
        }

        // TODO: test connect

        Long userId = SessionUserHolder.getUserId();
        UserPO userPO = userRepository.findById(userId).orElseThrow(() -> new ApiException(ApiExceptionEnum.NEED_LOGIN));
        PlatformAuthorizedPO platformAuthorizedPO = new PlatformAuthorizedPO();
        platformAuthorizedPO.setCredentials(credentialSet);
        platformAuthorizedPO.setPlatformId(platformPO.getId());
        platformAuthorizedPO.setUserPO(userPO);
        platformAuthorizedRepository.save(platformAuthorizedPO);
        PlatformVO platformVO = PlatformConverter.INSTANCE.fromPO2VO(platformPO);
        log.info("Adding authorized platform {}", platformDTO.getAuthCredentials().toString());
        log.info(platformPO.getCredential().toString());
        log.info("Adding authorized platform: {}", platformDTO);
        log.info(platformDTO.getAuthCredentials().toString());
        return platformVO;
    }

    @Override
    public List<PlatformAuthCredentialVO> platformsAuthCredential(Long platformId) {
        PlatformPO platformPO = platformRepository.findById(platformId).orElse(null);
        if (platformPO == null) {
            return null;
        }
        List<PlatformAuthCredentialVO> platformAuthCredentialVOs = new ArrayList<>();
        for (String key : platformPO.getCredential().keySet()) {
            PlatformAuthCredentialVO platformAuthCredentialVO = new PlatformAuthCredentialVO(key, platformPO.getCredential().get(key));
            platformAuthCredentialVOs.add(platformAuthCredentialVO);
        }
        return platformAuthCredentialVOs;
    }

    @Override
    public int deleteAuthorizedPlatform(Long platformId) {
        Long userId = SessionUserHolder.getUserId();
        UserPO userPO = userRepository.findById(userId).orElseThrow(() -> new ApiException(ApiExceptionEnum.NEED_LOGIN));
        List<PlatformAuthorizedPO> authorizedPlatformPOs = platformAuthorizedRepository.findAllByUserPO(userPO);
        for (PlatformAuthorizedPO authorizedPlatformPO : authorizedPlatformPOs) {
            if (authorizedPlatformPO.getId().equals(platformId)) {
                platformAuthorizedRepository.deleteById(authorizedPlatformPO.getId());
                return 0;
            }
        }
        return 1;
    }

    @Override
    public ChatThreadVO createChatThreads(Long platformId, String model) {
        PlatformAuthorizedPO platformAuthorizedPO = platformAuthorizedRepository.findById(platformId).orElse(null);
        if (platformAuthorizedPO == null) {
            log.info("No platform auth {}", platformId);
            return null;
        }
        Long userId = SessionUserHolder.getUserId();
        UserPO userPO = userRepository.findById(userId).orElse(null);
        if (userPO != null && !Objects.equals(userId, userPO.getId())) {
            return null;
        }
        PlatformPO platformPO = platformRepository.findById(platformAuthorizedPO.getPlatformId()).orElse(null);
        if (platformPO == null) {
            log.info("No platform {}", platformAuthorizedPO.getPlatformId());
            return null;
        }
        List<String> support_models = List.of(platformPO.getSupportModels().split(","));
        if (!support_models.contains(model)) {
            return null;
        }
        ChatThreadPO chatThreadPO = new ChatThreadPO();
        chatThreadPO.setUserPO(userPO);
        chatThreadPO.setModel(model);
        chatThreadPO.setPlatformPO(platformAuthorizedPO);
        chatThreadRepository.save(chatThreadPO);
        return ChatThreadConverter.INSTANCE.fromPO2VO(chatThreadPO);
    }

    @Override
    public int deleteChatThreads(Long platformId, Long threadId) {
        Random random = new Random();
        int randomInt = random.nextInt();
        return randomInt % 2;
    }

    @Override
    public List<ChatThreadVO> getAllChatThreads(Long platformId, String model) {
        List<ChatThreadVO> chatThreads = new ArrayList<>();
        if (model.equals("GPT-3.5")) {
            ChatThreadVO chatThreadVO = new ChatThreadVO(1L, platformId, "GPT-3.5", DateUtils.format(new Date()));
            chatThreads.add(chatThreadVO);
            ChatThreadVO chatThreadVO2 = new ChatThreadVO(3L, platformId, "GPT-3.5", DateUtils.format(new Date()));
            chatThreads.add(chatThreadVO2);
        }
        if (model.equals("GPT-4o")) {
            ChatThreadVO chatThreadVO = new ChatThreadVO(2L, platformId, "GPT-4o", DateUtils.format(new Date()));
            chatThreads.add(chatThreadVO);
        }
        return chatThreads;
    }

    @Override
    public SseEmitter talk(Long platformId, Long threadId, String message) {
        ChatThreadPO chatThreadPO = chatThreadRepository.findById(threadId).orElse(null);
        if (chatThreadPO == null) {
            return null;
        }
        Long userId = SessionUserHolder.getUserId();
        UserPO userPO = userRepository.findById(userId).orElse(null);
        if (userPO == null) {
            return null;
        }
        if (!chatThreadPO.getUserPO().getId().equals(userPO.getId())) {
            return null;
        }
        PlatformAuthorizedPO platformAuthorizedPO = platformAuthorizedRepository.findById(platformId).orElse(null);
        if (platformAuthorizedPO == null) {
            return null;
        }
        if (!platformAuthorizedPO.getId().equals(platformId)) {
            return null;
        }
        AIAssistantConfigProvider configProvider;
        configProvider = AIAssistantConfig.builder().set("apiKey", "sk-YxmC0296FXAw7XaILnbsspfl8hO534G6KOUsajwdQCIspMVz")
                // The `baseUrl` has a default value that is automatically generated based on the `PlatformType`.
                .set("baseUrl", "https://api.chatanywhere.tech/v1")
                // default 30
                .set("memoryLen", "10")
                .set("modelName", OpenAiChatModelName.GPT_3_5_TURBO.toString())
                .build();

        AIAssistant aiAssistant = aiAssistantFactory.create(PlatformType.OPENAI, configProvider);
        Flux<String> stringFlux = aiAssistant.streamAsk("hello, write a 100 words story");

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
                emitter::complete
        );
        return emitter;
    }

    @Override
    public List<ChatMessageVO> history(Long platformId, Long threadId) {
        List<ChatMessageVO> chatMessages = new ArrayList<>();
        Random random = new Random();
        int numberOfMessages = random.nextInt(11);
        boolean isUser = true;

        for (int i = 0; i < numberOfMessages; i++) {
            String sender = isUser ? "user" : "AI";
            String messageText = isUser ? "hello" : "hello, I'm GPT";
            messageText += i;

            ChatMessageVO message = new ChatMessageVO(sender, messageText, DateUtils.format(new Date()));
            chatMessages.add(message);

            isUser = !isUser;
        }
        return chatMessages;
    }
}
