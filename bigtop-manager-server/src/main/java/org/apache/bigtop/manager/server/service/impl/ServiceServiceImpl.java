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
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.po.ServiceConfigPO;
import org.apache.bigtop.manager.dao.po.ServiceConfigSnapshotPO;
import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.dao.query.ComponentQuery;
import org.apache.bigtop.manager.dao.query.ServiceQuery;
import org.apache.bigtop.manager.dao.repository.ClusterDao;
import org.apache.bigtop.manager.dao.repository.ComponentDao;
import org.apache.bigtop.manager.dao.repository.ServiceConfigDao;
import org.apache.bigtop.manager.dao.repository.ServiceConfigSnapshotDao;
import org.apache.bigtop.manager.dao.repository.ServiceDao;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.enums.HealthyStatusEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.model.converter.ComponentConverter;
import org.apache.bigtop.manager.server.model.converter.ServiceConfigConverter;
import org.apache.bigtop.manager.server.model.converter.ServiceConfigSnapshotConverter;
import org.apache.bigtop.manager.server.model.converter.ServiceConverter;
import org.apache.bigtop.manager.server.model.dto.ServiceConfigDTO;
import org.apache.bigtop.manager.server.model.query.PageQuery;
import org.apache.bigtop.manager.server.model.req.ServiceConfigReq;
import org.apache.bigtop.manager.server.model.req.ServiceConfigSnapshotReq;
import org.apache.bigtop.manager.server.model.vo.ComponentVO;
import org.apache.bigtop.manager.server.model.vo.PageVO;
import org.apache.bigtop.manager.server.model.vo.ServiceConfigSnapshotVO;
import org.apache.bigtop.manager.server.model.vo.ServiceConfigVO;
import org.apache.bigtop.manager.server.model.vo.ServiceUserVO;
import org.apache.bigtop.manager.server.model.vo.ServiceVO;
import org.apache.bigtop.manager.server.service.ServiceService;
import org.apache.bigtop.manager.server.utils.PageUtils;
import org.apache.bigtop.manager.server.utils.StackConfigUtils;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.apache.commons.collections4.CollectionUtils;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class ServiceServiceImpl implements ServiceService {

    @Resource
    private ClusterDao clusterDao;

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
    public PageVO<ServiceUserVO> serviceUsers(Long clusterId) {
        PageQuery pageQuery = PageUtils.getPageQuery();
        try (Page<?> ignored =
                PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize(), pageQuery.getOrderBy())) {
            ServiceQuery query = ServiceQuery.builder().clusterId(clusterId).build();
            List<ServicePO> servicePOList = serviceDao.findByQuery(query);

            ClusterPO clusterPO = clusterDao.findById(clusterId);
            List<ServiceUserVO> res = new ArrayList<>();
            for (ServicePO servicePO : servicePOList) {
                ServiceUserVO serviceUserVO = new ServiceUserVO();
                serviceUserVO.setDisplayName(servicePO.getDisplayName());
                serviceUserVO.setUser(servicePO.getUser());
                serviceUserVO.setUserGroup(clusterPO.getUserGroup());
                serviceUserVO.setDesc(servicePO.getDesc());
                res.add(serviceUserVO);
            }

            PageInfo<ServicePO> pageInfo = new PageInfo<>(servicePOList);
            return PageVO.of(res, pageInfo.getTotal());
        } finally {
            PageHelper.clearPage();
        }
    }

    @Override
    public ServiceVO get(Long id) {
        ServiceVO serviceVO = ServiceConverter.INSTANCE.fromPO2VO(serviceDao.findById(id));

        ComponentQuery query = ComponentQuery.builder().serviceId(id).build();
        List<ComponentPO> componentPOList = componentDao.findByQuery(query);
        List<ComponentVO> componentVOList = ComponentConverter.INSTANCE.fromPO2VO(componentPOList);
        List<ServiceConfigVO> serviceConfigVOList = listConf(null, id);

        serviceVO.setComponents(componentVOList);
        serviceVO.setConfigs(serviceConfigVOList);
        return serviceVO;
    }

    @Override
    @Transactional
    public Boolean remove(Long id) {
        ServicePO servicePO = serviceDao.findById(id);
        if (servicePO == null) {
            throw new ApiException(ApiExceptionEnum.SERVICE_NOT_FOUND);
        }

        // Check if required by other installed services
        List<String> requiredBy = StackUtils.getServiceRequiredBy(servicePO.getName());
        if (CollectionUtils.isNotEmpty(requiredBy)) {
            boolean isInfra = servicePO.getClusterId() == 0;
            List<ServicePO> servicePOList;
            if (isInfra) {
                servicePOList = serviceDao.findByClusterIdAndNames(null, requiredBy);
            } else {
                servicePOList = serviceDao.findByClusterIdAndNames(servicePO.getClusterId(), requiredBy);
            }

            if (CollectionUtils.isNotEmpty(servicePOList)) {
                throw new ApiException(
                        ApiExceptionEnum.SERVICE_REQUIRED_BY,
                        servicePOList.get(0).getDisplayName());
            }
        }

        // Check service status - only allow deletion when service is stopped
        if (!Objects.equals(servicePO.getStatus(), HealthyStatusEnum.UNHEALTHY.getCode())) {
            throw new ApiException(ApiExceptionEnum.SERVICE_IS_RUNNING);
        }

        ComponentQuery query = ComponentQuery.builder().serviceId(id).build();
        List<ComponentPO> componentPOList = componentDao.findByQuery(query);

        // Check all components status - only allow deletion when all components are stopped
        // Skip client components as they are always healthy
        for (ComponentPO componentPO : componentPOList) {
            if (StackUtils.isClientComponent(componentPO.getName())) {
                continue;
            }
            if (!Objects.equals(componentPO.getStatus(), HealthyStatusEnum.UNHEALTHY.getCode())) {
                throw new ApiException(ApiExceptionEnum.COMPONENT_IS_RUNNING);
            }
        }

        // Delete all components first
        componentDao.deleteByIds(
                componentPOList.stream().map(ComponentPO::getId).toList());

        // Delete all service configurations
        List<ServiceConfigPO> configPOList = serviceConfigDao.findByServiceId(id);
        serviceConfigDao.deleteByIds(
                configPOList.stream().map(ServiceConfigPO::getId).toList());

        // Delete all service config snapshots
        List<ServiceConfigSnapshotPO> snapshotPOList = serviceConfigSnapshotDao.findByServiceId(id);
        serviceConfigSnapshotDao.deleteByIds(
                snapshotPOList.stream().map(ServiceConfigSnapshotPO::getId).toList());

        // Finally delete the service
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
        ServicePO servicePO = serviceDao.findById(serviceId);
        List<ServiceConfigPO> configs = serviceConfigDao.findByServiceId(serviceId);

        List<ServiceConfigDTO> oriConfigs;
        List<ServiceConfigDTO> newConfigs;
        List<ServiceConfigDTO> mergedConfigs;

        // Merge stack config with existing config first, in case new property has been added to config xml.
        oriConfigs = StackUtils.SERVICE_CONFIG_MAP.get(servicePO.getName());
        newConfigs = ServiceConfigConverter.INSTANCE.fromPO2DTO(configs);
        mergedConfigs = StackConfigUtils.mergeServiceConfigs(oriConfigs, newConfigs);

        // Merge existing config with new config in request object
        oriConfigs = mergedConfigs;
        newConfigs = ServiceConfigConverter.INSTANCE.fromReq2DTO(reqs);
        mergedConfigs = StackConfigUtils.mergeServiceConfigs(oriConfigs, newConfigs);

        // Save merged config
        List<ServiceConfigPO> serviceConfigPOList = ServiceConfigConverter.INSTANCE.fromDTO2PO(mergedConfigs);
        serviceConfigDao.partialUpdateByIds(serviceConfigPOList);
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
