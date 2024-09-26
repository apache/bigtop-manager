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
package org.apache.bigtop.manager.server.controller;

import org.apache.bigtop.manager.server.model.dto.AuthPlatformDTO;
import org.apache.bigtop.manager.server.model.dto.PlatformDTO;
import org.apache.bigtop.manager.server.model.req.AuthPlatformReq;
import org.apache.bigtop.manager.server.model.req.ChatbotMessageReq;
import org.apache.bigtop.manager.server.model.req.PlatformReq;
import org.apache.bigtop.manager.server.model.vo.AuthPlatformVO;
import org.apache.bigtop.manager.server.model.vo.ChatMessageVO;
import org.apache.bigtop.manager.server.model.vo.ChatThreadVO;
import org.apache.bigtop.manager.server.model.vo.PlatformAuthCredentialVO;
import org.apache.bigtop.manager.server.model.vo.PlatformVO;
import org.apache.bigtop.manager.server.service.ChatbotService;
import org.apache.bigtop.manager.server.utils.MessageSourceUtils;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatbotControllerTest {

    @Mock
    private ChatbotService chatbotService;

    @InjectMocks
    private ChatbotController chatbotController;

    private MockedStatic<MessageSourceUtils> mockedMessageSourceUtils;

    @BeforeEach
    void setUp() {
        mockedMessageSourceUtils = Mockito.mockStatic(MessageSourceUtils.class);
        when(MessageSourceUtils.getMessage(any())).thenReturn("Mocked message");
    }

    @AfterEach
    void tearDown() {
        mockedMessageSourceUtils.close();
    }

    @Test
    void getAllPlatforms() {
        List<PlatformVO> platforms = new ArrayList<>();
        when(chatbotService.platforms()).thenReturn(platforms);

        ResponseEntity<List<PlatformVO>> response = chatbotController.platforms();

        assertTrue(response.isSuccess());
        assertEquals(platforms, response.getData());
    }

    @Test
    void getAuthorizedPlatforms() {
        List<AuthPlatformVO> authorizedPlatforms = new ArrayList<>();
        when(chatbotService.authorizedPlatforms()).thenReturn(authorizedPlatforms);

        ResponseEntity<List<AuthPlatformVO>> response = chatbotController.authorizedPlatforms();

        assertTrue(response.isSuccess());
        assertEquals(authorizedPlatforms, response.getData());
    }

    @Test
    void platformsAuthCredential() {
        Long platformId = 1L;
        List<PlatformAuthCredentialVO> credentials = new ArrayList<>();
        when(chatbotService.platformsAuthCredentials(platformId)).thenReturn(credentials);

        ResponseEntity<List<PlatformAuthCredentialVO>> response = chatbotController.platformsAuthCredential(platformId);

        assertTrue(response.isSuccess());
        assertEquals(credentials, response.getData());
    }

    @Test
    void addAuthorizedPlatform() {
        AuthPlatformReq authPlatformReq = new AuthPlatformReq();
        AuthPlatformVO authorizedVO = new AuthPlatformVO();

        when(chatbotService.addAuthorizedPlatform(any(AuthPlatformDTO.class))).thenReturn(authorizedVO);

        ResponseEntity<AuthPlatformVO> response = chatbotController.addAuthorizedPlatform(authPlatformReq);

        assertTrue(response.isSuccess());
        assertEquals(authorizedVO, response.getData());
    }

    @Test
    void deleteAuthorizedPlatform() {
        Long platformId = 1L;
        when(chatbotService.deleteAuthorizedPlatform(platformId)).thenReturn(true);

        ResponseEntity<Boolean> response = chatbotController.deleteAuthorizedPlatform(platformId);

        assertTrue(response.isSuccess());
        assertEquals(true, response.getData());
    }

    @Test
    void createChatThreads() {
        Long platformId = 1L;
        String model = "model1";
        ChatThreadVO chatThread = new ChatThreadVO();

        when(chatbotService.createChatThreads(eq(platformId), eq(model))).thenReturn(chatThread);

        ResponseEntity<ChatThreadVO> response = chatbotController.createChatThreads(platformId, model);

        assertTrue(response.isSuccess());
        assertEquals(chatThread, response.getData());
    }

    @Test
    void deleteChatThreads() {
        Long platformId = 1L;
        Long threadId = 1L;

        when(chatbotService.deleteChatThreads(platformId, threadId)).thenReturn(true);

        ResponseEntity<Boolean> response = chatbotController.deleteChatThreads(platformId, threadId);

        assertTrue(response.isSuccess());
        assertEquals(true, response.getData());
    }

    @Test
    void getAllChatThreads() {
        Long platformId = 1L;
        String model = "model1";
        List<ChatThreadVO> chatThreads = new ArrayList<>();

        when(chatbotService.getAllChatThreads(eq(platformId), eq(model))).thenReturn(chatThreads);

        ResponseEntity<List<ChatThreadVO>> response = chatbotController.getAllChatThreads(platformId, model);

        assertTrue(response.isSuccess());
        assertEquals(chatThreads, response.getData());
    }

    @Test
    void talk() {
        Long platformId = 1L;
        Long threadId = 1L;
        ChatbotMessageReq messageReq = new ChatbotMessageReq();
        messageReq.setMessage("Hello");

        SseEmitter emitter = new SseEmitter();
        when(chatbotService.talk(eq(platformId), eq(threadId), eq(messageReq.getMessage())))
                .thenReturn(emitter);

        SseEmitter result = chatbotController.talk(platformId, threadId, messageReq);

        assertEquals(emitter, result);
    }

    @Test
    void history() {
        Long platformId = 1L;
        Long threadId = 1L;
        List<ChatMessageVO> history = new ArrayList<>();

        when(chatbotService.history(platformId, threadId)).thenReturn(history);

        ResponseEntity<List<ChatMessageVO>> response = chatbotController.history(platformId, threadId);

        assertTrue(response.isSuccess());
        assertEquals(history, response.getData());
    }
}
