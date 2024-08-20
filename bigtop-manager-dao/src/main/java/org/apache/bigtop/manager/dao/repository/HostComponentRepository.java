/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.bigtop.manager.dao.repository;

import org.apache.bigtop.manager.dao.po.HostComponentPO;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HostComponentRepository extends JpaRepository<HostComponentPO, Long> {

    List<HostComponentPO> findAllByComponentPOClusterPOIdAndComponentPOComponentName(
            Long clusterId, String componentName);

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
