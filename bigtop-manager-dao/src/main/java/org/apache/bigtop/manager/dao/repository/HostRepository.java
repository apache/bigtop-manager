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

import org.apache.bigtop.manager.dao.po.HostPO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface HostRepository extends JpaRepository<HostPO, Long> {

    HostPO findByHostname(String hostname);

    List<HostPO> findAllByHostnameIn(Collection<String> hostnames);

    List<HostPO> findAllByClusterPOIdAndHostnameIn(Long clusterId, Collection<String> hostnames);

    List<HostPO> findAllByClusterPOId(Long clusterId);

    Page<HostPO> findAllByClusterPOId(Long clusterId, Pageable pageable);

    List<HostPO> findAllByClusterPOClusterName(String clusterName);
}
