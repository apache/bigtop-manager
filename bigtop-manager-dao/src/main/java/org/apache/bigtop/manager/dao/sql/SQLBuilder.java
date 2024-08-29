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

package org.apache.bigtop.manager.dao.sql;

import org.apache.ibatis.jdbc.SQL;

import org.springframework.beans.BeanUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Multiple data source support
 */
@Slf4j
public class SQLBuilder {

    public static <Entity> String insert(TableMetaData tableMetaData, Entity entity, String databaseId) {
        Class<?> entityClass = entity.getClass();
        Map<String, String> fieldColumnMap = tableMetaData.getFieldColumnMap();

        SQL sql = new SQL();
        switch (DBType.toType(databaseId)) {
            case MYSQL: {
                sql.INSERT_INTO(tableMetaData.getTableName());
                for (Map.Entry<String, String> entry : fieldColumnMap.entrySet()) {
                    // Ignore primary key
                    if (Objects.equals(entry.getKey(), tableMetaData.getPkProperty())) {
                        continue;
                    }
                    PropertyDescriptor ps = BeanUtils.getPropertyDescriptor(entityClass, entry.getKey());
                    if (ps == null || ps.getReadMethod() == null) {
                        continue;
                    }
                    Object value = ReflectionUtils.invokeMethod(ps.getReadMethod(), entity);
                    if (!ObjectUtils.isEmpty(value)) {
                        sql.VALUES("`" + entry.getValue() + "`", getTokenParam(entry.getKey()));
                    }
                }
                break;
            }
            default: {
                log.error("Unsupported data source");
            }
        }

        return sql.toString();
    }

    public static <Entity> String update(TableMetaData tableMetaData, Entity entity, String databaseId) {
        Class<?> entityClass = entity.getClass();
        Map<String, String> fieldColumnMap = tableMetaData.getFieldColumnMap();

        SQL sql = new SQL();
        switch (DBType.toType(databaseId)) {
            case MYSQL: {
                sql.UPDATE(tableMetaData.getTableName());
                for (Map.Entry<String, String> entry : fieldColumnMap.entrySet()) {
                    // Ignore primary key
                    if (Objects.equals(entry.getKey(), tableMetaData.getPkProperty())) {
                        continue;
                    }
                    PropertyDescriptor ps = BeanUtils.getPropertyDescriptor(entityClass, entry.getKey());
                    if (ps == null || ps.getReadMethod() == null) {
                        continue;
                    }
                    Object value = ReflectionUtils.invokeMethod(ps.getReadMethod(), entity);
                    if (!ObjectUtils.isEmpty(value)) {
                        sql.SET("`" + getEquals(entry.getValue() + "`", entry.getKey()));
                    }
                }

                sql.WHERE(getEquals(tableMetaData.getPkColumn(), tableMetaData.getPkProperty()));
                break;
            }
            default: {
                log.error("Unsupported data source");
            }
        }

        return sql.toString();
    }

    public static String selectById(TableMetaData tableMetaData, String databaseId, Serializable id) {

        SQL sql = new SQL();
        switch (DBType.toType(databaseId)) {
            case MYSQL: {
                sql.SELECT(tableMetaData.getBaseColumns());
                sql.FROM(tableMetaData.getTableName());
                sql.WHERE(tableMetaData.getPkColumn() + " = '" + id + "'");
                break;
            }
            default: {
                log.error("Unsupported data source");
            }
        }

        return sql.toString();
    }

    public static String selectByIds(
            TableMetaData tableMetaData, String databaseId, Collection<? extends Serializable> ids) {

        SQL sql = new SQL();
        switch (DBType.toType(databaseId)) {
            case MYSQL: {
                String idsStr = ids.stream().map(String::valueOf).collect(Collectors.joining("', '"));
                sql.SELECT(tableMetaData.getBaseColumns());
                sql.FROM(tableMetaData.getTableName());
                sql.WHERE(tableMetaData.getPkColumn() + " in ('" + idsStr + "')");
                break;
            }
            default: {
                log.error("Unsupported data source");
            }
        }

        return sql.toString();
    }

    public static String selectAll(TableMetaData tableMetaData, String databaseId) {

        SQL sql = new SQL();
        switch (DBType.toType(databaseId)) {
            case MYSQL: {
                sql.SELECT(tableMetaData.getBaseColumns());
                sql.FROM(tableMetaData.getTableName());
                break;
            }
            default: {
                log.error("Unsupported data source");
            }
        }

        return sql.toString();
    }

    public static String deleteById(TableMetaData tableMetaData, String databaseId, Serializable id) {
        SQL sql = new SQL();
        switch (DBType.toType(databaseId)) {
            case MYSQL: {
                sql.DELETE_FROM(tableMetaData.getTableName());
                sql.WHERE(tableMetaData.getPkColumn() + " = '" + id + "'");
                break;
            }
            default: {
                log.error("Unsupported data source");
            }
        }

        return sql.toString();
    }

    public static String deleteByIds(
            TableMetaData tableMetaData, String databaseId, Collection<? extends Serializable> ids) {
        SQL sql = new SQL();
        switch (DBType.toType(databaseId)) {
            case MYSQL: {
                String idsStr = ids.stream().map(String::valueOf).collect(Collectors.joining("', '"));
                sql.DELETE_FROM(tableMetaData.getTableName());
                sql.WHERE(tableMetaData.getPkColumn() + " in ('" + idsStr + "')");
                break;
            }
            default: {
                log.error("Unsupported data source");
            }
        }

        return sql.toString();
    }

    private static String getEquals(String column, String property) {
        return column + " = " + getTokenParam(property);
    }

    private static String getTokenParam(String property) {
        return "#{" + property + "}";
    }
}
