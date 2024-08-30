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

import org.apache.bigtop.manager.dao.sql.TableMetaData;

import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

@Slf4j
@Component
@Intercepts({
    @Signature(
            type = Executor.class,
            method = "update",
            args = {MappedStatement.class, Object.class})
})
public class DynamicKeyInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();

        String[] keyColumns = mappedStatement.getKeyColumns();
        String[] keyProperties = mappedStatement.getKeyProperties();

        if (mappedStatement.getResource().contains(".xml") && keyColumns != null && keyProperties != null) {
            return invocation.proceed();
        }

        if (SqlCommandType.INSERT == sqlCommandType) {
            Object parameter = invocation.getArgs()[1];
            log.debug("parameter: {}", parameter);

            Object object = parameter;
            if (parameter instanceof MapperMethod.ParamMap<?> paramMap) {
                if (paramMap.get("param1") instanceof Collection) {
                    object = ((Collection<?>) paramMap.get("param1"))
                            .stream().findFirst().get();
                } else {
                    object = paramMap.get("param1");
                }
            }
            TableMetaData metaData = TableMetaData.forClass(object.getClass());

            String pkColumn = metaData.getPkColumn();
            String pkProperty = metaData.getPkProperty();
            log.debug("pkColumn  {} pkProperty {}", pkColumn, pkProperty);
            Method delimitedStringToArray =
                    MappedStatement.class.getDeclaredMethod("delimitedStringToArray", String.class);
            delimitedStringToArray.setAccessible(true);
            // keyColumns
            Field field1 = MappedStatement.class.getDeclaredField("keyColumns");
            field1.setAccessible(true);
            field1.set(mappedStatement, delimitedStringToArray.invoke(mappedStatement, pkProperty));
            // keyProperties
            Field field2 = MappedStatement.class.getDeclaredField("keyProperties");
            field2.setAccessible(true);
            field2.set(mappedStatement, delimitedStringToArray.invoke(mappedStatement, pkColumn));
            log.debug(
                    "mappedStatement keyColumns: {} keyProperties: {} keyGenerator: {}",
                    mappedStatement.getKeyColumns(),
                    mappedStatement.getKeyProperties(),
                    mappedStatement.getKeyGenerator());
        }

        return invocation.proceed();
    }
}
