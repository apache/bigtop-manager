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

package org.apache.bigtop.manager.dao.mapper;

import org.apache.bigtop.manager.dao.po.HostComponentPO;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HostComponentMapper extends BaseMapper<HostComponentPO> {

    List<HostComponentPO> findAllJoin();

    List<HostComponentPO> findAllByClusterIdAndComponentName(
            @Param("clusterId") Long clusterId, @Param("componentName") String componentName);

    HostComponentPO findByClusterIdAndComponentNameAndHostname(
            @Param("clusterId") Long clusterId,
            @Param("componentName") String componentName,
            @Param("hostname") String hostname);

    List<HostComponentPO> findAllByClusterIdAndComponentNameAndHostnameIn(
            @Param("clusterId") Long clusterId,
            @Param("componentName") String componentName,
            @Param("hostnames") List<String> hostnames);

    List<HostComponentPO> findAllByClusterId(@Param("clusterId") Long clusterId);

    HostComponentPO findByComponentPOComponentNameAndHostPOHostname(String componentName, String hostName);

    List<HostComponentPO> findAllByClusterIdAndHostId(@Param("clusterId") Long clusterId, @Param("hostId") Long hostId);

    List<HostComponentPO> findAllByClusterIdAndServiceId(
            @Param("clusterId") Long clusterId, @Param("serviceId") Long serviceId);

}
