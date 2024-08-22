package org.apache.bigtop.manager.dao.mapper;

import org.apache.bigtop.manager.dao.sql.BaseSqlProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;
import java.util.Optional;

public interface BaseMapper<Entity> {

    /**
     * Insert the entity.
     */
    @InsertProvider(type = BaseSqlProvider.class, method = "save")
    int save(Entity entity);

    /**
     * Update the entity by primary key.
     */
    @UpdateProvider(type = BaseSqlProvider.class, method = "updateById")
    int updateById(Entity entity);

    /**
     * Query the entity by primary key.
     */
    @SelectProvider(type = BaseSqlProvider.class, method = "findById")
    Optional<Entity> findById(Long id);

    /**
     * Query all entities.
     */
    @SelectProvider(type = BaseSqlProvider.class, method = "findAll")
    List<Entity> findAll();


}
