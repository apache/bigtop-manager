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

import org.apache.bigtop.manager.dao.annotations.CreateBy;
import org.apache.bigtop.manager.dao.annotations.CreateTime;
import org.apache.bigtop.manager.dao.annotations.UpdateBy;
import org.apache.bigtop.manager.dao.annotations.UpdateTime;
import org.apache.bigtop.manager.dao.enums.DBType;

import org.apache.ibatis.jdbc.SQL;

import org.springframework.beans.BeanUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.Column;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
                sql.INSERT_INTO(keywordsFormat(tableMetaData.getTableName(), DBType.MYSQL));
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
                        sql.VALUES(keywordsFormat(entry.getValue(), DBType.MYSQL), getTokenParam(entry.getKey()));
                    }
                }
                break;
            }
            case POSTGRESQL: {
                sql.INSERT_INTO(keywordsFormat(tableMetaData.getTableName(), DBType.POSTGRESQL));
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
                        sql.VALUES(keywordsFormat(entry.getValue(), DBType.POSTGRESQL), getTokenParam(entry.getKey()));
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

    public static <Entity> String insertList(TableMetaData tableMetaData, List<Entity> entities, String databaseId) {
        if (entities == null || entities.isEmpty()) {
            throw new IllegalArgumentException("Entities list must not be null or empty");
        }

        Class<?> entityClass = entities.get(0).getClass();
        Map<String, String> fieldColumnMap = tableMetaData.getFieldColumnMap();

        SQL sql = new SQL();
        switch (DBType.toType(databaseId)) {
            case MYSQL: {
                sql.INSERT_INTO(keywordsFormat(tableMetaData.getTableName(), DBType.MYSQL));

                boolean firstRow = true;
                int idx = 0;
                for (Entity entity : entities) {
                    List<String> values = new ArrayList<>();
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
                            if (firstRow) {
                                sql.VALUES(
                                        keywordsFormat(entry.getValue(), DBType.MYSQL),
                                        getTokenParam("arg0[" + idx + "]." + entry.getKey()));
                            }
                            values.add(getTokenParam("arg0[" + idx + "]." + entry.getKey()));
                        }
                    }
                    if (firstRow) {
                        firstRow = false;
                    } else {
                        sql.ADD_ROW();
                        sql.INTO_VALUES(values.toArray(new String[0]));
                    }
                    idx++;
                }
                break;
            }
            case POSTGRESQL: {
                sql.INSERT_INTO(keywordsFormat(tableMetaData.getTableName(), DBType.POSTGRESQL));

                boolean firstRow = true;
                List<String> columns = new ArrayList<>();
                int idx = 0;
                for (Entity entity : entities) {
                    List<String> values = new ArrayList<>();
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
                            if (firstRow) {
                                sql.VALUES(
                                        keywordsFormat(entry.getValue(), DBType.POSTGRESQL),
                                        getTokenParam("arg0[" + idx + "]." + entry.getKey()));
                            }
                            values.add(getTokenParam("arg0[" + idx + "]." + entry.getKey()));
                        }
                    }
                    if (firstRow) {
                        firstRow = false;
                    } else {
                        sql.ADD_ROW();
                        sql.INTO_VALUES(values.toArray(new String[0]));
                    }
                    idx++;
                }
                break;
            }

            default: {
                log.error("Unsupported data source");
            }
        }

        return sql.toString();
    }

    public static <Entity> String update(
            TableMetaData tableMetaData, Entity entity, String databaseId, boolean partial) {
        Class<?> entityClass = entity.getClass();
        Map<String, String> fieldColumnMap = tableMetaData.getFieldColumnMap();

        SQL sql = new SQL();
        switch (DBType.toType(databaseId)) {
            case MYSQL: {
                sql.UPDATE(keywordsFormat(tableMetaData.getTableName(), DBType.MYSQL));
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
                    if (ObjectUtils.isEmpty(value) && partial) {
                        continue;
                    }
                    Field field = ReflectionUtils.findField(entityClass, entry.getKey());
                    if (field != null) {
                        if (checkBaseField(field)) {
                            continue;
                        }
                        Column column = field.getAnnotation(Column.class);
                        if (column != null && !column.nullable() && value == null) {
                            continue;
                        }
                    }
                    sql.SET(getEquals(keywordsFormat(entry.getValue(), DBType.MYSQL), entry.getKey()));
                }

                sql.WHERE(getEquals(
                        keywordsFormat(tableMetaData.getPkColumn(), DBType.MYSQL), tableMetaData.getPkProperty()));
                break;
            }
            case POSTGRESQL: {
                sql.UPDATE(keywordsFormat(tableMetaData.getTableName(), DBType.POSTGRESQL));
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
                    if (ObjectUtils.isEmpty(value) && partial) {
                        continue;
                    }
                    Field field = ReflectionUtils.findField(entityClass, entry.getKey());
                    if (field != null) {
                        if (checkBaseField(field)) {
                            continue;
                        }
                        Column column = field.getAnnotation(Column.class);
                        if (column != null && !column.nullable() && value == null) {
                            continue;
                        }
                    }
                    sql.SET(getEquals(keywordsFormat(entry.getValue(), DBType.POSTGRESQL), entry.getKey()));
                }
                sql.WHERE(getEquals(
                        keywordsFormat(tableMetaData.getPkColumn(), DBType.POSTGRESQL), tableMetaData.getPkProperty()));
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
                sql.WHERE(getEquals(tableMetaData.getPkColumn(), tableMetaData.getPkProperty()));
                break;
            }
            case POSTGRESQL: {
                String baseColumns = tableMetaData.getBaseColumns();
                if (baseColumns.toLowerCase().contains("user.")) {
                    baseColumns = baseColumns.replace("user.", "\"user\".");
                }
                sql.SELECT(baseColumns);
                sql.FROM(keywordsFormat(tableMetaData.getTableName(), DBType.POSTGRESQL));
                sql.WHERE(tableMetaData.getPkColumn() + " = " + id);
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
                sql.SELECT(tableMetaData.getBaseColumns());
                sql.FROM(tableMetaData.getTableName());
                if (ids == null || ids.isEmpty()) {
                    sql.WHERE("1 = 0");
                    break;
                }

                StringBuilder idStr = new StringBuilder();
                for (int i = 0; i < ids.size(); i++) {
                    idStr.append(getTokenParam("arg0[" + i + "]")).append(",");
                }
                idStr.deleteCharAt(idStr.lastIndexOf(","));

                sql.WHERE(tableMetaData.getPkColumn() + " IN ( " + idStr + " )");
                break;
            }
            case POSTGRESQL: {
                String idsStr = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
                String baseColumns = tableMetaData.getBaseColumns();
                if (baseColumns.toLowerCase().contains("user.")) {
                    baseColumns = baseColumns.replace("user.", "\"user\".");
                }
                sql.SELECT(baseColumns);
                sql.FROM(keywordsFormat(tableMetaData.getTableName(), DBType.POSTGRESQL));
                sql.WHERE(tableMetaData.getPkColumn() + " in (" + idsStr + ")");
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
            case POSTGRESQL:
                String baseColumns = tableMetaData.getBaseColumns();
                if (baseColumns.toLowerCase().contains("user.")) {
                    baseColumns = baseColumns.replace("user.", "\"user\".");
                }
                sql.SELECT(baseColumns);
                sql.FROM(keywordsFormat(tableMetaData.getTableName(), DBType.POSTGRESQL));
                break;
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
                sql.WHERE(getEquals(tableMetaData.getPkColumn(), tableMetaData.getPkProperty()));
                break;
            }
            case POSTGRESQL: {
                sql.FROM(keywordsFormat(tableMetaData.getTableName(), DBType.POSTGRESQL));
                sql.WHERE(tableMetaData.getPkColumn() + " = " + id);
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
                if (ids == null || ids.isEmpty()) {
                    break;
                }
                sql.DELETE_FROM(tableMetaData.getTableName());

                StringBuilder idStr = new StringBuilder();
                for (int i = 0; i < ids.size(); i++) {
                    idStr.append(getTokenParam("arg0[" + i + "]")).append(",");
                }
                idStr.deleteCharAt(idStr.lastIndexOf(","));

                sql.WHERE(tableMetaData.getPkColumn() + " IN ( " + idStr + " )");
                break;
            }
            case POSTGRESQL: {
                String idsStr = ids.stream().map(String::valueOf).collect(Collectors.joining(", "));
                sql.DELETE_FROM(keywordsFormat(tableMetaData.getTableName(), DBType.POSTGRESQL));
                sql.WHERE(tableMetaData.getPkColumn() + " in (" + idsStr + ")");
                break;
            }
            default: {
                log.error("Unsupported data source");
            }
        }

        return sql.toString();
    }

    private static String keywordsFormat(String keyword, DBType dbType) {
        return switch (dbType) {
            case MYSQL -> "`" + keyword + "`";
            case POSTGRESQL -> "\"" + keyword + "\"";
            default -> keyword;
        };
    }

    private static String getEquals(String column, String property) {
        return column + " = " + getTokenParam(property);
    }

    private static String getTokenParam(String property) {
        return "#{" + property + "}";
    }

    private static boolean checkBaseField(Field field) {
        return field.isAnnotationPresent(CreateBy.class)
                || field.isAnnotationPresent(CreateTime.class)
                || field.isAnnotationPresent(UpdateBy.class)
                || field.isAnnotationPresent(UpdateTime.class);
    }
}
