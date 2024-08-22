package org.apache.bigtop.manager.dao.sql;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import org.apache.bigtop.manager.common.utils.CaseUtils;
import org.apache.bigtop.manager.common.utils.ClassUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

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
public class TableMataData {

    private static final Map<Class<?>, TableMataData> TABLE_CACHE = new ConcurrentHashMap<>(64);

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

    private TableMataData(Class<?> clazz) {
        initTableInfo(clazz);
    }


    public static TableMataData forClass(Class<?> entityClass) {
        TableMataData tableMataDate = TABLE_CACHE.get(entityClass);
        if (tableMataDate == null) {
            tableMataDate = new TableMataData(entityClass);
            TABLE_CACHE.put(entityClass, tableMataDate);
        }

        return tableMataDate;
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
        tableName = clazz.isAnnotationPresent(Table.class) ? clazz.getAnnotation(Table.class).name()
                : CaseUtils.toUnderScoreCase(clazz.getSimpleName());

        //获取父类字段
        List<Field> fields = ClassUtils.getFields(clazz);
        for (Field field : fields) {

            if (Modifier.isStatic(field.getModifiers()) ||
                    field.isAnnotationPresent(Transient.class) ||
                    !BeanUtils.isSimpleValueType(field.getType())) {
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
