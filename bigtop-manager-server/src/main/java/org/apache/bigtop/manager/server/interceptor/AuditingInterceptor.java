/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.bigtop.manager.server.interceptor;

import org.apache.bigtop.manager.common.utils.ClassUtils;
import org.apache.bigtop.manager.dao.annotations.CreateBy;
import org.apache.bigtop.manager.dao.annotations.CreateTime;
import org.apache.bigtop.manager.dao.annotations.UpdateBy;
import org.apache.bigtop.manager.dao.annotations.UpdateTime;
import org.apache.bigtop.manager.server.holder.SessionUserHolder;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
@Intercepts({
    @Signature(
            type = Executor.class,
            method = "update",
            args = {MappedStatement.class, Object.class})
})
public class AuditingInterceptor implements Interceptor {

    @SuppressWarnings("unchecked")
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();

        // Get Entity Object
        Object parameter = invocation.getArgs()[1];
        log.debug("sqlCommandType {}", sqlCommandType);

        Collection<Object> objects;
        if (parameter instanceof MapperMethod.ParamMap) {
            MapperMethod.ParamMap<Object> paramMap = ((MapperMethod.ParamMap<Object>) parameter);
            if (paramMap.get("param1") instanceof Collection) {
                objects = ((Collection<Object>) paramMap.get("param1"));
            } else {
                objects = Collections.singletonList(paramMap.get("param1"));
            }
        } else {
            objects = Collections.singletonList(parameter);
        }

        for (Object o : objects) {
            setAuditFields(o, sqlCommandType);
        }

        return invocation.proceed();
    }

    private Pair<Long, Timestamp> getAuditInfo() {
        // Get the current time and operator
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Long currentUser = SessionUserHolder.getUserId();
        log.debug("timestamp: {} currentUser: {}", timestamp, currentUser);
        return Pair.of(currentUser, timestamp);
    }

    private void setAuditFields(Object object, SqlCommandType sqlCommandType) throws IllegalAccessException {
        Pair<Long, Timestamp> auditInfo = getAuditInfo();
        Long currentUser = auditInfo.getLeft();
        Timestamp timestamp = auditInfo.getRight();

        List<Field> fields = ClassUtils.getFields(object.getClass());
        if (SqlCommandType.INSERT == sqlCommandType || SqlCommandType.UPDATE == sqlCommandType) {
            for (Field field : fields) {
                boolean accessible = field.canAccess(object);
                field.setAccessible(true);
                if (field.isAnnotationPresent(CreateBy.class)
                        && SqlCommandType.INSERT == sqlCommandType
                        && currentUser != null) {
                    field.set(object, currentUser);
                }
                if (field.isAnnotationPresent(CreateTime.class) && SqlCommandType.INSERT == sqlCommandType) {
                    field.set(object, timestamp);
                }
                if (field.isAnnotationPresent(UpdateBy.class) && currentUser != null) {
                    field.set(object, currentUser);
                }
                if (field.isAnnotationPresent(UpdateTime.class)) {
                    field.set(object, timestamp);
                }
                field.setAccessible(accessible);
            }
        }
    }
}
