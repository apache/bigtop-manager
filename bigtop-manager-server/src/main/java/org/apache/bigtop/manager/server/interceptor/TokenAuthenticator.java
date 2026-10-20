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
package org.apache.bigtop.manager.server.interceptor;

import org.apache.bigtop.manager.common.constants.Caches;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.holder.SessionUserHolder;
import org.apache.bigtop.manager.server.model.vo.UserVO;
import org.apache.bigtop.manager.server.service.UserService;
import org.apache.bigtop.manager.server.utils.CacheUtils;
import org.apache.bigtop.manager.server.utils.JWTUtils;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Shared JWT-based login check used by every {@link org.springframework.web.servlet.HandlerInterceptor}
 * that guards an HTTP entry point (REST API, MCP endpoints, ...).
 * <p>
 * Centralizing this logic ensures that any new entry point starts from the same
 * "must present a valid, current token" baseline instead of a hand-rolled (and
 * potentially forgotten) check.
 */
@Component
public class TokenAuthenticator {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTUtils jwtUtils;

    /**
     * Validates the {@code Token} header on {@code request}.
     * <p>
     * On success, the authenticated user id is stored in {@link SessionUserHolder} and
     * {@code null} is returned. On failure, the response body to write back is returned;
     * the caller is expected to reject the request in that case.
     */
    public ResponseEntity<?> authenticate(HttpServletRequest request) {
        String token = request.getHeader("Token");
        if (StringUtils.isBlank(token)) {
            return ResponseEntity.error(ApiExceptionEnum.NEED_LOGIN);
        }

        try {
            DecodedJWT decodedJWT = jwtUtils.resolveToken(token);
            Long userId = decodedJWT.getClaim(JWTUtils.CLAIM_ID).asLong();
            Integer tokenVersion =
                    decodedJWT.getClaim(JWTUtils.CLAIM_TOKEN_VERSION).asInt();

            // Check if the user exists
            UserVO userVO = CacheUtils.getCache(Caches.CACHE_USER, userId.toString(), UserVO.class);
            if (userVO == null) {
                userVO = userService.get(userId);
                if (userVO == null) {
                    return ResponseEntity.error(ApiExceptionEnum.NEED_LOGIN);
                }
                // Explicitly set cache expiration time
                CacheUtils.setCache(
                        Caches.CACHE_USER, userId.toString(), userVO, Caches.USER_EXPIRE_TIME_DAYS, TimeUnit.DAYS);
            }

            // Check if the token version matches
            if (!Objects.equals(tokenVersion, userVO.getTokenVersion())) {
                CacheUtils.removeCache(Caches.CACHE_USER, userId.toString());
                return ResponseEntity.error(ApiExceptionEnum.NEED_LOGIN);
            }

            SessionUserHolder.setUserId(userId);
            return null;
        } catch (Exception e) {
            return ResponseEntity.error(ApiExceptionEnum.NEED_LOGIN);
        }
    }
}
