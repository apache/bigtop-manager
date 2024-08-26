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

import org.apache.bigtop.manager.server.model.converter.PlatformConverter;
import org.apache.bigtop.manager.server.model.dto.PlatformDTO;
import org.apache.bigtop.manager.server.model.req.PlatformReq;
import org.apache.bigtop.manager.server.model.vo.ChatMessageVO;
import org.apache.bigtop.manager.server.model.vo.ChatThreadVO;
import org.apache.bigtop.manager.server.model.vo.PlatformAuthCredentialVO;
import org.apache.bigtop.manager.server.model.vo.PlatformAuthorizedVO;
import org.apache.bigtop.manager.server.model.vo.PlatformVO;
import org.apache.bigtop.manager.server.service.AIChatService;
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

@Tag(name = "AI Chat Controller")
@RestController
@RequestMapping("/ai/chat/")
public class AIChatController {

    @Resource
    private AIChatService chatService;

    @Operation(summary = "platforms", description = "Get all platforms")
    @GetMapping("/platforms")
    public ResponseEntity<List<PlatformVO>> platforms() {
        return ResponseEntity.success(chatService.platforms());
    }

    @Operation(summary = "platforms", description = "Get authorized platforms")
    @GetMapping("/platforms/authorized")
    public ResponseEntity<List<PlatformAuthorizedVO>> authorizedPlatforms() {
        return ResponseEntity.success(chatService.authorizedPlatforms());
    }

    @Operation(summary = "platforms", description = "Get authorized platforms")
    @GetMapping("/platforms/{platformId}/auth/credential")
    public ResponseEntity<List<PlatformAuthCredentialVO>> platformsAuthCredential(@PathVariable Long platformId) {
        return ResponseEntity.success(chatService.platformsAuthCredential(platformId));
    }

    @Operation(summary = "platforms", description = "Add authorized platforms")
    @PutMapping("/platforms")
    public ResponseEntity<PlatformVO> addAuthorizedPlatform(@RequestBody PlatformReq platformReq) {
        PlatformDTO platformDTO = PlatformConverter.INSTANCE.fromReq2DTO(platformReq);
        return ResponseEntity.success(chatService.addAuthorizedPlatform(platformDTO));
    }

    @Operation(summary = "platforms", description = "Delete authorized platforms")
    @DeleteMapping("/platforms/{platformId}")
    public ResponseEntity<Boolean> deleteAuthorizedPlatform(@PathVariable Long platformId) {
        return ResponseEntity.success(chatService.deleteAuthorizedPlatform(platformId));
    }

    @Operation(summary = "new threads", description = "Create a chat threads")
    @PutMapping("/platforms/{platformId}/threads")
    public ResponseEntity<ChatThreadVO> createChatThreads(@PathVariable Long platformId, @RequestParam String model) {
        return ResponseEntity.success(chatService.createChatThreads(platformId, model));
    }

    @Operation(summary = "delete threads", description = "Delete a chat threads")
    @DeleteMapping("platforms/{platformId}/threads/{threadId}")
    public ResponseEntity<Boolean> deleteChatThreads(@PathVariable Long platformId, @PathVariable Long threadId) {
        return ResponseEntity.success(chatService.deleteChatThreads(platformId, threadId));
    }

    @Operation(summary = "get", description = "Get all threads of a platform")
    @GetMapping("platforms/{platformId}/threads")
    public ResponseEntity<List<ChatThreadVO>> getAllChatThreads(
            @PathVariable Long platformId, @RequestParam String model) {
        return ResponseEntity.success(chatService.getAllChatThreads(platformId, model));
    }

    @Operation(summary = "talk", description = "Talk with AI")
    @PostMapping("platforms/{platformId}/threads/{threadId}/talk")
    public SseEmitter talk(@PathVariable Long platformId, @PathVariable Long threadId, @RequestParam String message) {
        return chatService.talk(platformId, threadId, message);
    }

    @Operation(summary = "history", description = "Get chat records")
    @GetMapping("platforms/{platformId}/threads/{threadId}/history")
    public ResponseEntity<List<ChatMessageVO>> history(@PathVariable Long platformId, @PathVariable Long threadId) {
        return ResponseEntity.success(chatService.history(platformId, threadId));
    }
}
