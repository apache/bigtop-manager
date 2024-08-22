package org.apache.bigtop.manager.dao.mapper;

import org.apache.bigtop.manager.dao.sql.BaseSqlProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BaseMapper<Entity> {

    /**
     * Insert the entity.
     */
    @InsertProvider(type = BaseSqlProvider.class, method = "insert")
    int save(Entity entity);

    /**
     * Update the entity by primary key.
     */
    @UpdateProvider(type = BaseSqlProvider.class, method = "updateById")
    int updateById(Entity entity);

    /**
     * Query the entity by primary key.
     */
    @SelectProvider(type = BaseSqlProvider.class, method = "selectById")
    Entity findById(Long id);

    /**
     * Query the entity by primary key.
     */
    default Optional<Entity> findOptionalById(Long id) {
        return Optional.ofNullable(findById(id));
    }

    /**
     * Query the entity by primary key.
     */
    @SelectProvider(type = BaseSqlProvider.class, method = "selectByIds")
    List<Entity> findByIds(Collection<Long> ids);

    /**
     * Query all entities.
     */
    @SelectProvider(type = BaseSqlProvider.class, method = "selectAll")
    List<Entity> findAll();


}
