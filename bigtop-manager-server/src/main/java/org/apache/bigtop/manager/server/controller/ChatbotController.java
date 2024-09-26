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

import org.apache.bigtop.manager.server.model.converter.AuthPlatformConverter;
import org.apache.bigtop.manager.server.model.dto.AuthPlatformDTO;
import org.apache.bigtop.manager.server.model.req.AuthPlatformReq;
import org.apache.bigtop.manager.server.model.req.ChatbotMessageReq;
import org.apache.bigtop.manager.server.model.vo.AuthPlatformVO;
import org.apache.bigtop.manager.server.model.vo.ChatMessageVO;
import org.apache.bigtop.manager.server.model.vo.ChatThreadVO;
import org.apache.bigtop.manager.server.model.vo.PlatformAuthCredentialVO;
import org.apache.bigtop.manager.server.model.vo.PlatformVO;
import org.apache.bigtop.manager.server.service.ChatbotService;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.annotation.Resource;
import java.util.List;

@Tag(name = "Chatbot Controller")
@RestController
@RequestMapping("/chatbot/")
public class ChatbotController {

    @Resource
    private ChatbotService chatbotService;

    @Operation(summary = "get platforms", description = "Get all platforms")
    @GetMapping("/platforms")
    public ResponseEntity<List<PlatformVO>> platforms() {
        return ResponseEntity.success(chatbotService.platforms());
    }

    @Operation(summary = "platform credentials", description = "Get platform auth credentials")
    @GetMapping("/platforms/{platformId}/auth-credentials")
    public ResponseEntity<List<PlatformAuthCredentialVO>> platformsAuthCredential(@PathVariable Long platformId) {
        return ResponseEntity.success(chatbotService.platformsAuthCredentials(platformId));
    }

    @Operation(summary = "get auth platforms", description = "Get authorized platforms")
    @GetMapping("/auth-platforms")
    public ResponseEntity<List<AuthPlatformVO>> authorizedPlatforms() {
        return ResponseEntity.success(chatbotService.authorizedPlatforms());
    }

    @Operation(summary = "auth platform", description = "Add authorized platforms")
    @PostMapping("/auth-platforms")
    public ResponseEntity<AuthPlatformVO> addAuthorizedPlatform(@RequestBody AuthPlatformReq authPlatformReq) {
        AuthPlatformDTO authPlatformDTO = AuthPlatformConverter.INSTANCE.fromReq2DTO(authPlatformReq);
        return ResponseEntity.success(chatbotService.addAuthorizedPlatform(authPlatformDTO));
    }

    @Operation(summary = "delete auth platform", description = "Delete authorized platforms")
    @DeleteMapping("/auth-platforms/{authId}")
    public ResponseEntity<Boolean> deleteAuthorizedPlatform(@PathVariable Long authId) {
        return ResponseEntity.success(chatbotService.deleteAuthorizedPlatform(authId));
    }

    @Operation(summary = "new thread", description = "Create a chat threads")
    @PostMapping("/auth-platforms/{authId}/threads")
    public ResponseEntity<ChatThreadVO> createChatThreads(@PathVariable Long authId, @RequestParam String model) {
        return ResponseEntity.success(chatbotService.createChatThreads(authId, model));
    }

    @Operation(summary = "delete thread", description = "Delete a chat threads")
    @DeleteMapping("/auth-platforms/{authId}/threads/{threadId}")
    public ResponseEntity<Boolean> deleteChatThreads(@PathVariable Long authId, @PathVariable Long threadId) {
        return ResponseEntity.success(chatbotService.deleteChatThreads(authId, threadId));
    }

    @Operation(summary = "get threads", description = "Get all threads of a auth platform")
    @GetMapping("/auth-platforms/{authId}/threads")
    public ResponseEntity<List<ChatThreadVO>> getAllChatThreads(
            @PathVariable Long authId, @RequestParam String model) {
        return ResponseEntity.success(chatbotService.getAllChatThreads(authId, model));
    }

    @Operation(summary = "talk", description = "Talk with Chatbot")
    @PostMapping("/auth-platforms/{authId}/threads/{threadId}/talk")
    public SseEmitter talk(
            @PathVariable Long authId, @PathVariable Long threadId, @RequestBody ChatbotMessageReq messageReq) {
        return chatbotService.talk(authId, threadId, messageReq.getMessage());
    }

    @Operation(summary = "history", description = "Get chat records")
    @GetMapping("/auth-platforms/{authId}/threads/{threadId}/history")
    public ResponseEntity<List<ChatMessageVO>> history(@PathVariable Long authId, @PathVariable Long threadId) {
        return ResponseEntity.success(chatbotService.history(authId, threadId));
    }
}
