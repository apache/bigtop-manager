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

package org.apache.bigtop.manager.server.service;

import org.apache.bigtop.manager.common.constants.Caches;
import org.apache.bigtop.manager.dao.po.UserPO;
import org.apache.bigtop.manager.dao.repository.UserDao;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.model.dto.LoginDTO;
import org.apache.bigtop.manager.server.service.impl.LoginServiceImpl;
import org.apache.bigtop.manager.server.utils.CacheUtils;
import org.apache.bigtop.manager.server.utils.PasswordUtils;
import org.apache.bigtop.manager.server.utils.Pbkdf2Utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoginServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private LoginService loginService = new LoginServiceImpl();

    private static final String USERNAME = "root";
    private static final String RAW_PASSWORD = "123456";

    private UserPO mockUser;
    private String nonce;

    @BeforeEach
    public void setUp() {
        nonce = PasswordUtils.randomString(16);
        String cacheKey = USERNAME + ":" + nonce;
        CacheUtils.setCache(Caches.CACHE_NONCE, cacheKey, nonce, Caches.NONCE_EXPIRE_TIME_MINUTES, TimeUnit.MINUTES);

        mockUser = new UserPO();
        mockUser.setId(1L);
        mockUser.setUsername(USERNAME);
        mockUser.setPassword(Pbkdf2Utils.getBcryptPassword(USERNAME, RAW_PASSWORD));
        mockUser.setStatus(true);
    }

    @Test
    public void testLogin_WhenUserNotFound_ShouldThrowException() {
        when(userDao.findByUsername(any())).thenReturn(null);

        ApiException exception = assertThrows(ApiException.class, () -> loginService.login(new LoginDTO()));
        assertEquals(ApiExceptionEnum.INCORRECT_USERNAME_OR_PASSWORD, exception.getEx());
    }

    @Test
    public void testLogin_WhenPasswordMismatch_ShouldThrowException() {
        LoginDTO loginDTO = createLoginDTO("wrong_password");

        when(userDao.findByUsername(any())).thenReturn(mockUser);

        ApiException exception = assertThrows(ApiException.class, () -> loginService.login(loginDTO));
        assertEquals(ApiExceptionEnum.INCORRECT_USERNAME_OR_PASSWORD, exception.getEx());
    }

    @Test
    public void testLogin_WhenUserIsDisabled_ShouldThrowException() {
        mockUser.setStatus(false);
        LoginDTO loginDTO = createLoginDTO(RAW_PASSWORD);

        when(userDao.findByUsername(any())).thenReturn(mockUser);

        ApiException exception = assertThrows(ApiException.class, () -> loginService.login(loginDTO));
        assertEquals(ApiExceptionEnum.USER_IS_DISABLED, exception.getEx());
    }

    @Test
    public void testLogin_WhenValidCredentials_ShouldReturnToken() {
        LoginDTO loginDTO = createLoginDTO(RAW_PASSWORD);

        when(userDao.findByUsername(any())).thenReturn(mockUser);

        Object result = loginService.login(loginDTO);
        assertNotNull(result);
    }

    private LoginDTO createLoginDTO(String password) {
        LoginDTO dto = new LoginDTO();
        dto.setUsername(USERNAME);
        dto.setPassword(Pbkdf2Utils.getPbkdf2Password(USERNAME, password));
        dto.setNonce(nonce);
        return dto;
    }
}
