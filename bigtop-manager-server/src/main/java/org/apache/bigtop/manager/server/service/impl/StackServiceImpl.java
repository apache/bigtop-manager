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
import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.dao.query.ServiceQuery;
import org.apache.bigtop.manager.dao.repository.ClusterDao;
import org.apache.bigtop.manager.dao.repository.ServiceDao;
import org.apache.bigtop.manager.server.model.converter.ClusterConverter;
import org.apache.bigtop.manager.server.model.converter.ServiceConverter;
import org.apache.bigtop.manager.server.model.converter.StackConverter;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.StackDTO;
import org.apache.bigtop.manager.server.model.vo.ClusterVO;
import org.apache.bigtop.manager.server.model.vo.StackVO;
import org.apache.bigtop.manager.server.service.StackService;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class StackServiceImpl implements StackService {

    @Resource
    private ClusterDao clusterDao;

    @Resource
    private ServiceDao serviceDao;

    @Override
    public List<StackVO> list() {
        List<StackVO> stackVOList = new ArrayList<>();

        for (Map.Entry<StackDTO, List<ServiceDTO>> entry : StackUtils.STACK_SERVICE_MAP.entrySet()) {
            StackDTO stackDTO = entry.getKey();
            List<ServiceDTO> serviceDTOList = entry.getValue();
            for (ServiceDTO serviceDTO : serviceDTOList) {
                serviceDTO.setConfigs(StackUtils.SERVICE_CONFIG_MAP.get(serviceDTO.getName()));
            }

            StackVO stackVO = StackConverter.INSTANCE.fromDTO2VO(stackDTO);
            stackVO.setServices(ServiceConverter.INSTANCE.fromDTO2VO(serviceDTOList));
            stackVOList.add(stackVO);
        }

        return stackVOList;
    }

    @Override
    public List<ClusterVO> serviceClusters(String serviceName) {
        ServiceQuery query = ServiceQuery.builder().name(serviceName).build();
        List<ServicePO> servicePOList = serviceDao.findByQuery(query);
        if (servicePOList.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> clusterIds =
                servicePOList.stream().map(ServicePO::getClusterId).toList();
        List<ClusterPO> clusterPOList = clusterDao.findByIds(clusterIds);
        return ClusterConverter.INSTANCE.fromPO2VO(clusterPOList);
    }
}
