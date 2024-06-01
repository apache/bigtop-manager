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

import org.apache.bigtop.manager.server.annotations.Audit;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.holder.SessionUserHolder;
import org.apache.bigtop.manager.server.model.dto.LoginDTO;
import org.apache.bigtop.manager.server.model.mapper.LoginMapper;
import org.apache.bigtop.manager.server.model.req.LoginReq;
import org.apache.bigtop.manager.server.model.vo.LoginVO;
import org.apache.bigtop.manager.server.service.LoginService;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import jakarta.annotation.Resource;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Login Controller")
@RestController
public class LoginController {

    @Resource
    private LoginService loginService;

    @Audit
    @Operation(summary = "login", description = "User Login")
    @PostMapping(value = "/login")
    public ResponseEntity<LoginVO> login(@RequestBody LoginReq loginReq) {
        if (!StringUtils.hasText(loginReq.getUsername()) || !StringUtils.hasText(loginReq.getPassword())) {
            throw new ApiException(ApiExceptionEnum.USERNAME_OR_PASSWORD_REQUIRED);
        }

        LoginDTO loginDTO = LoginMapper.INSTANCE.fromReq2DTO(loginReq);
        return ResponseEntity.success(loginService.login(loginDTO));
    }

    @Operation(summary = "test", description = "test")
    @GetMapping(value = "/test")
    public ResponseEntity<String> test() {
        Long userId = SessionUserHolder.getUserId();
        // throw new ServerException(ServerExceptionStatus.USERNAME_OR_PASSWORD_REQUIRED);
        return ResponseEntity.success("111");
    }
}
