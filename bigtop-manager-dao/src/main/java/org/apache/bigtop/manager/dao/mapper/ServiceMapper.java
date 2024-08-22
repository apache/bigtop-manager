package org.apache.bigtop.manager.dao.mapper;

import org.apache.bigtop.manager.dao.po.ServicePO;

import java.util.List;

public interface ServiceMapper extends BaseMapper<ServicePO> {

    List<ServicePO> findAllByClusterPOId(Long clusterId);

    ServicePO findByClusterPOIdAndServiceName(Long clusterId, String serviceName);

    List<ServicePO> findByClusterPOIdAndServiceNameIn(Long clusterId, List<String> serviceNames);
}
