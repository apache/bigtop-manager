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
package org.apache.bigtop.manager.server.service.impl;

import org.apache.bigtop.manager.dao.po.UserPO;
import org.apache.bigtop.manager.dao.repository.UserDao;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.model.dto.LoginDTO;
import org.apache.bigtop.manager.server.model.vo.LoginVO;
import org.apache.bigtop.manager.server.service.LoginService;
import org.apache.bigtop.manager.server.utils.JWTUtils;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class LoginServiceImpl implements LoginService {

    @Resource
    private UserDao userDao;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        UserPO userPO = userDao.findByUsername(loginDTO.getUsername());
        if (userPO == null || !loginDTO.getPassword().equalsIgnoreCase(userPO.getPassword())) {
            throw new ApiException(ApiExceptionEnum.INCORRECT_USERNAME_OR_PASSWORD);
        }

        if (!userPO.getStatus()) {
            throw new ApiException(ApiExceptionEnum.USER_IS_DISABLED);
        }

        String token = JWTUtils.generateToken(userPO.getId(), userPO.getUsername());

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        return loginVO;
    }
}
