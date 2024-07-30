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

import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.dao.po.ServiceConfigPO;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ServiceConfigRepository extends JpaRepository<ServiceConfigPO, Long> {

    List<ServiceConfigPO> findAllByCluster(ClusterPO clusterPO);

    List<ServiceConfigPO> findAllByCluster(ClusterPO clusterPO, Sort sort);

    List<ServiceConfigPO> findAllByClusterAndService(ClusterPO clusterPO, ServicePO servicePO);

    ServiceConfigPO findByClusterAndServiceAndSelectedIsTrue(ClusterPO clusterPO, ServicePO servicePO);

    List<ServiceConfigPO> findAllByClusterAndSelectedIsTrue(ClusterPO clusterPO);

    @Transactional
    @Modifying
    @Query("UPDATE ServiceConfigPO s SET s.selected = false WHERE s.clusterPO = :cluster AND s.service = :service")
    void setAllSelectedToFalseByClusterAndService(ClusterPO clusterPO, ServicePO servicePO);
}
