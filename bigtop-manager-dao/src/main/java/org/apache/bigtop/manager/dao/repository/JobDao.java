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

package org.apache.bigtop.manager.dao.repository;

import org.apache.bigtop.manager.dao.po.JobPO;

import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface JobDao extends BaseDao<JobPO> {

    List<JobPO> findAllByClusterId(@Param("clusterId") Long clusterId);

    List<JobPO> findAllByIdsJoin(@Param("ids") Collection<Long> ids);

    Optional<JobPO> findByIdJoin(@Param("id") Long id);

    List<JobPO> findAllByClusterIsNull();
}
