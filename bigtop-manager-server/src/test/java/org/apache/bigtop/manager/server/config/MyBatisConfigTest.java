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
package org.apache.bigtop.manager.server.config;

import org.apache.ibatis.session.SqlSessionFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mybatis.spring.annotation.MapperScan;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MapperScan("org.apache.bigtop.manager.dao")
public class MyBatisConfigTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData databaseMetaData;

    @InjectMocks
    private MyBatisConfig myBatisConfig;

    @BeforeEach
    public void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
    }

    @Test
    public void testSqlSessionFactory_withMySQL() throws Exception {
        when(databaseMetaData.getDatabaseProductName()).thenReturn("MySQL");

        SqlSessionFactory sqlSessionFactory = myBatisConfig.sqlSessionFactory(dataSource);

        assertNotNull(sqlSessionFactory);
        verify(databaseMetaData, times(2)).getDatabaseProductName();
    }

    @Test
    public void testSqlSessionFactory_withPostgreSQL() throws Exception {
        when(databaseMetaData.getDatabaseProductName()).thenReturn("PostgreSQL");

        SqlSessionFactory sqlSessionFactory = myBatisConfig.sqlSessionFactory(dataSource);

        assertNotNull(sqlSessionFactory);
        verify(databaseMetaData, times(2)).getDatabaseProductName();
    }

    @Test
    public void testSqlSessionFactory_withUnsupportedDatabase() throws SQLException {
        when(databaseMetaData.getDatabaseProductName()).thenReturn("Oracle");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            myBatisConfig.sqlSessionFactory(dataSource);
        });

        assertEquals("Unsupported database: Oracle", exception.getMessage());
        verify(databaseMetaData).getDatabaseProductName();
    }

    @Test
    public void testProductNameDatabaseIdProvider_getDatabaseId_withMySQL() throws SQLException {
        when(databaseMetaData.getDatabaseProductName()).thenReturn("MySQL");

        MyBatisConfig.ProductNameDatabaseIdProvider provider = new MyBatisConfig.ProductNameDatabaseIdProvider();
        String databaseId = provider.getDatabaseId(dataSource);

        assertEquals("mysql", databaseId);
        verify(databaseMetaData).getDatabaseProductName();
    }

    @Test
    public void testProductNameDatabaseIdProvider_getDatabaseId_withPostgreSQL() throws SQLException {
        when(databaseMetaData.getDatabaseProductName()).thenReturn("PostgreSQL");

        MyBatisConfig.ProductNameDatabaseIdProvider provider = new MyBatisConfig.ProductNameDatabaseIdProvider();
        String databaseId = provider.getDatabaseId(dataSource);

        assertEquals("postgresql", databaseId);
        verify(databaseMetaData).getDatabaseProductName();
    }

    @Test
    public void testProductNameDatabaseIdProvider_getDatabaseId_withUnsupportedDatabase() throws SQLException {
        when(databaseMetaData.getDatabaseProductName()).thenReturn("Oracle");

        MyBatisConfig.ProductNameDatabaseIdProvider provider = new MyBatisConfig.ProductNameDatabaseIdProvider();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            provider.getDatabaseId(dataSource);
        });

        assertEquals("Unsupported database: Oracle", exception.getMessage());
        verify(databaseMetaData).getDatabaseProductName();
    }
}
