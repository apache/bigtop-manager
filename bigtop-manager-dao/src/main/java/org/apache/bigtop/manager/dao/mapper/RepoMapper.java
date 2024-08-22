package org.apache.bigtop.manager.dao.mapper;

import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.RepoPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface RepoMapper extends BaseMapper<RepoPO> {

    Optional<RepoPO> findByRepoName(@Param("repoName") String clusterName);

    int saveAll(@Param("clusters") List<RepoPO> repos);

    List<RepoPO> findAllByClusterId(@Param("clusterId") Long clusterId);

}
