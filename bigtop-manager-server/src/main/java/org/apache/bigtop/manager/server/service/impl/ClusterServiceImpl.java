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

import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.dao.repository.ClusterDao;
import org.apache.bigtop.manager.dao.repository.HostDao;
import org.apache.bigtop.manager.dao.repository.ServiceDao;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.model.converter.ClusterConverter;
import org.apache.bigtop.manager.server.model.dto.ClusterDTO;
import org.apache.bigtop.manager.server.model.vo.ClusterVO;
import org.apache.bigtop.manager.server.service.ClusterService;

import org.apache.commons.collections4.CollectionUtils;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class ClusterServiceImpl implements ClusterService {

    @Resource
    private ClusterDao clusterDao;

    @Resource
    private HostDao hostDao;

    @Resource
    private ServiceDao serviceDao;

    @Override
    public List<ClusterVO> list() {
        List<ClusterPO> clusterPOList = clusterDao.findAll();
        return ClusterConverter.INSTANCE.fromPO2VO(clusterPOList);
    }

    @Override
    public ClusterVO get(Long id) {
        ClusterPO clusterPO = clusterDao.findDetailsById(id);
        if (clusterPO == null) {
            throw new ApiException(ApiExceptionEnum.CLUSTER_NOT_FOUND);
        }

        int serviceNum = serviceDao.countByClusterId(id);
        clusterPO.setTotalService((long) serviceNum);
        return ClusterConverter.INSTANCE.fromPO2VO(clusterPO);
    }

    @Override
    public ClusterVO update(Long id, ClusterDTO clusterDTO) {
        ClusterPO clusterPO = ClusterConverter.INSTANCE.fromDTO2PO(clusterDTO);
        clusterPO.setId(id);
        clusterDao.partialUpdateById(clusterPO);

        return get(id);
    }

    @Override
    public Boolean remove(Long id) {
        List<HostPO> hostPOList = hostDao.findAllByClusterId(id);
        if (CollectionUtils.isNotEmpty(hostPOList)) {
            throw new ApiException(ApiExceptionEnum.CLUSTER_HAS_HOSTS);
        }

        List<ServicePO> servicePOList = serviceDao.findByClusterId(id);
        if (CollectionUtils.isNotEmpty(servicePOList)) {
            throw new ApiException(ApiExceptionEnum.CLUSTER_HAS_SERVICES);
        }

        return clusterDao.deleteById(id);
    }
}
