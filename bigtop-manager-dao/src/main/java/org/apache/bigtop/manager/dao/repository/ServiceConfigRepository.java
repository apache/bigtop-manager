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

import org.apache.bigtop.manager.dao.po.Cluster;
import org.apache.bigtop.manager.dao.po.Service;
import org.apache.bigtop.manager.dao.po.ServiceConfig;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ServiceConfigRepository extends JpaRepository<ServiceConfig, Long> {

    List<ServiceConfig> findAllByCluster(Cluster cluster);

    List<ServiceConfig> findAllByCluster(Cluster cluster, Sort sort);

    List<ServiceConfig> findAllByClusterAndService(Cluster cluster, Service service);

    ServiceConfig findByClusterAndServiceAndSelectedIsTrue(Cluster cluster, Service service);

    List<ServiceConfig> findAllByClusterAndSelectedIsTrue(Cluster cluster);

    @Transactional
    @Modifying
    @Query("UPDATE ServiceConfig s SET s.selected = false WHERE s.cluster = :cluster AND s.service = :service")
    void setAllSelectedToFalseByClusterAndService(Cluster cluster, Service service);
}
