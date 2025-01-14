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
package org.apache.bigtop.manager.server.service;

import org.apache.bigtop.manager.dao.query.ServiceQuery;
import org.apache.bigtop.manager.server.model.req.ServiceConfigReq;
import org.apache.bigtop.manager.server.model.req.ServiceConfigSnapshotReq;
import org.apache.bigtop.manager.server.model.vo.PageVO;
import org.apache.bigtop.manager.server.model.vo.ServiceConfigSnapshotVO;
import org.apache.bigtop.manager.server.model.vo.ServiceConfigVO;
import org.apache.bigtop.manager.server.model.vo.ServiceVO;

import java.util.List;

public interface ServiceService {

    /**
     * Get services by query.
     *
     * @return services
     */
    PageVO<ServiceVO> list(ServiceQuery query);

    /**
     * Get a service.
     *
     * @return service
     */
    ServiceVO get(Long id);

    /**
     * Remove a service.
     *
     * @return service
     */
    Boolean remove(Long id);

    List<ServiceConfigVO> listConf(Long clusterId, Long serviceId);

    List<ServiceConfigVO> updateConf(Long clusterId, Long serviceId, List<ServiceConfigReq> reqs);

    List<ServiceConfigSnapshotVO> listConfSnapshots(Long clusterId, Long serviceId);

    ServiceConfigSnapshotVO takeConfSnapshot(Long clusterId, Long serviceId, ServiceConfigSnapshotReq req);

    List<ServiceConfigVO> recoveryConfSnapshot(Long clusterId, Long serviceId, Long snapshotId);

    Boolean deleteConfSnapshot(Long clusterId, Long serviceId, Long snapshotId);
}
