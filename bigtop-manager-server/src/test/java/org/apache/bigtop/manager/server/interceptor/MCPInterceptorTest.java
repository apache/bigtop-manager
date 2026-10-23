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
import org.apache.bigtop.manager.server.config.JwtProperties;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.enums.LocaleKeys;
import org.apache.bigtop.manager.server.holder.SessionUserHolder;
import org.apache.bigtop.manager.server.model.vo.UserVO;
import org.apache.bigtop.manager.server.service.UserService;
import org.apache.bigtop.manager.server.utils.CacheUtils;
import org.apache.bigtop.manager.server.utils.JWTUtils;
import org.apache.bigtop.manager.server.utils.MessageSourceUtils;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Regression test for the MCP endpoints' authentication.
 * <p>
 * {@link MCPInterceptor#checkAuthenticated} must enforce the same JWT check as
 * the regular REST API.
 */
@ExtendWith(MockitoExtension.class)
class MCPInterceptorTest {

    @Mock
    private UserService userService;

    private JWTUtils jwtUtils;

    private MCPInterceptor mcpInterceptor;

    private MockedStatic<MessageSourceUtils> messageSourceUtilsMock;

    @BeforeEach
    void setUp() {
        // ApiExceptionEnum#getMessage() delegates to MessageSourceUtils, which requires a
        // Spring MessageSource that isn't available in this plain unit test.
        messageSourceUtilsMock = mockStatic(MessageSourceUtils.class);
        messageSourceUtilsMock
                .when(() -> MessageSourceUtils.getMessage(any(LocaleKeys.class)))
                .thenReturn("Login required");

        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("unit-test-secret");
        jwtProperties.setIssuer("bigtop-manager");
        jwtProperties.setAudience("bigtop-manager");
        jwtProperties.setExpirationDays(7);
        jwtUtils = new JWTUtils(jwtProperties);

        TokenAuthenticator tokenAuthenticator = new TokenAuthenticator();
        ReflectionTestUtils.setField(tokenAuthenticator, "userService", userService);
        ReflectionTestUtils.setField(tokenAuthenticator, "jwtUtils", jwtUtils);

        mcpInterceptor = new MCPInterceptor();
        ReflectionTestUtils.setField(mcpInterceptor, "tokenAuthenticator", tokenAuthenticator);
    }

    @AfterEach
    void tearDown() {
        messageSourceUtilsMock.close();
        SessionUserHolder.clear();
        CacheUtils.clearCache(Caches.CACHE_USER);
    }

    @Test
    void preHandle_NoTokenHeader_BlocksRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = mcpInterceptor.preHandle(request, response, new Object());

        assertFalse(allowed, "MCP endpoints must reject requests without a Token header");
        assertNull(SessionUserHolder.getUserId());

        ResponseEntity<?> body = JsonUtils.readFromString(response.getContentAsString(), ResponseEntity.class);
        assertEquals(ApiExceptionEnum.NEED_LOGIN.getCode(), body.getCode());
    }

    @Test
    void preHandle_InvalidToken_BlocksRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Token", "not-a-valid-jwt");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = mcpInterceptor.preHandle(request, response, new Object());

        assertFalse(allowed);
        assertNull(SessionUserHolder.getUserId());
    }

    @Test
    void preHandle_UnknownUser_BlocksRequest() throws Exception {
        Long userId = 99L;
        when(userService.get(userId)).thenReturn(null);
        String token = jwtUtils.generateToken(userId, "ghost", 1);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Token", token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = mcpInterceptor.preHandle(request, response, new Object());

        assertFalse(allowed);
        assertNull(SessionUserHolder.getUserId());
    }

    @Test
    void preHandle_ValidToken_AllowsRequestAndSetsSessionUser() throws Exception {
        Long userId = 7L;
        UserVO userVO = new UserVO();
        userVO.setId(userId);
        userVO.setTokenVersion(1);
        when(userService.get(userId)).thenReturn(userVO);
        String token = jwtUtils.generateToken(userId, "alice", 1);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Token", token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = mcpInterceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
        assertEquals(userId, SessionUserHolder.getUserId());
    }

    @Test
    void afterCompletion_ClearsSessionUserHolder() throws Exception {
        SessionUserHolder.setUserId(123L);

        mcpInterceptor.afterCompletion(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object(), null);

        assertNull(SessionUserHolder.getUserId());
    }
}
