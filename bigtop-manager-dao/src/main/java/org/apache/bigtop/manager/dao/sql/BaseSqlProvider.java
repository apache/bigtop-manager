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

import org.apache.bigtop.manager.dao.repository.BaseDao;

import org.apache.ibatis.builder.annotation.ProviderContext;

import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

@Slf4j
public class BaseSqlProvider {

    public <Entity> String insert(Entity entity, ProviderContext context) {
        Assert.notNull(entity, "entity must not null");

        String databaseId = context.getDatabaseId();

        Class<?> entityClass = entity.getClass();
        TableMetaData tableMetaData = TableMetaData.forClass(entityClass);

        return SQLBuilder.insert(tableMetaData, entity, databaseId);
    }

    public <Entity> String insertList(List<Entity> entities, ProviderContext context) {
        Assert.notNull(entities, "entities must not be null");
        Assert.notEmpty(entities, "entities list must not be empty");

        String databaseId = context.getDatabaseId();

        Class<?> entityClass = entities.get(0).getClass();

        TableMetaData tableMetaData = TableMetaData.forClass(entityClass);

        return SQLBuilder.insertList(tableMetaData, entities, databaseId);
    }

    public <Entity> String partialUpdateById(Entity entity, ProviderContext context) {
        Assert.notNull(entity, "entity must not null");

        String databaseId = context.getDatabaseId();

        Class<?> entityClass = entity.getClass();
        TableMetaData tableMetaData = TableMetaData.forClass(entityClass);

        return SQLBuilder.update(tableMetaData, entity, databaseId, true);
    }

    public <Entity> String updateById(Entity entity, ProviderContext context) {
        Assert.notNull(entity, "entity must not null");

        String databaseId = context.getDatabaseId();

        Class<?> entityClass = entity.getClass();
        TableMetaData tableMetaData = TableMetaData.forClass(entityClass);

        return SQLBuilder.update(tableMetaData, entity, databaseId, false);
    }

    public String selectById(Serializable id, ProviderContext context) {
        String databaseId = context.getDatabaseId();

        Class<?> entityClass = getEntityClass(context);
        TableMetaData tableMetaData = TableMetaData.forClass(entityClass);

        return SQLBuilder.selectById(tableMetaData, databaseId, id);
    }

    public String selectByIds(Collection<? extends Serializable> ids, ProviderContext context) {
        String databaseId = context.getDatabaseId();

        Class<?> entityClass = getEntityClass(context);
        TableMetaData tableMetaData = TableMetaData.forClass(entityClass);

        return SQLBuilder.selectByIds(tableMetaData, databaseId, ids);
    }

    public String selectAll(ProviderContext context) {
        String databaseId = context.getDatabaseId();

        Class<?> entityClass = getEntityClass(context);
        TableMetaData tableMetaData = TableMetaData.forClass(entityClass);

        return SQLBuilder.selectAll(tableMetaData, databaseId);
    }

    public String deleteById(Serializable id, ProviderContext context) {
        String databaseId = context.getDatabaseId();

        Class<?> entityClass = getEntityClass(context);
        TableMetaData tableMetaData = TableMetaData.forClass(entityClass);

        return SQLBuilder.deleteById(tableMetaData, databaseId, id);
    }

    public String deleteByIds(Collection<? extends Serializable> ids, ProviderContext context) {
        String databaseId = context.getDatabaseId();

        Class<?> entityClass = getEntityClass(context);
        TableMetaData tableMetaData = TableMetaData.forClass(entityClass);

        return SQLBuilder.deleteByIds(tableMetaData, databaseId, ids);
    }

    private Class<?> getEntityClass(ProviderContext context) {
        Class<?> mapperType = context.getMapperType();
        for (Type parent : mapperType.getGenericInterfaces()) {
            ResolvableType parentType = ResolvableType.forType(parent);
            if (parentType.getRawClass() == BaseDao.class) {
                return parentType.getGeneric(0).getRawClass();
            }
        }
        return null;
    }
}
