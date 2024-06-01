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

import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.entity.AuditLog;
import org.apache.bigtop.manager.dao.repository.AuditLogRepository;
import org.apache.bigtop.manager.server.holder.SessionUserHolder;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Configuration
public class AuditAspect {

    private static final String POINT_CUT = "@annotation(org.apache.bigtop.manager.server.annotations.Audit)";

    @Resource
    private AuditLogRepository auditLogRepository;

    @Before(value = POINT_CUT)
    public void before(JoinPoint joinPoint) {
        MethodSignature ms = (MethodSignature) joinPoint.getSignature();
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(SessionUserHolder.getUserId());

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            // obtain request uri
            HttpServletRequest request = attributes.getRequest();
            String uri = request.getRequestURI();
            // obtain controller name
            Class<?> controller = joinPoint.getThis().getClass();
            Tag annotation = controller.getAnnotation(Tag.class);
            String apiName = "";
            String apiDesc = "";
            if (annotation != null) {
                apiName = annotation.name();
                apiDesc = annotation.description();
            }
            // obtain method name
            String methodName = ms.getName();
            // obtain method desc
            String operationSummary = "";
            String operationDesc = "";
            Operation operation = ms.getMethod().getDeclaredAnnotation(Operation.class);
            if (operation != null) {
                operationSummary = operation.summary();
                operationDesc = operation.description();
            }

            auditLog.setUri(uri);
            auditLog.setTagName(apiName);
            auditLog.setTagDesc(apiDesc);
            auditLog.setOperationSummary(operationSummary);
            auditLog.setOperationDesc(operationDesc);
            auditLog.setArgs(JsonUtils.writeAsString(joinPoint.getArgs()));

            log.debug("auditLog: {}", auditLog);
            log.debug("request method：{}.{}", joinPoint.getSignature().getDeclaringTypeName(), methodName);

            auditLogRepository.save(auditLog);
        }
    }

}
