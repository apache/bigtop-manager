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

import org.apache.bigtop.manager.dao.po.UserPO;
import org.apache.bigtop.manager.dao.repository.UserDao;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.model.dto.LoginDTO;
import org.apache.bigtop.manager.server.service.impl.LoginServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoginServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private LoginService loginService = new LoginServiceImpl();

    @Test
    public void testLogin() {
        when(userDao.findByUsername(any())).thenReturn(null);
        assertEquals(
                ApiExceptionEnum.INCORRECT_USERNAME_OR_PASSWORD,
                assertThrows(ApiException.class, () -> loginService.login(new LoginDTO()))
                        .getEx());

        UserPO userPO = new UserPO();
        userPO.setStatus(false);
        userPO.setPassword("wrong_password");
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setPassword("password");
        when(userDao.findByUsername(any())).thenReturn(userPO);
        assertEquals(
                ApiExceptionEnum.INCORRECT_USERNAME_OR_PASSWORD,
                assertThrows(ApiException.class, () -> loginService.login(loginDTO))
                        .getEx());

        userPO.setPassword("password");
        when(userDao.findByUsername(any())).thenReturn(userPO);
        assertEquals(
                ApiExceptionEnum.USER_IS_DISABLED,
                assertThrows(ApiException.class, () -> loginService.login(loginDTO))
                        .getEx());

        userPO.setStatus(true);
        when(userDao.findByUsername(any())).thenReturn(userPO);
        assert loginService.login(loginDTO) != null;
    }
}
