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

import org.apache.bigtop.manager.ai.assistant.config.GeneralAssistantConfig;
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
import org.apache.bigtop.manager.server.enums.ChatbotCommand;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.holder.SessionUserHolder;
import org.apache.bigtop.manager.server.model.converter.AuthPlatformConverter;
import org.apache.bigtop.manager.server.model.converter.ChatMessageConverter;
import org.apache.bigtop.manager.server.model.converter.ChatThreadConverter;
import org.apache.bigtop.manager.server.model.dto.AuthPlatformDTO;
import org.apache.bigtop.manager.server.model.dto.ChatThreadDTO;
import org.apache.bigtop.manager.server.model.vo.ChatMessageVO;
import org.apache.bigtop.manager.server.model.vo.ChatThreadVO;
import org.apache.bigtop.manager.server.model.vo.TalkVO;
import org.apache.bigtop.manager.server.service.ChatbotService;
import org.apache.bigtop.manager.server.tools.provider.AIServiceToolsProvider;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Resource
    private AIServiceToolsProvider aiServiceToolsProvider;

    @Resource
    private AIAssistantFactory aiAssistantFactory;

    @Override
    public ChatThreadVO createChatThread(ChatThreadDTO chatThreadDTO) {
        AuthPlatformPO authPlatformPO = validateAndGetActiveAuthPlatform();
        PlatformPO platformPO = platformDao.findById(authPlatformPO.getPlatformId());

        chatThreadDTO.setPlatformId(platformPO.getId());
        chatThreadDTO.setAuthId(authPlatformPO.getId());

        ChatThreadPO chatThreadPO = ChatThreadConverter.INSTANCE.fromDTO2PO(chatThreadDTO);
        chatThreadPO.setUserId(SessionUserHolder.getUserId());
        chatThreadDao.save(chatThreadPO);

        return ChatThreadConverter.INSTANCE.fromPO2VO(chatThreadPO, authPlatformPO, platformPO);
    }

    @Override
    public boolean deleteChatThread(Long threadId) {
        ChatThreadPO chatThreadPO = validateAndGetChatThread(threadId);
        chatThreadPO.setIsDeleted(true);
        chatThreadDao.partialUpdateById(chatThreadPO);

        List<ChatMessagePO> chatMessagePOS = chatMessageDao.findAllByThreadId(threadId);
        chatMessagePOS.forEach(chatMessagePO -> chatMessagePO.setIsDeleted(true));
        chatMessageDao.partialUpdateByIds(chatMessagePOS);

        return true;
    }

    @Override
    public List<ChatThreadVO> getAllChatThreads() {
        AuthPlatformPO authPlatformPO = validateAndGetActiveAuthPlatform();
        PlatformPO platformPO = platformDao.findById(authPlatformPO.getPlatformId());

        List<ChatThreadPO> chatThreadPOS =
                chatThreadDao.findAllByAuthIdAndUserId(authPlatformPO.getId(), SessionUserHolder.getUserId());
        List<ChatThreadVO> chatThreads = new ArrayList<>();
        for (ChatThreadPO chatThreadPO : chatThreadPOS) {
            if (chatThreadPO.getIsDeleted()) {
                continue;
            }
            ChatThreadVO chatThreadVO =
                    ChatThreadConverter.INSTANCE.fromPO2VO(chatThreadPO, authPlatformPO, platformPO);
            chatThreads.add(chatThreadVO);
        }
        return chatThreads;
    }

    @Override
    public SseEmitter talk(Long threadId, ChatbotCommand command, String message) {
        AIAssistant aiAssistant = prepareTalk(threadId, command);

        Flux<String> stringFlux =
                (command == null) ? aiAssistant.streamAsk(message) : Flux.just(aiAssistant.ask(message));

        SseEmitter emitter = new SseEmitter();

        stringFlux.subscribe(
                s -> sendTalkVO(emitter, s, null),
                throwable -> handleError(emitter, throwable),
                () -> completeEmitter(emitter));

        emitter.onTimeout(emitter::complete);

        return emitter;
    }

    @Override
    public List<ChatMessageVO> history(Long threadId) {
        List<ChatMessageVO> chatMessages = new ArrayList<>();
        validateAndGetChatThread(threadId);

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
        ChatThreadPO chatThreadPO = validateAndGetChatThread(chatThreadDTO.getId());

        chatThreadPO.setName(chatThreadDTO.getName());
        chatThreadDao.partialUpdateById(chatThreadPO);

        AuthPlatformPO authPlatformPO = authPlatformDao.findById(chatThreadPO.getAuthId());
        return ChatThreadConverter.INSTANCE.fromPO2VO(
                chatThreadPO, authPlatformPO, platformDao.findById(authPlatformPO.getPlatformId()));
    }

    @Override
    public List<String> getChatbotCommands() {
        return ChatbotCommand.getAllCommands();
    }

    @Override
    public ChatThreadVO getChatThread(Long threadId) {
        ChatThreadPO chatThreadPO = validateAndGetChatThread(threadId);

        AuthPlatformPO authPlatformPO = authPlatformDao.findById(chatThreadPO.getAuthId());
        return ChatThreadConverter.INSTANCE.fromPO2VO(
                chatThreadPO, authPlatformPO, platformDao.findById(authPlatformPO.getPlatformId()));
    }

    private AuthPlatformPO validateAndGetActiveAuthPlatform() {
        AuthPlatformPO authPlatform = null;
        List<AuthPlatformPO> authPlatformPOS = authPlatformDao.findAll();
        for (AuthPlatformPO authPlatformPO : authPlatformPOS) {
            if (AuthPlatformStatus.isActive(authPlatformPO.getStatus())) {
                authPlatform = authPlatformPO;
            }
        }
        if (authPlatform == null || authPlatform.getIsDeleted()) {
            throw new ApiException(ApiExceptionEnum.NO_PLATFORM_IN_USE);
        }
        return authPlatform;
    }

    private ChatThreadPO validateAndGetChatThread(Long threadId) {
        ChatThreadPO chatThreadPO = chatThreadDao.findById(threadId);
        if (chatThreadPO == null || chatThreadPO.getIsDeleted()) {
            throw new ApiException(ApiExceptionEnum.CHAT_THREAD_NOT_FOUND);
        }
        Long userId = SessionUserHolder.getUserId();
        if (!chatThreadPO.getUserId().equals(userId)) {
            throw new ApiException(ApiExceptionEnum.PERMISSION_DENIED);
        }
        return chatThreadPO;
    }

    private GeneralAssistantConfig getAIAssistantConfig(
            String platformName, String model, Map<String, String> credentials, Long id) {
        return GeneralAssistantConfig.builder()
                .setPlatformType(getPlatformType(platformName))
                .setModel(model)
                .setId(id)
                .setLanguage(LocaleContextHolder.getLocale().toString())
                .addCredentials(credentials)
                .build();
    }

    private PlatformType getPlatformType(String platformName) {
        return PlatformType.getPlatformType(platformName.toLowerCase());
    }

    private AIAssistant buildAIAssistant(
            String platformName, String model, Map<String, String> credentials, Long threadId, ChatbotCommand command) {
        return aiAssistantFactory.createAIService(
                getAIAssistantConfig(platformName, model, credentials, threadId),
                aiServiceToolsProvider.getToolsProvide(command));
    }

    private AIAssistant prepareTalk(Long threadId, ChatbotCommand command) {
        ChatThreadPO chatThreadPO = validateAndGetChatThread(threadId);
        AuthPlatformPO authPlatformPO = validateAndGetActiveAuthPlatform();

        if (!authPlatformPO.getId().equals(chatThreadPO.getAuthId())) {
            throw new ApiException(ApiExceptionEnum.PLATFORM_NOT_IN_USE);
        }

        AuthPlatformDTO authPlatformDTO = AuthPlatformConverter.INSTANCE.fromPO2DTO(authPlatformPO);
        PlatformPO platformPO = platformDao.findById(authPlatformPO.getPlatformId());

        return buildAIAssistant(
                platformPO.getName(),
                authPlatformDTO.getModel(),
                authPlatformDTO.getAuthCredentials(),
                threadId,
                command);
    }

    private void sendTalkVO(SseEmitter emitter, String content, String finishReason) {
        try {
            TalkVO talkVO = new TalkVO();
            talkVO.setContent(content);
            talkVO.setFinishReason(finishReason);
            emitter.send(talkVO);
        } catch (Exception e) {
            log.error("Error sending data to SseEmitter", e);
            emitter.completeWithError(e);
        }
    }

    private void handleError(SseEmitter emitter, Throwable throwable) {
        log.error("Error during SSE streaming: {}", throwable.getMessage(), throwable);
        sendTalkVO(emitter, null, "Error: " + throwable.getMessage());
        emitter.completeWithError(throwable);
    }

    private void completeEmitter(SseEmitter emitter) {
        sendTalkVO(emitter, null, "completed");
        emitter.complete();
    }
}
