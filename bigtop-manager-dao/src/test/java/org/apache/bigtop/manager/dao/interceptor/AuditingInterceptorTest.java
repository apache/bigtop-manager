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
package org.apache.bigtop.manager.dao.interceptor;

import org.apache.bigtop.manager.dao.annotations.CreateBy;
import org.apache.bigtop.manager.dao.annotations.CreateTime;
import org.apache.bigtop.manager.dao.annotations.UpdateBy;
import org.apache.bigtop.manager.dao.annotations.UpdateTime;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.Getter;

import java.sql.Timestamp;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuditingInterceptorTest {

    private AuditingInterceptor auditingInterceptor;

    @BeforeEach
    void setUp() {
        Supplier<Long> currentUserSupplier = () -> 1L; // Mock current user ID
        auditingInterceptor = new AuditingInterceptor(currentUserSupplier);
    }

    @Test
    void testInterceptWithInsertCommand() throws Throwable {
        // Mock MappedStatement and Invocation
        MappedStatement mappedStatement = mock(MappedStatement.class);
        when(mappedStatement.getSqlCommandType()).thenReturn(SqlCommandType.INSERT);

        TestEntity entity = new TestEntity(); // Test entity with audit fields
        Invocation invocation = mockInvocation(mappedStatement, entity);

        // Execute intercept method
        auditingInterceptor.intercept(invocation);

        // Assert audit fields are set
        assertEquals(1L, entity.getCreateBy());
        assertEquals(1L, entity.getUpdateBy());
        assertEquals(entity.getCreateTime(), entity.getUpdateTime());
    }

    @Test
    void testInterceptWithUpdateCommand() throws Throwable {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        // Mock MappedStatement and Invocation
        MappedStatement mappedStatement = mock(MappedStatement.class);
        when(mappedStatement.getSqlCommandType()).thenReturn(SqlCommandType.UPDATE);

        TestEntity entity = new TestEntity(); // Test entity with audit fields
        Invocation invocation = mockInvocation(mappedStatement, entity);

        // Execute intercept method
        auditingInterceptor.intercept(invocation);

        // Assert update fields are set
        assertEquals(1L, entity.getUpdateBy());
        assertTrue(now.getTime() < entity.getUpdateTime().getTime());
    }

    @Test
    void testInterceptWithNonInsertOrUpdate() throws Throwable {
        // Mock MappedStatement and Invocation
        MappedStatement mappedStatement = mock(MappedStatement.class);
        when(mappedStatement.getSqlCommandType()).thenReturn(SqlCommandType.DELETE);

        TestEntity entity = new TestEntity();
        Invocation invocation = mockInvocation(mappedStatement, entity);

        // Execute intercept method
        auditingInterceptor.intercept(invocation);

        // Assert no changes to entity
        assertNull(entity.getCreateBy());
        assertNull(entity.getUpdateBy());
        assertNull(entity.getCreateTime());
        assertNull(entity.getUpdateTime());
    }

    private Invocation mockInvocation(MappedStatement mappedStatement, Object parameter) throws Throwable {
        Invocation invocation = mock(Invocation.class);
        when(invocation.getArgs()).thenReturn(new Object[] {mappedStatement, parameter});
        when(invocation.proceed()).thenReturn(null);
        return invocation;
    }

    @Getter
    static class TestEntity {
        @CreateBy
        private Long createBy;

        @CreateTime
        private Timestamp createTime;

        @UpdateBy
        private Long updateBy;

        @UpdateTime
        private Timestamp updateTime;
    }
}
