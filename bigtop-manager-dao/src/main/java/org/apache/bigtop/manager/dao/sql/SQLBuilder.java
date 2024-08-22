package org.apache.bigtop.manager.dao.sql;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Multiple data source support
 */
@Slf4j
public class SQLBuilder {

    public static <Entity> String insert(TableMataData mataData, Entity entity, String databaseId) {
        Class<?> entityClass = entity.getClass();
        Map<String, String> fieldColumnMap = mataData.getFieldColumnMap();

        SQL sql = new SQL();
        switch (databaseId) {
            case "mysql": {
                sql.INSERT_INTO(mataData.getTableName());
                for (Map.Entry<String, String> entry : fieldColumnMap.entrySet()) {
                    // 忽略主键
                    if (Objects.equals(entry.getKey(), mataData.getPkProperty())) {
                        continue;
                    }
                    PropertyDescriptor ps = BeanUtils.getPropertyDescriptor(entityClass, entry.getKey());
                    if (ps == null || ps.getReadMethod() == null) {
                        continue;
                    }
                    Object value = ReflectionUtils.invokeMethod(ps.getReadMethod(), entity);
                    if (!ObjectUtils.isEmpty(value)) {
                        sql.VALUES(entry.getValue(), getTokenParam(entry.getKey()));
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

    public static <Entity> String update(TableMataData mataData, Entity entity, String databaseId) {
        Class<?> entityClass = entity.getClass();
        Map<String, String> fieldColumnMap = mataData.getFieldColumnMap();

        SQL sql = new SQL();
        switch (databaseId) {
            case "mysql": {
                sql.UPDATE(mataData.getTableName());
                for (Map.Entry<String, String> entry : fieldColumnMap.entrySet()) {
                    // 忽略主键
                    if (Objects.equals(entry.getKey(), mataData.getPkProperty())) {
                        continue;
                    }
                    PropertyDescriptor ps = BeanUtils.getPropertyDescriptor(entityClass, entry.getKey());
                    if (ps == null || ps.getReadMethod() == null) {
                        continue;
                    }
                    Object value = ReflectionUtils.invokeMethod(ps.getReadMethod(), entity);
                    if (!ObjectUtils.isEmpty(value)) {
                        sql.SET(getEquals(entry.getValue(), entry.getKey()));
                    }
                }

                sql.WHERE(getEquals(mataData.getPkColumn(), mataData.getPkProperty()));
                break;
            }
            default: {
                log.error("Unsupported data source");
            }
        }

        return sql.toString();
    }

    public static String selectById(TableMataData mataData, String databaseId, Long id) {

        SQL sql = new SQL();
        switch (databaseId) {
            case "mysql": {
                sql.SELECT(mataData.getBaseColumns());
                sql.FROM(mataData.getTableName());
                sql.WHERE(mataData.getPkColumn() + " = '" + id + "'");
                break;
            }
            default: {
                log.error("Unsupported data source");
            }
        }

        return sql.toString();
    }

    public static String selectByIds(TableMataData mataData, String databaseId, Collection<Long> ids) {

        SQL sql = new SQL();
        switch (databaseId) {
            case "mysql": {
                String idsStr = ids.stream().map(String::valueOf).collect(Collectors.joining("', '"));
                sql.SELECT(mataData.getBaseColumns());
                sql.FROM(mataData.getTableName());
                sql.WHERE(mataData.getPkColumn() + " in ('" + idsStr + "')");
                break;
            }
            default: {
                log.error("Unsupported data source");
            }
        }

        return sql.toString();
    }

    public static String selectAll(TableMataData mataData, String databaseId) {

        SQL sql = new SQL();
        switch (databaseId) {
            case "mysql": {
                sql.SELECT(mataData.getBaseColumns());
                sql.FROM(mataData.getTableName());
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
