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

import org.apache.bigtop.manager.dao.query.HostQuery;
import org.apache.bigtop.manager.server.model.dto.HostDTO;
import org.apache.bigtop.manager.server.model.vo.ComponentVO;
import org.apache.bigtop.manager.server.model.vo.HostVO;
import org.apache.bigtop.manager.server.model.vo.InstalledStatusVO;
import org.apache.bigtop.manager.server.model.vo.PageVO;

import java.util.List;

public interface HostService {

    /**
     * Get all hosts.
     *
     * @return Hosts
     */
    PageVO<HostVO> list(HostQuery hostQuery);

    List<HostVO> add(HostDTO hostDTO);

    /**
     * Save a host
     *
     * @return Host
     */
    List<HostVO> batchSave(Long clusterId, List<String> hostnames);

    /**
     * Get a host
     *
     * @return Host
     */
    HostVO get(Long id);

    /**
     * Update a host
     *
     * @return Host
     */
    HostVO update(Long id, HostDTO hostDTO);

    /**
     * Delete a host
     *
     * @return Host
     */
    Boolean remove(Long id);

    /**
     * Get host components
     *
     * @return Components
     */
    List<ComponentVO> components(Long id);

    /**
     * Batch delete hosts
     *
     * @return Host
     */
    Boolean batchRemove(List<Long> ids);

    /**
     * Check hosts connection
     *
     * @param hostDTO host infos
     * @return true if all hosts are able to connect
     */
    Boolean checkConnection(HostDTO hostDTO);

    /**
     * Install dependencies
     *
     * @param hostDTOList host infos
     * @return true if all dependencies are installed
     */
    Boolean installDependencies(List<HostDTO> hostDTOList);

    /**
     * Get dependency installed status
     *
     * @return installed status
     */
    List<InstalledStatusVO> installedStatus();

    /**
     * Start agent on the host
     *
     * @param hostId host id
     * @return true if started successfully
     */
    Boolean startAgent(Long hostId);

    /**
     * Stop agent on the host
     *
     * @param hostId host id
     * @return true if stopped successfully
     */
    Boolean stopAgent(Long hostId);

    /**
     * Restart agent on the host
     *
     * @param hostId host id
     * @return true if restarted successfully
     */
    Boolean restartAgent(Long hostId);

    /**
     * validate host exists
     * @param hostDTO hostnames
     */
    Boolean checkDuplicate(HostDTO hostDTO);
}
