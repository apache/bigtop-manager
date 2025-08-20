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
import org.apache.bigtop.manager.server.holder.SessionUserHolder;
import org.apache.bigtop.manager.server.model.dto.UserDTO;
import org.apache.bigtop.manager.server.service.impl.UserServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService = new UserServiceImpl();

    @Test
    public void testCurrentAndUpdate() {
        // Behavior of Mock SessionUserHolder
        try (MockedStatic<SessionUserHolder> mockedSessionHolder = mockStatic(SessionUserHolder.class)) {

            // Set SessionUserHolder. getUserId() to return 1L
            mockedSessionHolder.when(SessionUserHolder::getUserId).thenReturn(1L);

            when(userDao.findOptionalById(1L)).thenReturn(Optional.of(new UserPO()));
            assert userService.current() != null;

            UserDTO userDTO = new UserDTO();
            userDTO.setNickname("test");
            assert userService.update(userDTO).getNickname().equals("test");
        }
    }

    @Test
    public void testNeedLogin() {
        when(userDao.findOptionalById(any())).thenReturn(Optional.empty());
        ApiException exception1 = assertThrows(ApiException.class, () -> {
            userService.current();
        });
        assertEquals(ApiExceptionEnum.NEED_LOGIN, exception1.getEx());

        ApiException exception2 = assertThrows(ApiException.class, () -> {
            userService.update(new UserDTO());
        });
        assertEquals(ApiExceptionEnum.NEED_LOGIN, exception2.getEx());
    }
}
