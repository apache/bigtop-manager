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

import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.model.dto.LoginDTO;
import org.apache.bigtop.manager.server.model.req.LoginReq;
import org.apache.bigtop.manager.server.model.vo.LoginVO;
import org.apache.bigtop.manager.server.service.LoginService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock
    private LoginService loginService;

    @InjectMocks
    private LoginController loginController;

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
    void loginReturnsSuccessForValidCredentials() {
        LoginReq loginReq = new LoginReq();
        loginReq.setUsername("validUser");
        loginReq.setPassword("validPassword");

        LoginVO loginVO = new LoginVO();
        when(loginService.login(any(LoginDTO.class))).thenReturn(loginVO);

        ResponseEntity<LoginVO> response = loginController.login(loginReq);

        assertEquals(loginVO, response.getData());
    }

    @Test
    void loginThrowsExceptionForMissingUsername() {
        LoginReq loginReq = new LoginReq();
        loginReq.setUsername("");
        loginReq.setPassword("validPassword");

        ApiException exception = assertThrows(ApiException.class, () -> loginController.login(loginReq));

        assertEquals(ApiExceptionEnum.USERNAME_OR_PASSWORD_REQUIRED, exception.getEx());
    }

    @Test
    void loginThrowsExceptionForMissingPassword() {
        LoginReq loginReq = new LoginReq();
        loginReq.setUsername("validUser");
        loginReq.setPassword("");

        ApiException exception = assertThrows(ApiException.class, () -> loginController.login(loginReq));

        assertEquals(ApiExceptionEnum.USERNAME_OR_PASSWORD_REQUIRED, exception.getEx());
    }

    @Test
    void loginThrowsExceptionForMissingUsernameAndPassword() {
        LoginReq loginReq = new LoginReq();
        loginReq.setUsername("");
        loginReq.setPassword("");

        ApiException exception = assertThrows(ApiException.class, () -> loginController.login(loginReq));

        assertEquals(ApiExceptionEnum.USERNAME_OR_PASSWORD_REQUIRED, exception.getEx());
    }
}
