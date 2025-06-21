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

import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.holder.SessionUserHolder;
import org.apache.bigtop.manager.server.utils.JWTUtils;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    private ResponseEntity<?> responseEntity;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        log.info(request.getRequestURI());
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
            SessionUserHolder.setUserId(decodedJWT.getClaim(JWTUtils.CLAIM_ID).asLong());
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
