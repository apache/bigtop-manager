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
import org.apache.bigtop.manager.server.model.converter.UserConverter;
import org.apache.bigtop.manager.server.model.dto.ChangePasswordDTO;
import org.apache.bigtop.manager.server.model.dto.UserDTO;
import org.apache.bigtop.manager.server.model.req.ChangePasswordReq;
import org.apache.bigtop.manager.server.model.req.UserReq;
import org.apache.bigtop.manager.server.model.vo.UserVO;
import org.apache.bigtop.manager.server.service.UserService;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.annotation.Resource;

@Tag(name = "User Controller")
@RestController
@RequestMapping("/users")
public class UserController {

    @Resource
    private UserService userService;

    @Operation(summary = "current", description = "Get current user")
    @GetMapping("/current")
    public ResponseEntity<UserVO> current() {
        return ResponseEntity.success(userService.current());
    }

    @Audit
    @Operation(summary = "update", description = "Update a user")
    @PutMapping
    public ResponseEntity<UserVO> update(@RequestBody @Validated UserReq userReq) {
        UserDTO userDTO = UserConverter.INSTANCE.fromReq2DTO(userReq);
        return ResponseEntity.success(userService.update(userDTO));
    }

    @Audit
    @Operation(summary = "changePassword", description = "Change password")
    @PutMapping("/change-password")
    public ResponseEntity<UserVO> changePassword(@RequestBody @Validated ChangePasswordReq changePasswordReq) {
        if (!StringUtils.hasText(changePasswordReq.getPassword())
                || !StringUtils.hasText(changePasswordReq.getNewPassword())
                || !StringUtils.hasText(changePasswordReq.getConfirmPassword())) {
            throw new ApiException(ApiExceptionEnum.PASSWORD_NOT_EMPTY);
        }

        ChangePasswordDTO changePasswordDTO = UserConverter.INSTANCE.fromReq2DTO(changePasswordReq);
        UserVO result = userService.changePassword(changePasswordDTO);
        return ResponseEntity.success(result);
    }
}
