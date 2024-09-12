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

import org.apache.bigtop.manager.dao.interceptor.AuditingInterceptor;
import org.apache.bigtop.manager.server.holder.SessionUserHolder;

import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.session.SqlSessionFactory;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
@MapperScan("org.apache.bigtop.manager.dao")
public class MyBatisConfig {

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources(String.format("classpath*:mapper/%s/**/*Mapper.xml", new ProductNameDatabaseIdProvider().getDatabaseId(dataSource))));

        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setUseGeneratedKeys(true);
        configuration.addInterceptor(new AuditingInterceptor(SessionUserHolder::getUserId));

        sessionFactory.setDatabaseIdProvider(new ProductNameDatabaseIdProvider());
        sessionFactory.setConfiguration(configuration);
        return sessionFactory.getObject();
    }

    static class ProductNameDatabaseIdProvider implements DatabaseIdProvider {

        @Override
        public void setProperties(Properties p) {
            // No properties to set
        }

        @Override
        public String getDatabaseId(DataSource dataSource) throws SQLException {
            try (Connection connection = dataSource.getConnection()) {
                String databaseProductName = connection.getMetaData().getDatabaseProductName();
                return switch (databaseProductName) {
                    case "MySQL" -> "mysql";
                    case "PostgreSQL" -> "postgresql";
                    default -> throw new IllegalArgumentException("Unsupported database: " + databaseProductName);
                };
            }
        }
    }
}
