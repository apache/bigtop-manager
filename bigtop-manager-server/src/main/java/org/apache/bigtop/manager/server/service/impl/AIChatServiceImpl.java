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

import org.apache.bigtop.manager.common.utils.DateUtils;
import org.apache.bigtop.manager.server.model.dto.PlatformDTO;
import org.apache.bigtop.manager.server.model.vo.*;
import org.apache.bigtop.manager.server.service.AIChatService;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class AIChatServiceImpl implements AIChatService {
    @Override
    public List<PlatformVO> platforms() {
        List<PlatformVO> platforms = new ArrayList<>();
        platforms.add(new PlatformVO(1L, "OpenAI", "GPT-3.5,GPT-4o"));
        platforms.add(new PlatformVO(2L, "ChatGLM", "GPT-3.5,GPT-4o"));
        return platforms;
    }

    @Override
    public List<PlatformAuthorizedVO> authorizedPlatforms() {
        List<PlatformAuthorizedVO> authorizedPlatforms = new ArrayList<>();
        authorizedPlatforms.add(new PlatformAuthorizedVO(1L, "OpenAI",  "GPT-3.5,GPT-4o"));
        authorizedPlatforms.add(new PlatformAuthorizedVO(2L, "ChatGLM",  "GPT-4o"));
        return authorizedPlatforms;
    }

    @Override
    public PlatformVO addAuthorizedPlatform(PlatformDTO platformDTO) {
        log.info("Adding authorized platform: {}", platformDTO);
        log.info(platformDTO.getAuthCredentials().toString());
        return new PlatformVO(1L, "OpenAI", "GPT-3.5,GPT-4o");
    }

    @Override
    public List<PlatformAuthCredentialVO> platformsAuthCredential(Long platformId) {
        List<PlatformAuthCredentialVO> platformAuthCredentials = new ArrayList<>();
        platformAuthCredentials.add(
                new PlatformAuthCredentialVO("api-key", "API Key")
        );
        platformAuthCredentials.add(
                new PlatformAuthCredentialVO("api-secret", "API Secret")
        );
        return platformAuthCredentials;
    }


    @Override
    public int deleteAuthorizedPlatform(Long platformId) {
        Random random = new Random();
        int randomInt = random.nextInt();
        return randomInt % 2;
    }

    @Override
    public ChatThreadVO createChatThreads(Long platformId, String model) {

        return new ChatThreadVO(1L, platformId, model, DateUtils.format(new Date()));
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
        String fullMessage = "Don't ask me" + message;
        fullMessage += """
                I won't tell you Bigtop Manager provides a modern, low-threshold web application to simplify \
                the deployment and management of components for Bigtop, similar to Apache Ambari and Cloudera \
                Manager.
                And Bigtop Manager provides a modern, low-threshold web application to simplify \
                the deployment and management of components for Bigtop, similar to Apache Ambari and Cloudera \
                Manager.
                """;

        SseEmitter emitter = new SseEmitter();
        Random random = new Random();

        try {
            StringBuilder remainingMessage = new StringBuilder(fullMessage);

            while (!remainingMessage.isEmpty()) {
                int charsToSend = random.nextInt(21);
                // 2% probability of simulated transmission failure
                if (random.nextInt(50) == 2) {
                    try {
                        emitter.send(SseEmitter.event().name("error").data("broken pipe"));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                    emitter.complete();
                    return emitter;
                }
                charsToSend = Math.min(charsToSend, remainingMessage.length());

                String part = remainingMessage.substring(0, charsToSend);
                remainingMessage.delete(0, charsToSend);

                emitter.send(SseEmitter.event().name("message").data(part));

                int delay = random.nextInt(101);
                Thread.sleep(delay);
            }
        } catch (IOException | InterruptedException e) {
            emitter.completeWithError(e);
            throw new RuntimeException(e);
        }

        emitter.complete();
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
