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

import org.apache.bigtop.manager.server.model.dto.ChangePasswordDTO;
import org.apache.bigtop.manager.server.model.dto.UserDTO;
import org.apache.bigtop.manager.server.model.vo.UserVO;

public interface UserService {

    /**
     * Get current login user
     *
     * @return User
     */
    UserVO current();

    /**
     * Get a user
     *
     * @param id user id
     * @return user
     */
    UserVO get(Long id);

    /**
     * Update a user
     *
     * @return user
     */
    UserVO update(UserDTO userDTO);

    /**
     * Change password
     *
     * @param changePasswordDTO changePasswordDTO
     */
    UserVO changePassword(ChangePasswordDTO changePasswordDTO);
}
