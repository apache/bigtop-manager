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

import org.apache.bigtop.manager.server.model.req.ChatbotMessageReq;
import org.apache.bigtop.manager.server.model.req.ChatbotThreadReq;
import org.apache.bigtop.manager.server.model.vo.ChatMessageVO;
import org.apache.bigtop.manager.server.model.vo.ChatThreadVO;
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
    void createChatThread() {
        ChatThreadVO chatThread = new ChatThreadVO();
        when(chatbotService.createChatThread(any())).thenReturn(chatThread);

        ResponseEntity<ChatThreadVO> response = chatbotController.createChatThread(new ChatbotThreadReq());

        assertTrue(response.isSuccess());
        assertEquals(chatThread, response.getData());
    }

    @Test
    void deleteChatThread() {
        Long threadId = 1L;

        when(chatbotService.deleteChatThread(threadId)).thenReturn(true);

        ResponseEntity<Boolean> response = chatbotController.deleteChatThread(threadId);

        assertTrue(response.isSuccess());
        assertEquals(true, response.getData());
    }

    @Test
    void getAllChatThreads() {
        List<ChatThreadVO> chatThreads = new ArrayList<>();

        when(chatbotService.getAllChatThreads()).thenReturn(chatThreads);

        ResponseEntity<List<ChatThreadVO>> response = chatbotController.getAllChatThreads();

        assertTrue(response.isSuccess());
        assertEquals(chatThreads, response.getData());
    }

    @Test
    void talk() {
        Long threadId = 1L;
        ChatbotMessageReq messageReq = new ChatbotMessageReq();
        messageReq.setMessage("Hello");

        SseEmitter emitter = new SseEmitter();
        when(chatbotService.talk(eq(threadId), any(), eq(messageReq.getMessage())))
                .thenReturn(emitter);

        SseEmitter result = chatbotController.talk(threadId, messageReq);

        assertEquals(emitter, result);
    }

    @Test
    void history() {
        Long threadId = 1L;
        List<ChatMessageVO> history = new ArrayList<>();

        when(chatbotService.history(threadId)).thenReturn(history);

        ResponseEntity<List<ChatMessageVO>> response = chatbotController.history(threadId);

        assertTrue(response.isSuccess());
        assertEquals(history, response.getData());
    }
}
