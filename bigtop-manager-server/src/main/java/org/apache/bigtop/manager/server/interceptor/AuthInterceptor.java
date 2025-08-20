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
import org.apache.bigtop.manager.common.utils.JsonUtils;
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
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    private ResponseEntity<?> responseEntity;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (checkLogin(request) && checkPermission()) {
            return HandlerInterceptor.super.preHandle(request, response, handler);
        } else {
            response.setHeader("Content-Type", "application/json; charset=UTF-8");
            response.getWriter().write(JsonUtils.writeAsString(responseEntity));
            return false;
        }
    }

    @Override
    public void postHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
            throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        clearStatus();

        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    private void clearStatus() {
        SessionUserHolder.clear();
    }

    private Boolean checkLogin(HttpServletRequest request) {
        String token = request.getHeader("Token");
        if (StringUtils.isBlank(token)) {
            responseEntity = ResponseEntity.error(ApiExceptionEnum.NEED_LOGIN);
            return false;
        }

        try {
            DecodedJWT decodedJWT = JWTUtils.resolveToken(token);
            Long userId = decodedJWT.getClaim(JWTUtils.CLAIM_ID).asLong();
            Integer tokenVersion =
                    decodedJWT.getClaim(JWTUtils.CLAIM_TOKEN_VERSION).asInt();

            // Check if the user exists
            UserVO userVO = CacheUtils.getCache(Caches.CACHE_USER, userId.toString(), UserVO.class);
            if (userVO == null) {
                userVO = userService.get(userId);
                if (userVO == null) {
                    responseEntity = ResponseEntity.error(ApiExceptionEnum.NEED_LOGIN);
                    return false;
                }
                // Explicitly set cache expiration time
                CacheUtils.setCache(
                        Caches.CACHE_USER, userId.toString(), userVO, Caches.USER_EXPIRE_TIME_DAYS, TimeUnit.DAYS);
            }

            // Check if the token version matches
            if (!Objects.equals(tokenVersion, userVO.getTokenVersion())) {
                CacheUtils.removeCache(Caches.CACHE_USER, userId.toString());
                responseEntity = ResponseEntity.error(ApiExceptionEnum.NEED_LOGIN);
                return false;
            }

            SessionUserHolder.setUserId(userId);
        } catch (Exception e) {
            responseEntity = ResponseEntity.error(ApiExceptionEnum.NEED_LOGIN);
            return false;
        }

        return true;
    }

    private Boolean checkPermission() {
        return true;
    }
}
