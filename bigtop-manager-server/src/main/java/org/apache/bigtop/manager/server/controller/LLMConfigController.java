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
import org.apache.bigtop.manager.server.model.vo.AuthPlatformVO;
import org.apache.bigtop.manager.server.model.vo.PlatformAuthCredentialVO;
import org.apache.bigtop.manager.server.model.vo.PlatformVO;
import org.apache.bigtop.manager.server.service.LLMConfigService;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.annotation.Resource;
import java.util.List;

@Tag(name = "LLM Config Controller")
@RestController
@RequestMapping("/llm/config/")
public class LLMConfigController {

    @Resource
    private LLMConfigService llmConfigService;

    @Operation(summary = "get platforms", description = "Get all platforms")
    @GetMapping("/platforms")
    public ResponseEntity<List<PlatformVO>> platforms() {
        return ResponseEntity.success(llmConfigService.platforms());
    }

    @Operation(summary = "platform credentials", description = "Get platform auth credentials")
    @GetMapping("/platforms/{platformId}/auth-credentials")
    public ResponseEntity<List<PlatformAuthCredentialVO>> platformsAuthCredential(@PathVariable Long platformId) {
        return ResponseEntity.success(llmConfigService.platformsAuthCredentials(platformId));
    }

    @Operation(summary = "get auth platforms", description = "Get authorized platforms")
    @GetMapping("/auth-platforms")
    public ResponseEntity<List<AuthPlatformVO>> authorizedPlatforms() {
        return ResponseEntity.success(llmConfigService.authorizedPlatforms());
    }

    @Operation(summary = "test auth platform", description = "Test authorized platforms")
    @PostMapping("/auth-platforms/test")
    public ResponseEntity<Boolean> testAuthorizedPlatform(@RequestBody AuthPlatformReq authPlatformReq) {
        AuthPlatformDTO authPlatformDTO = AuthPlatformConverter.INSTANCE.fromReq2DTO(authPlatformReq);
        return ResponseEntity.success(llmConfigService.testAuthorizedPlatform(authPlatformDTO));
    }

    @Operation(summary = "add auth platform", description = "Add authorized platforms")
    @PostMapping("/auth-platforms")
    public ResponseEntity<AuthPlatformVO> addAuthorizedPlatform(@RequestBody AuthPlatformReq authPlatformReq) {
        AuthPlatformDTO authPlatformDTO = AuthPlatformConverter.INSTANCE.fromReq2DTO(authPlatformReq);
        return ResponseEntity.success(llmConfigService.addAuthorizedPlatform(authPlatformDTO));
    }

    @Operation(summary = "update auth platform", description = "Update authorized platforms")
    @PutMapping("/auth-platforms/{authId}")
    public ResponseEntity<AuthPlatformVO> updateAuthorizedPlatform(
            @PathVariable Long authId, @RequestBody AuthPlatformReq authPlatformReq) {
        AuthPlatformDTO authPlatformDTO = AuthPlatformConverter.INSTANCE.fromReq2DTO(authPlatformReq);
        authPlatformDTO.setId(authId);
        return ResponseEntity.success(llmConfigService.updateAuthorizedPlatform(authPlatformDTO));
    }

    @Operation(summary = "delete auth platform", description = "Delete authorized platforms")
    @DeleteMapping("/auth-platforms/{authId}")
    public ResponseEntity<Boolean> deleteAuthorizedPlatform(@PathVariable Long authId) {
        return ResponseEntity.success(llmConfigService.deleteAuthorizedPlatform(authId));
    }
}
