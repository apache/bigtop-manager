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
import org.apache.bigtop.manager.dao.annotations.CreateBy;
import org.apache.bigtop.manager.dao.annotations.CreateTime;
import org.apache.bigtop.manager.dao.annotations.QueryCondition;
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
import java.util.LinkedHashMap;
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

    public static <Entity> String updateList(
            TableMetaData tableMetaData, List<Entity> entities, String databaseId, boolean partial) {
        if (entities == null || entities.isEmpty()) {
            throw new IllegalArgumentException("Entities list must not be null or empty");
        }

        Class<?> entityClass = entities.get(0).getClass();
        Map<String, String> fieldColumnMap = tableMetaData.getFieldColumnMap();

        StringBuilder sqlBuilder = new StringBuilder();
        switch (DBType.toType(databaseId)) {
            case MYSQL: {
                sqlBuilder
                        .append("UPDATE ")
                        .append(keywordsFormat(tableMetaData.getTableName(), DBType.MYSQL))
                        .append(" SET ");
                Map<String, StringBuilder> setClauses = new LinkedHashMap<>();
                String primaryKey = keywordsFormat("id", DBType.MYSQL);
                for (Map.Entry<String, String> entry : fieldColumnMap.entrySet()) {
                    // Ignore primary key
                    if (Objects.equals(entry.getKey(), tableMetaData.getPkProperty())) {
                        primaryKey = keywordsFormat(entry.getValue(), DBType.MYSQL);
                        continue;
                    }
                    Field field = ReflectionUtils.findField(entityClass, entry.getKey());
                    if (field == null || checkBaseField(field)) {
                        continue;
                    }
                    StringBuilder caseClause = new StringBuilder();
                    caseClause
                            .append(keywordsFormat(entry.getValue(), DBType.MYSQL))
                            .append(" = CASE ");
                    for (Entity entity : entities) {
                        PropertyDescriptor ps = BeanUtils.getPropertyDescriptor(entityClass, entry.getKey());
                        if (ps == null || ps.getReadMethod() == null) {
                            continue;
                        }
                        Object value = ReflectionUtils.invokeMethod(ps.getReadMethod(), entity);
                        PropertyDescriptor pkPs =
                                BeanUtils.getPropertyDescriptor(entityClass, tableMetaData.getPkProperty());
                        if (pkPs == null || pkPs.getReadMethod() == null) {
                            continue;
                        }
                        Object pkValue = ReflectionUtils.invokeMethod(pkPs.getReadMethod(), entity);

                        if (!ObjectUtils.isEmpty(value)) {
                            caseClause
                                    .append("WHEN ")
                                    .append(primaryKey)
                                    .append(" = '")
                                    .append(pkValue)
                                    .append("' THEN '")
                                    .append(escapeSingleQuote(value.toString()))
                                    .append("' ");
                        } else if (!partial) {
                            Column column = field.getAnnotation(Column.class);
                            if (column != null && !column.nullable() && value == null) {
                                continue;
                            }
                            caseClause
                                    .append("WHEN ")
                                    .append(primaryKey)
                                    .append(" = '")
                                    .append(pkValue)
                                    .append("' THEN NULL ");
                        }
                    }
                    if (caseClause.toString().endsWith("CASE ")) {
                        caseClause.append("WHEN TRUE THEN ");
                    } else {
                        caseClause.append("ELSE ");
                    }
                    caseClause
                            .append(keywordsFormat(entry.getValue(), DBType.MYSQL))
                            .append(" ");
                    caseClause.append("END");
                    setClauses.put(entry.getValue(), caseClause);
                }
                sqlBuilder.append(String.join(", ", setClauses.values()));

                sqlBuilder.append(" WHERE ").append(primaryKey).append(" IN (");
                String pkValues = entities.stream()
                        .map(entity -> {
                            PropertyDescriptor pkPs =
                                    BeanUtils.getPropertyDescriptor(entityClass, tableMetaData.getPkProperty());
                            Object pkValue = ReflectionUtils.invokeMethod(pkPs.getReadMethod(), entity);
                            return "'" + pkValue.toString() + "'";
                        })
                        .collect(Collectors.joining(", "));

                sqlBuilder.append(pkValues).append(")");
                break;
            }
            case POSTGRESQL: {
                sqlBuilder
                        .append("UPDATE ")
                        .append(keywordsFormat(tableMetaData.getTableName(), DBType.POSTGRESQL))
                        .append(" SET ");
                Map<String, StringBuilder> setClauses = new LinkedHashMap<>();
                String primaryKey = keywordsFormat("id", DBType.POSTGRESQL);
                for (Map.Entry<String, String> entry : fieldColumnMap.entrySet()) {
                    // Ignore primary key
                    if (Objects.equals(entry.getKey(), tableMetaData.getPkProperty())) {
                        primaryKey = keywordsFormat(entry.getValue(), DBType.POSTGRESQL);
                        continue;
                    }
                    Field field = ReflectionUtils.findField(entityClass, entry.getKey());
                    if (field == null || checkBaseField(field)) {
                        continue;
                    }
                    StringBuilder caseClause = new StringBuilder();
                    caseClause
                            .append(keywordsFormat(entry.getValue(), DBType.POSTGRESQL))
                            .append(" = CASE ");

                    for (Entity entity : entities) {
                        PropertyDescriptor ps = BeanUtils.getPropertyDescriptor(entityClass, entry.getKey());
                        if (ps == null || ps.getReadMethod() == null) {
                            continue;
                        }
                        Object value = ReflectionUtils.invokeMethod(ps.getReadMethod(), entity);
                        PropertyDescriptor pkPs =
                                BeanUtils.getPropertyDescriptor(entityClass, tableMetaData.getPkProperty());
                        if (pkPs == null || pkPs.getReadMethod() == null) {
                            continue;
                        }
                        Object pkValue = ReflectionUtils.invokeMethod(pkPs.getReadMethod(), entity);

                        if (!ObjectUtils.isEmpty(value)) {
                            caseClause
                                    .append("WHEN ")
                                    .append(primaryKey)
                                    .append(" = '")
                                    .append(pkValue)
                                    .append("' THEN '")
                                    .append(escapeSingleQuote(value.toString()))
                                    .append("' ");
                        } else if (!partial) {
                            Column column = field.getAnnotation(Column.class);
                            if (column != null && !column.nullable() && value == null) {
                                continue;
                            }
                            caseClause
                                    .append("WHEN ")
                                    .append(primaryKey)
                                    .append(" = '")
                                    .append(pkValue)
                                    .append("' THEN NULL ");
                        }
                    }
                    if (caseClause.toString().endsWith("CASE ")) {
                        caseClause.append("WHEN TRUE THEN ");
                    } else {
                        caseClause.append("ELSE ");
                    }
                    caseClause
                            .append(keywordsFormat(entry.getValue(), DBType.POSTGRESQL))
                            .append(" ");
                    caseClause.append("END");
                    setClauses.put(entry.getValue(), caseClause);
                }
                sqlBuilder.append(String.join(", ", setClauses.values()));

                sqlBuilder.append(" WHERE ").append(primaryKey).append(" IN (");
                String pkValues = entities.stream()
                        .map(entity -> {
                            PropertyDescriptor pkPs =
                                    BeanUtils.getPropertyDescriptor(entityClass, tableMetaData.getPkProperty());
                            Object pkValue = ReflectionUtils.invokeMethod(pkPs.getReadMethod(), entity);
                            return "'" + pkValue.toString() + "'";
                        })
                        .collect(Collectors.joining(", "));

                sqlBuilder.append(pkValues).append(")");
                break;
            }
            default: {
                log.error("Unsupported data source");
            }
        }

        return sqlBuilder.toString();
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

    public static <Condition> String findByCondition(
            TableMetaData tableMetaData, String databaseId, Condition condition) throws IllegalAccessException {
        String tableName = tableMetaData.getTableName();
        log.info("databaseId: {}", databaseId);
        SQL sql = new SQL();
        switch (DBType.toType(databaseId)) {
            case POSTGRESQL:
                tableName = "\"" + tableName + "\"";
            case MYSQL: {
                sql = mysqlCondition(condition, tableMetaData);
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

    private static String escapeSingleQuote(String input) {
        if (input != null) {
            return input.replace("'", "''");
        }
        return null;
    }

    private static boolean checkBaseField(Field field) {
        return field.isAnnotationPresent(CreateBy.class)
                || field.isAnnotationPresent(CreateTime.class)
                || field.isAnnotationPresent(UpdateBy.class)
                || field.isAnnotationPresent(UpdateTime.class);
    }

    private static <Condition> SQL mysqlCondition(Condition condition, TableMetaData tableMetaData)
            throws IllegalAccessException {

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
        sql.FROM(tableMetaData.getTableName());
        for (Field field : fieldList) {
            field.setAccessible(true);
            String fieldName = field.getName();
            log.debug("[requestField] {}, [requestValue] {}", fieldName, field.get(condition));
            if (field.isAnnotationPresent(QueryCondition.class) && Objects.nonNull(field.get(condition))) {
                QueryCondition annotation = field.getAnnotation(QueryCondition.class);

                String property = fieldName;
                if (!annotation.queryKey().isEmpty()) {
                    property = annotation.queryKey();
                }

                Object value = field.get(condition);
                Map<String, String> fieldColumnMap = tableMetaData.getFieldColumnMap();

                if (value != null && fieldColumnMap.containsKey(property)) {
                    String columnName = fieldColumnMap.get(property);

                    log.info(
                            "[queryKey] {}, [queryType] {}, [queryValue] {}",
                            property,
                            annotation.queryType().toString(),
                            field.get(condition));
                    switch (annotation.queryType()) {
                        case EQ:
                            sql.WHERE(getEquals(columnName, fieldName));
                            break;
                        case NOT_EQ:
                            sql.WHERE(columnName + " != " + getTokenParam(fieldName));
                            break;
                        case IN:
                            sql.WHERE(columnName + " IN ( REPLACE( " + getTokenParam(fieldName) + ", '"
                                    + annotation.multipleDelimiter() + "', ',') )");
                            break;
                        case NOT_IN:
                            sql.WHERE(columnName + " NOT IN ( REPLACE( " + getTokenParam(fieldName) + ", '"
                                    + annotation.multipleDelimiter() + "', ',') )");
                            break;
                        case GT:
                            sql.WHERE(columnName + " > " + getTokenParam(fieldName));
                            break;
                        case GTE:
                            sql.WHERE(columnName + " >= " + getTokenParam(fieldName));
                            break;
                        case LT:
                            sql.WHERE(columnName + " < " + getTokenParam(fieldName));
                            break;
                        case LTE:
                            sql.WHERE(columnName + " <= " + getTokenParam(fieldName));
                            break;
                        case BETWEEN:
                            sql.WHERE(columnName + " BETWEEN SUBSTRING_INDEX( " + getTokenParam(fieldName) + ", '"
                                    + annotation.pairDelimiter() + "', 1) AND SUBSTRING_INDEX( "
                                    + getTokenParam(fieldName) + ", '"
                                    + annotation.pairDelimiter() + "', 2)");
                            break;
                        case PREFIX_LIKE:
                            sql.WHERE(columnName + " LIKE CONCAT( " + getTokenParam(fieldName) + ", '%')");
                            break;
                        case SUFFIX_LIKE:
                            sql.WHERE(columnName + " LIKE CONCAT('%', " + getTokenParam(fieldName) + ")");
                            break;
                        case LIKE:
                            sql.WHERE(columnName + " LIKE CONCAT('%', " + getTokenParam(fieldName) + ", '%')");
                            break;
                        case NOT_LIKE:
                            sql.WHERE(columnName + " NOT LIKE CONCAT('%', " + getTokenParam(fieldName) + ", '%')");
                            break;
                        case NULL:
                            sql.WHERE(columnName + " IS NULL");
                            break;
                        case NOT_NULL:
                            sql.WHERE(columnName + " IS NOT NULL");
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
