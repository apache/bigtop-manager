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

import org.apache.bigtop.manager.common.utils.CaseUtils;
import org.apache.bigtop.manager.common.utils.ClassUtils;

import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import lombok.Getter;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class TableMetaData {

    private static final Map<Class<?>, TableMetaData> TABLE_CACHE = new ConcurrentHashMap<>(64);

    /**
     * tableName
     */
    private String tableName;

    /**
     * primaryField
     */
    private String pkProperty;

    /**
     * primaryKey
     */
    private String pkColumn;

    /**
     * {entityField,column}
     */
    private final Map<String, String> fieldColumnMap = new HashMap<>();

    /**
     * {column,columnType}
     */
    private final Map<String, Class<?>> fieldTypeMap = new HashMap<>();

    private TableMetaData(Class<?> clazz) {
        initTableInfo(clazz);
    }

    public static TableMetaData forClass(Class<?> entityClass) {
        TableMetaData tableMetaData = TABLE_CACHE.get(entityClass);
        if (tableMetaData == null) {
            tableMetaData = new TableMetaData(entityClass);
            TABLE_CACHE.put(entityClass, tableMetaData);
        }

        return tableMetaData;
    }

    public String getBaseColumns() {
        Collection<String> columns = fieldColumnMap.values();
        if (CollectionUtils.isEmpty(columns)) {
            return "";
        }
        Iterator<String> iterator = columns.iterator();
        StringBuilder sb = new StringBuilder();
        while (iterator.hasNext()) {
            String next = iterator.next();
            sb.append(tableName).append(".").append(next);
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private void initTableInfo(Class<?> clazz) {
        tableName = clazz.isAnnotationPresent(Table.class)
                ? clazz.getAnnotation(Table.class).name()
                : CaseUtils.toUnderScoreCase(clazz.getSimpleName());

        // 获取父类字段
        List<Field> fields = ClassUtils.getFields(clazz);
        for (Field field : fields) {

            if (Modifier.isStatic(field.getModifiers())
                    || field.isAnnotationPresent(Transient.class)
                    || !BeanUtils.isSimpleValueType(field.getType())) {
                continue;
            }

            String property = field.getName();
            Column column = field.getAnnotation(Column.class);
            String columnName = column != null ? column.name() : CaseUtils.toUnderScoreCase(property);

            if (field.isAnnotationPresent(Id.class) || (property.equalsIgnoreCase("id") && pkProperty == null)) {
                pkProperty = property;
                pkColumn = columnName;
            }
            PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(clazz, property);
            if (descriptor != null && descriptor.getReadMethod() != null && descriptor.getWriteMethod() != null) {
                fieldColumnMap.put(property, columnName);
                fieldTypeMap.put(property, field.getType());
            }
        }
    }
}
