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

import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.po.ServiceConfigPO;
import org.apache.bigtop.manager.dao.po.ServiceConfigSnapshotPO;
import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.dao.query.ComponentQuery;
import org.apache.bigtop.manager.dao.query.ServiceQuery;
import org.apache.bigtop.manager.dao.repository.ComponentDao;
import org.apache.bigtop.manager.dao.repository.ServiceConfigDao;
import org.apache.bigtop.manager.dao.repository.ServiceConfigSnapshotDao;
import org.apache.bigtop.manager.dao.repository.ServiceDao;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.model.converter.ServiceConfigConverter;
import org.apache.bigtop.manager.server.model.converter.ServiceConfigSnapshotConverter;
import org.apache.bigtop.manager.server.model.converter.ServiceConverter;
import org.apache.bigtop.manager.server.model.query.PageQuery;
import org.apache.bigtop.manager.server.model.req.ServiceConfigReq;
import org.apache.bigtop.manager.server.model.req.ServiceConfigSnapshotReq;
import org.apache.bigtop.manager.server.model.vo.PageVO;
import org.apache.bigtop.manager.server.model.vo.ServiceConfigSnapshotVO;
import org.apache.bigtop.manager.server.model.vo.ServiceConfigVO;
import org.apache.bigtop.manager.server.model.vo.ServiceVO;
import org.apache.bigtop.manager.server.service.ServiceService;
import org.apache.bigtop.manager.server.utils.PageUtils;

import org.apache.commons.collections4.CollectionUtils;

import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ServiceServiceImpl implements ServiceService {

    @Resource
    private ServiceDao serviceDao;

    @Resource
    private ServiceConfigDao serviceConfigDao;

    @Resource
    private ServiceConfigSnapshotDao serviceConfigSnapshotDao;

    @Resource
    private ComponentDao componentDao;

    @Override
    public PageVO<ServiceVO> list(ServiceQuery query) {
        PageQuery pageQuery = PageUtils.getPageQuery();
        try (Page<?> ignored =
                PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize(), pageQuery.getOrderBy())) {
            List<ServicePO> servicePOList = serviceDao.findByQuery(query);
            PageInfo<ServicePO> pageInfo = new PageInfo<>(servicePOList);
            return PageVO.of(pageInfo);
        } finally {
            PageHelper.clearPage();
        }
    }

    @Override
    public ServiceVO get(Long id) {
        return ServiceConverter.INSTANCE.fromPO2VO(serviceDao.findById(id));
    }

    @Override
    public Boolean remove(Long id) {
        ComponentQuery query = ComponentQuery.builder().serviceId(id).build();
        List<ComponentPO> componentPOList = componentDao.findByQuery(query);
        if (CollectionUtils.isNotEmpty(componentPOList)) {
            throw new ApiException(ApiExceptionEnum.SERVICE_HAS_COMPONENTS);
        }

        return serviceDao.deleteById(id);
    }

    @Override
    public List<ServiceConfigVO> listConf(Long clusterId, Long serviceId) {
        List<ServiceConfigPO> list = serviceConfigDao.findByServiceId(serviceId);
        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        } else {
            return ServiceConfigConverter.INSTANCE.fromPO2VO(list);
        }
    }

    @Override
    public List<ServiceConfigVO> updateConf(Long clusterId, Long serviceId, List<ServiceConfigReq> reqs) {
        List<ServiceConfigPO> list = new ArrayList<>();
        for (ServiceConfigReq req : reqs) {
            ServiceConfigPO serviceConfigPO = new ServiceConfigPO();
            serviceConfigPO.setId(req.getId());
            serviceConfigPO.setPropertiesJson(JsonUtils.writeAsString(req.getProperties()));
            list.add(serviceConfigPO);
        }

        serviceConfigDao.partialUpdateByIds(list);
        return listConf(clusterId, serviceId);
    }

    @Override
    public List<ServiceConfigSnapshotVO> listConfSnapshots(Long clusterId, Long serviceId) {
        List<ServiceConfigSnapshotPO> list = serviceConfigSnapshotDao.findByServiceId(serviceId);
        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        } else {
            return ServiceConfigSnapshotConverter.INSTANCE.fromPO2VO(list);
        }
    }

    @Override
    public ServiceConfigSnapshotVO takeConfSnapshot(Long clusterId, Long serviceId, ServiceConfigSnapshotReq req) {
        List<ServiceConfigPO> list = serviceConfigDao.findByServiceId(serviceId);
        Map<String, String> confMap = new HashMap<>();
        for (ServiceConfigPO serviceConfigPO : list) {
            confMap.put(serviceConfigPO.getName(), serviceConfigPO.getPropertiesJson());
        }

        String confJson = JsonUtils.writeAsString(confMap);
        ServiceConfigSnapshotPO serviceConfigSnapshotPO = new ServiceConfigSnapshotPO();
        serviceConfigSnapshotPO.setName(req.getName());
        serviceConfigSnapshotPO.setDesc(req.getDesc());
        serviceConfigSnapshotPO.setConfigJson(confJson);
        serviceConfigSnapshotPO.setServiceId(serviceId);
        serviceConfigSnapshotDao.save(serviceConfigSnapshotPO);
        return ServiceConfigSnapshotConverter.INSTANCE.fromPO2VO(serviceConfigSnapshotPO);
    }

    @Override
    public List<ServiceConfigVO> recoveryConfSnapshot(Long clusterId, Long serviceId, Long snapshotId) {
        ServiceConfigSnapshotPO serviceConfigSnapshotPO = serviceConfigSnapshotDao.findById(snapshotId);
        Map<String, String> confMap = JsonUtils.readFromString(serviceConfigSnapshotPO.getConfigJson());
        List<ServiceConfigPO> list = serviceConfigDao.findByServiceId(serviceId);
        for (ServiceConfigPO serviceConfigPO : list) {
            String value = confMap.get(serviceConfigPO.getName());
            if (value != null) {
                serviceConfigPO.setPropertiesJson(value);
            }
        }

        serviceConfigDao.updateByIds(list);
        return ServiceConfigConverter.INSTANCE.fromPO2VO(list);
    }

    @Override
    public Boolean deleteConfSnapshot(Long clusterId, Long serviceId, Long snapshotId) {
        return serviceConfigSnapshotDao.deleteById(snapshotId);
    }
}
