package org.apache.bigtop.manager.dao.mapper;

import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ComponentMapper extends BaseMapper<ComponentPO> {

    ComponentPO findByClusterIdAndComponentName(@Param("clusterId") Long clusterId, @Param("componentName") String componentName);

    List<ComponentPO> findAllByClusterId(@Param("clusterId") Long clusterId);

    List<ComponentPO> findAllByClusterIdAndServiceServiceNameIn(@Param("clusterId") Long clusterId, @Param("serviceNames") List<String> serviceNames);

}
