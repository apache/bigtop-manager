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

import org.apache.bigtop.manager.common.utils.ClassUtils;
import org.apache.bigtop.manager.dao.annotations.QueryCondition;
import org.apache.bigtop.manager.dao.enums.DBType;

import org.apache.ibatis.jdbc.SQL;

import org.springframework.beans.BeanUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.MessageFormat;
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
            case POSTGRESQL: {
                sql.INSERT_INTO("\"" + tableMetaData.getTableName() + "\"");
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
                        sql.VALUES("\"" + entry.getValue() + "\"", getTokenParam(entry.getKey()));
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
            case POSTGRESQL: {
                sql.UPDATE("\"" + tableMetaData.getTableName() + "\"");
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
                        sql.SET("\"" + getEquals(entry.getValue() + "\"", entry.getKey()));
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
            case POSTGRESQL: {
                String baseColumns = tableMetaData.getBaseColumns();
                if (baseColumns.toLowerCase().contains("user.")) {
                    baseColumns = baseColumns.replace("user.", "\"user\".");
                }
                sql.SELECT(baseColumns);
                sql.FROM("\"" + tableMetaData.getTableName() + "\"");
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
                String idsStr = ids.stream().map(String::valueOf).collect(Collectors.joining("', '"));
                sql.SELECT(tableMetaData.getBaseColumns());
                sql.FROM(tableMetaData.getTableName());
                sql.WHERE(tableMetaData.getPkColumn() + " in ('" + idsStr + "')");
                break;
            }
            case POSTGRESQL: {
                String idsStr = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
                String baseColumns = tableMetaData.getBaseColumns();
                if (baseColumns.toLowerCase().contains("user.")) {
                    baseColumns = baseColumns.replace("user.", "\"user\".");
                }
                sql.SELECT(baseColumns);
                sql.FROM("\"" + tableMetaData.getTableName() + "\"");
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
                sql.FROM("\"" + tableMetaData.getTableName() + "\"");
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
                sql.WHERE(tableMetaData.getPkColumn() + " = '" + id + "'");
                break;
            }
            case POSTGRESQL: {
                sql.FROM("\"" + tableMetaData.getTableName() + "\"");
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
                String idsStr = ids.stream().map(String::valueOf).collect(Collectors.joining("', '"));
                sql.DELETE_FROM(tableMetaData.getTableName());
                sql.WHERE(tableMetaData.getPkColumn() + " in ('" + idsStr + "')");
                break;
            }
            case POSTGRESQL: {
                String idsStr = ids.stream().map(String::valueOf).collect(Collectors.joining(", "));
                sql.DELETE_FROM("\"" + tableMetaData.getTableName() + "\"");
                sql.WHERE(tableMetaData.getPkColumn() + " in (" + idsStr + ")");
                break;
            }
            default: {
                log.error("Unsupported data source");
            }
        }

        return sql.toString();
    }

    public static <Condition> String findByCondition(
            TableMetaData tableMetaData, String databaseId, Condition condition) throws IllegalAccessException {
        String tableName = tableMetaData.getTableName();
        log.info("databaseId: {}", databaseId);
        SQL sql = new SQL();
        switch (DBType.toType(databaseId)) {
            case POSTGRESQL:
                tableName = "\"" + tableName + "\"";
            case MYSQL: {
                sql = mysqlCondition(condition, tableName);
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

    private static <Condition> SQL mysqlCondition(Condition condition, String tableName) throws IllegalAccessException {

        Class<?> loadClass;
        try {
            loadClass = condition.getClass();
        } catch (Exception e) {
            throw new RuntimeException("Get class Error!!!");
        }

        List<Field> fieldList = ClassUtils.getFields(loadClass);
        /* Prepare SQL */
        SQL sql = new SQL();
        sql.SELECT("*");
        sql.FROM(tableName);
        for (Field field : fieldList) {
            field.setAccessible(true);
            String fieldName = field.getName();
            log.debug("[requestField] {}, [requestValue] {}", fieldName, field.get(condition));
            if (field.isAnnotationPresent(QueryCondition.class) && Objects.nonNull(field.get(condition))) {
                QueryCondition annotation = field.getAnnotation(QueryCondition.class);

                String queryKey = fieldName;
                if (!annotation.queryKey().isEmpty()) {
                    queryKey = annotation.queryKey();
                }

                log.info(
                        "[queryKey] {}, [queryType] {}, [queryValue] {}",
                        queryKey,
                        annotation.queryType().toString(),
                        field.get(condition));

                Object value = field.get(condition);
                if (value != null) {
                    switch (annotation.queryType()) {
                        case EQ:
                            sql.WHERE(MessageFormat.format("{0} = ''{1}''", queryKey, value));
                            break;
                        case NOT_EQ:
                            sql.WHERE(MessageFormat.format("{0} != ''{1}''", queryKey, value));
                            break;
                        case IN:
                            sql.WHERE(MessageFormat.format(
                                    "{0} IN (''{1}'')",
                                    queryKey,
                                    String.join("','", value.toString().split(annotation.multipleDelimiter()))));
                            break;
                        case NOT_IN:
                            sql.WHERE(MessageFormat.format(
                                    "{0} NOT IN (''{1}'')",
                                    queryKey,
                                    String.join("','", value.toString().split(annotation.multipleDelimiter()))));
                            break;
                        case GT:
                            sql.WHERE(MessageFormat.format("{0} > ''{1}''", queryKey, value));
                            break;
                        case GTE:
                            sql.WHERE(MessageFormat.format("{0} >= ''{1}''", queryKey, value));
                            break;
                        case LT:
                            sql.WHERE(MessageFormat.format("{0} < ''{1}''", queryKey, value));
                            break;
                        case LTE:
                            sql.WHERE(MessageFormat.format("{0} <= ''{1}''", queryKey, value));
                            break;
                        case BETWEEN:
                            String[] valueArr = field.get(condition).toString().split(annotation.pairDelimiter());
                            if (valueArr.length == 2) {
                                sql.WHERE(MessageFormat.format(
                                        "{0} BETWEEN ''{1}'' AND ''{2}''", queryKey, valueArr[0], valueArr[1]));
                            }
                            break;
                        case PREFIX_LIKE:
                            sql.WHERE(MessageFormat.format("{0} LIKE CONCAT(''{1}'', ''%'')", queryKey, value));
                            break;
                        case SUFFIX_LIKE:
                            sql.WHERE(MessageFormat.format("{0} LIKE CONCAT(''%'', ''{1}'')", queryKey, value));
                            break;
                        case LIKE:
                            sql.WHERE(MessageFormat.format("{0} LIKE CONCAT(''%'', ''{1}'', ''%'')", queryKey, value));
                            break;
                        case NOT_LIKE:
                            sql.WHERE(MessageFormat.format(
                                    "{0} NOT LIKE CONCAT(''%'', ''{1}'', ''%'')", queryKey, value));
                            break;
                        case NULL:
                            sql.WHERE(queryKey + " IS NULL");
                            break;
                        case NOT_NULL:
                            sql.WHERE(queryKey + " IS NOT NULL");
                            break;
                        default:
                            log.warn("Unknown query type: {}", annotation.queryType());
                    }
                }
            }
        }

        return sql;
    }
}
