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
package org.apache.bigtop.manager.server.service.impl;

import org.apache.bigtop.manager.dao.po.HostComponentPO;
import org.apache.bigtop.manager.dao.repository.ComponentRepository;
import org.apache.bigtop.manager.dao.repository.HostComponentRepository;
import org.apache.bigtop.manager.dao.repository.HostRepository;
import org.apache.bigtop.manager.server.model.converter.HostComponentConverter;
import org.apache.bigtop.manager.server.model.vo.HostComponentVO;
import org.apache.bigtop.manager.server.service.HostComponentService;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class HostComponentServiceImpl implements HostComponentService {

    @Resource
    private HostComponentRepository hostComponentRepository;

    @Resource
    private ComponentRepository componentRepository;

    @Resource
    private HostRepository hostRepository;

    @Override
    public List<HostComponentVO> list(Long clusterId) {
        List<HostComponentPO> hostComponentPOList = hostComponentRepository.findAllByComponentClusterId(clusterId);
        return HostComponentConverter.INSTANCE.fromEntity2VO(hostComponentPOList);
    }

    @Override
    public List<HostComponentVO> listByHost(Long clusterId, Long hostId) {
        List<HostComponentPO> hostComponentPOList =
                hostComponentRepository.findAllByComponentClusterIdAndHostId(clusterId, clusterId);
        return HostComponentConverter.INSTANCE.fromEntity2VO(hostComponentPOList);
    }

    @Override
    public List<HostComponentVO> listByService(Long clusterId, Long serviceId) {
        List<HostComponentPO> hostComponentPOList =
                hostComponentRepository.findAllByComponentClusterIdAndComponentServiceId(clusterId, serviceId);
        return HostComponentConverter.INSTANCE.fromEntity2VO(hostComponentPOList);
    }
}
