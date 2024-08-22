package org.apache.bigtop.manager.dao.sql;

import lombok.extern.slf4j.Slf4j;
import org.apache.bigtop.manager.dao.mapper.BaseMapper;
import org.apache.ibatis.builder.annotation.ProviderContext;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

import java.lang.reflect.Type;
import java.util.Collection;

@Slf4j
public class BaseSqlProvider {

    public <Entity> String insert(Entity entity, ProviderContext context) {
        Assert.notNull(entity, "entity must not null");

        String databaseId = context.getDatabaseId();

        Class<?> entityClass = entity.getClass();
        TableMataData mataData = TableMataData.forClass(entityClass);

        return SQLBuilder.insert(mataData, entity, databaseId);
    }

    public <Entity> String updateById(Entity entity, ProviderContext context) {
        Assert.notNull(entity, "entity must not null");

        String databaseId = context.getDatabaseId();

        Class<?> entityClass = entity.getClass();
        TableMataData mataData = TableMataData.forClass(entityClass);

        return SQLBuilder.update(mataData, entity, databaseId);
    }

    public String selectById(Long id, ProviderContext context) {
        String databaseId = context.getDatabaseId();

        Class<?> entityClass = getEntityClass(context);
        TableMataData mataData = TableMataData.forClass(entityClass);

        return SQLBuilder.selectById(mataData, databaseId, id);
    }

    public String selectByIds(Collection<Long> ids, ProviderContext context) {
        String databaseId = context.getDatabaseId();

        Class<?> entityClass = getEntityClass(context);
        TableMataData mataData = TableMataData.forClass(entityClass);

        return SQLBuilder.selectByIds(mataData, databaseId, ids);
    }

    public String selectAll(ProviderContext context) {
        String databaseId = context.getDatabaseId();

        Class<?> entityClass = getEntityClass(context);
        TableMataData mataData = TableMataData.forClass(entityClass);

        return SQLBuilder.selectAll(mataData, databaseId);

    }

    private Class<?> getEntityClass(ProviderContext context) {
        Class<?> mapperType = context.getMapperType();
        for (Type parent : mapperType.getGenericInterfaces()) {
            ResolvableType parentType = ResolvableType.forType(parent);
            if (parentType.getRawClass() == BaseMapper.class) {
                return parentType.getGeneric(0).getRawClass();
            }
        }
        return null;
    }

}
