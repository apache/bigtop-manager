package org.apache.bigtop.manager.dao.mapper;

import org.apache.bigtop.manager.dao.po.JobPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface JobMapper extends BaseMapper<JobPO> {

    List<JobPO> findAllByClusterId(@Param("clusterId") Long clusterId);

    List<JobPO> findAllByClusterIsNull();

}
