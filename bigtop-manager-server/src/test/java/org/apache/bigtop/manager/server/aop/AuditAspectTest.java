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
package org.apache.bigtop.manager.server.aop;

import org.apache.bigtop.manager.dao.mapper.AuditLogMapper;
import org.apache.bigtop.manager.dao.po.AuditLogPO;
import org.apache.bigtop.manager.server.holder.SessionUserHolder;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditAspectTest {

    @Mock
    private AuditLogMapper auditLogMapper;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ServletRequestAttributes attributes;

    @InjectMocks
    private AuditAspect auditAspect;

    @BeforeEach
    void setUp() {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
    }

    @AfterEach
    void tearDown() {
        SessionUserHolder.setUserId(null);
        RequestContextHolder.setRequestAttributes(null);
    }

    @Test
    void before_ValidRequest_SavesAuditLog() {
        when(request.getRequestURI()).thenReturn("/test/uri");
        when(joinPoint.getThis()).thenReturn(this);
        when(attributes.getRequest()).thenReturn(request);
        when(methodSignature.getName()).thenReturn("testMethod");
        when(methodSignature.getMethod()).thenReturn(this.getClass().getDeclaredMethods()[0]);
        SessionUserHolder.setUserId(1L);
        RequestContextHolder.setRequestAttributes(attributes);

        auditAspect.before(joinPoint);

        verify(auditLogMapper, times(1)).save(any(AuditLogPO.class));
    }

    @Test
    void before_NullRequestAttributes_DoesNotSaveAuditLog() {
        SessionUserHolder.setUserId(1L);

        auditAspect.before(joinPoint);

        verify(auditLogMapper, never()).save(any(AuditLogPO.class));
    }

    @Test
    void before_NullUserId_DoesNotSaveAuditLog() {
        RequestContextHolder.setRequestAttributes(attributes);

        auditAspect.before(joinPoint);

        verify(auditLogMapper, never()).save(any(AuditLogPO.class));
    }
}
