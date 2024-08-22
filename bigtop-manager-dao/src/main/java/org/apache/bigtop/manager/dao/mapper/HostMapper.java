package org.apache.bigtop.manager.dao.mapper;

import org.apache.bigtop.manager.dao.po.HostPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

public interface HostMapper extends BaseMapper<HostPO> {

    HostPO findByHostname(String hostname);

    List<HostPO> findAllByHostnameIn(Collection<String> hostnames);

    List<HostPO> findAllByClusterPOIdAndHostnameIn(Long clusterId, Collection<String> hostnames);

    List<HostPO> findAllByClusterPOId(Long clusterId);

    Page<HostPO> findAllByClusterPOId(Long clusterId, Pageable pageable);

    List<HostPO> findAllByClusterPOClusterName(String clusterName);
}
