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

import org.apache.bigtop.manager.dao.po.AuditLogPO;
import org.apache.bigtop.manager.dao.repository.AuditLogDao;
import org.apache.bigtop.manager.server.holder.SessionUserHolder;

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

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Aspect
@Configuration
public class AuditAspect {

    @Resource
    private AuditLogDao auditLogDao;

    @Before(value = "@annotation(org.apache.bigtop.manager.server.annotations.Audit)")
    public void before(JoinPoint joinPoint) {
        MethodSignature ms = (MethodSignature) joinPoint.getSignature();
        Long userId = SessionUserHolder.getUserId();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null && userId != null) {
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

            AuditLogPO auditLogPO = new AuditLogPO();
            auditLogPO.setUserId(userId);
            auditLogPO.setUri(uri);
            auditLogPO.setTagName(apiName);
            auditLogPO.setTagDesc(apiDesc);
            auditLogPO.setOperationSummary(operationSummary);
            auditLogPO.setOperationDesc(operationDesc);

            // Temporary disable since params of command api sometimes are too long
            // auditLogPO.setArgs(JsonUtils.writeAsString(joinPoint.getArgs()));

            log.debug("auditLog: {}", auditLogPO);
            log.debug("request method：{}.{}", joinPoint.getSignature().getDeclaringTypeName(), methodName);

            auditLogDao.save(auditLogPO);
        }
    }
}
