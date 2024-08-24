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

import org.apache.bigtop.manager.dao.mapper.UserMapper;
import org.apache.bigtop.manager.dao.po.UserPO;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.holder.SessionUserHolder;
import org.apache.bigtop.manager.server.model.converter.UserConverter;
import org.apache.bigtop.manager.server.model.dto.UserDTO;
import org.apache.bigtop.manager.server.model.vo.UserVO;
import org.apache.bigtop.manager.server.service.UserService;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public UserVO current() {
        Long id = SessionUserHolder.getUserId();
        UserPO userPO =
                userMapper.findOptionalById(id).orElseThrow(() -> new ApiException(ApiExceptionEnum.NEED_LOGIN));
        return UserConverter.INSTANCE.fromPO2VO(userPO);
    }

    @Override
    public UserVO update(UserDTO userDTO) {
        Long id = SessionUserHolder.getUserId();
        UserPO userPO =
                userMapper.findOptionalById(id).orElseThrow(() -> new ApiException(ApiExceptionEnum.NEED_LOGIN));
        userPO.setNickname(userDTO.getNickname());
        userMapper.updateById(userPO);
        return UserConverter.INSTANCE.fromPO2VO(userPO);
    }
}
