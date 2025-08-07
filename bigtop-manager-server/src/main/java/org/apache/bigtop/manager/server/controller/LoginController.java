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

import org.apache.bigtop.manager.common.constants.Caches;
import org.apache.bigtop.manager.server.annotations.Audit;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.model.converter.LoginConverter;
import org.apache.bigtop.manager.server.model.dto.LoginDTO;
import org.apache.bigtop.manager.server.model.req.LoginReq;
import org.apache.bigtop.manager.server.model.vo.LoginVO;
import org.apache.bigtop.manager.server.service.LoginService;
import org.apache.bigtop.manager.server.utils.CacheUtils;
import org.apache.bigtop.manager.server.utils.PasswordUtils;
import org.apache.bigtop.manager.server.utils.Pbkdf2Utils;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Tag(name = "Login Controller")
@RestController
public class LoginController {

    @Resource
    private LoginService loginService;

    @Operation(summary = "salt", description = "Generate salt")
    @GetMapping(value = "/salt")
    public ResponseEntity<String> salt(String username) {
        String salt = Pbkdf2Utils.generateSalt(username);
        return ResponseEntity.success(salt);
    }

    @Operation(summary = "nonce", description = "Generate nonce")
    @GetMapping(value = "/nonce")
    public ResponseEntity<String> nonce(String username) {
        String nonce = PasswordUtils.randomString(16);
        String cacheKey = username + ":" + nonce;
        CacheUtils.setCache(Caches.CACHE_NONCE, cacheKey, nonce, Caches.NONCE_EXPIRE_TIME_MINUTES, TimeUnit.MINUTES);
        return ResponseEntity.success(nonce);
    }

    @Audit
    @Operation(summary = "login", description = "User Login")
    @PostMapping(value = "/login")
    public ResponseEntity<LoginVO> login(@RequestBody LoginReq loginReq) {
        if (!StringUtils.hasText(loginReq.getUsername()) || !StringUtils.hasText(loginReq.getPassword())) {
            throw new ApiException(ApiExceptionEnum.USERNAME_OR_PASSWORD_REQUIRED);
        }

        LoginDTO loginDTO = LoginConverter.INSTANCE.fromReq2DTO(loginReq);
        return ResponseEntity.success(loginService.login(loginDTO));
    }
}
