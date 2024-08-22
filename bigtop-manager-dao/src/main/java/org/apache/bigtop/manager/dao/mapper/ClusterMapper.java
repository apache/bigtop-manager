package org.apache.bigtop.manager.dao.mapper;

import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

public interface ClusterMapper extends BaseMapper<ClusterPO> {

    Optional<ClusterPO> findByClusterName(@Param("clusterName") String clusterName);

    Integer count();

}
