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

import org.apache.bigtop.manager.common.constants.Caches;
import org.apache.bigtop.manager.dao.po.UserPO;
import org.apache.bigtop.manager.dao.repository.UserDao;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.holder.SessionUserHolder;
import org.apache.bigtop.manager.server.model.converter.UserConverter;
import org.apache.bigtop.manager.server.model.dto.ChangePasswordDTO;
import org.apache.bigtop.manager.server.model.dto.UserDTO;
import org.apache.bigtop.manager.server.model.vo.UserVO;
import org.apache.bigtop.manager.server.service.UserService;
import org.apache.bigtop.manager.server.utils.CacheUtils;
import org.apache.bigtop.manager.server.utils.PasswordUtils;
import org.apache.bigtop.manager.server.utils.Pbkdf2Utils;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserDao userDao;

    @Override
    public UserVO current() {
        Long id = SessionUserHolder.getUserId();
        UserPO userPO = userDao.findOptionalById(id).orElseThrow(() -> new ApiException(ApiExceptionEnum.NEED_LOGIN));
        return UserConverter.INSTANCE.fromPO2VO(userPO);
    }

    @Override
    public UserVO get(Long id) {
        UserPO userPO = userDao.findById(id);
        return UserConverter.INSTANCE.fromPO2VO(userPO);
    }

    @Override
    public UserVO update(UserDTO userDTO) {
        Long id = SessionUserHolder.getUserId();
        UserPO userPO = userDao.findOptionalById(id).orElseThrow(() -> new ApiException(ApiExceptionEnum.NEED_LOGIN));
        userPO.setNickname(userDTO.getNickname());
        userDao.partialUpdateById(userPO);

        // Update user information in cache
        UserVO userVO = UserConverter.INSTANCE.fromPO2VO(userPO);
        CacheUtils.setCache(Caches.CACHE_USER, id.toString(), userVO, Caches.USER_EXPIRE_TIME_DAYS, TimeUnit.DAYS);
        return userVO;
    }

    @Override
    public UserVO changePassword(ChangePasswordDTO changePasswordDTO) {
        if (changePasswordDTO.getPassword().equals(changePasswordDTO.getNewPassword())) {
            throw new ApiException(ApiExceptionEnum.ORIGINAL_PASSWORD_SAME_AS_NEW_PASSWORD);
        }

        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new ApiException(ApiExceptionEnum.TWO_PASSWORDS_NOT_MATCH);
        }

        Long id = SessionUserHolder.getUserId();
        UserPO userPO = userDao.findOptionalById(id).orElseThrow(() -> new ApiException(ApiExceptionEnum.NEED_LOGIN));

        String password = Pbkdf2Utils.getPbkdf2Password(userPO.getUsername(), changePasswordDTO.getPassword());
        if (!PasswordUtils.checkBcryptPassword(password, userPO.getPassword())) {
            throw new ApiException(ApiExceptionEnum.ORIGINAL_PASSWORD_INCORRECT);
        }

        String newPassword = Pbkdf2Utils.getBcryptPassword(userPO.getUsername(), changePasswordDTO.getNewPassword());
        userPO.setPassword(newPassword);
        userPO.setTokenVersion(userPO.getTokenVersion() + 1);
        userDao.partialUpdateById(userPO);

        // Update user information in cache
        UserVO userVO = UserConverter.INSTANCE.fromPO2VO(userPO);
        CacheUtils.setCache(Caches.CACHE_USER, id.toString(), userVO, Caches.USER_EXPIRE_TIME_DAYS, TimeUnit.DAYS);
        return userVO;
    }
}
