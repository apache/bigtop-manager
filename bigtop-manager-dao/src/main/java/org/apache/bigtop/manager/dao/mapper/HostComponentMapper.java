package org.apache.bigtop.manager.dao.mapper;

import org.apache.bigtop.manager.dao.po.HostComponentPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HostComponentMapper extends BaseMapper<HostComponentPO> {

    List<HostComponentPO> findAllByClusterIdAndComponentName(
            @Param("clusterId") Long clusterId, @Param("componentName") String componentName);

    HostComponentPO findByComponentPOClusterPOIdAndComponentPOComponentNameAndHostPOHostname(
            Long clusterId, String componentName, String hostnames);

    List<HostComponentPO> findAllByComponentPOClusterPOIdAndComponentPOComponentNameAndHostPOHostnameIn(
            Long clusterId, String componentName, List<String> hostnames);

    List<HostComponentPO> findAllByComponentPOClusterPOId(Long clusterId);

    HostComponentPO findByComponentPOComponentNameAndHostPOHostname(String componentName, String hostName);

    List<HostComponentPO> findAllByComponentPOClusterPOIdAndHostPOId(Long clusterId, Long componentId);

    List<HostComponentPO> findAllByComponentPOClusterPOIdAndComponentPOServicePOId(Long clusterId, Long serviceId);

    List<HostComponentPO> findAllByComponentPOServicePOId(Long serviceId);
}
