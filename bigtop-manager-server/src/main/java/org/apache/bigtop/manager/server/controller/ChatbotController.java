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

import org.apache.bigtop.manager.server.model.converter.ChatThreadConverter;
import org.apache.bigtop.manager.server.model.dto.ChatThreadDTO;
import org.apache.bigtop.manager.server.model.req.ChatbotMessageReq;
import org.apache.bigtop.manager.server.model.req.ChatbotThreadReq;
import org.apache.bigtop.manager.server.model.vo.ChatMessageVO;
import org.apache.bigtop.manager.server.model.vo.ChatThreadVO;
import org.apache.bigtop.manager.server.service.ChatbotService;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.annotation.Resource;
import java.util.List;

@Tag(name = "Chatbot Controller")
@RestController
@RequestMapping("/llm/chatbot/")
public class ChatbotController {

    @Resource
    private ChatbotService chatbotService;

    @Operation(summary = "new thread", description = "Create a chat threads")
    @PostMapping("/threads")
    public ResponseEntity<ChatThreadVO> createChatThread(@RequestBody ChatbotThreadReq chatbotThreadReq) {
        ChatThreadDTO chatThreadDTO = ChatThreadConverter.INSTANCE.fromReq2DTO(chatbotThreadReq);
        return ResponseEntity.success(chatbotService.createChatThread(chatThreadDTO));
    }

    @Operation(summary = "update thread", description = "Update a chat threads")
    @PutMapping("/threads/{threadId}")
    public ResponseEntity<ChatThreadVO> updateChatThread(
            @PathVariable Long threadId, @RequestBody ChatbotThreadReq chatbotThreadReq) {
        ChatThreadDTO chatThreadDTO = ChatThreadConverter.INSTANCE.fromReq2DTO(chatbotThreadReq);
        chatThreadDTO.setId(threadId);
        return ResponseEntity.success(chatbotService.updateChatThread(chatThreadDTO));
    }

    @Operation(summary = "delete thread", description = "Delete a chat threads")
    @DeleteMapping("/threads/{threadId}")
    public ResponseEntity<Boolean> deleteChatThread(@PathVariable Long threadId) {
        return ResponseEntity.success(chatbotService.deleteChatThread(threadId));
    }

    @Operation(summary = "get thread", description = "Get a chat threads")
    @GetMapping("/threads/{threadId}")
    public ResponseEntity<ChatThreadVO> getChatThread(@PathVariable Long threadId) {
        return ResponseEntity.success(chatbotService.getChatThread(threadId));
    }

    @Operation(summary = "list threads", description = "List all threads of a auth platform")
    @GetMapping("/threads")
    public ResponseEntity<List<ChatThreadVO>> getAllChatThreads() {
        return ResponseEntity.success(chatbotService.getAllChatThreads());
    }

    @Operation(summary = "talk", description = "Talk with Chatbot")
    @PostMapping("/threads/{threadId}/talk")
    public SseEmitter talk(@PathVariable Long threadId, @RequestBody ChatbotMessageReq messageReq) {
        return chatbotService.talk(threadId, messageReq.getMessage());
    }

    @Operation(summary = "history", description = "Get chat records")
    @GetMapping("/threads/{threadId}/history")
    public ResponseEntity<List<ChatMessageVO>> history(@PathVariable Long threadId) {
        return ResponseEntity.success(chatbotService.history(threadId));
    }
}
