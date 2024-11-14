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

import org.apache.bigtop.manager.common.constants.ComponentCategories;
import org.apache.bigtop.manager.common.enums.MaintainState;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.po.HostComponentPO;
import org.apache.bigtop.manager.dao.po.ServiceConfigPO;
import org.apache.bigtop.manager.dao.po.ServiceConfigSnapshotPO;
import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.dao.repository.HostComponentDao;
import org.apache.bigtop.manager.dao.repository.ServiceConfigDao;
import org.apache.bigtop.manager.dao.repository.ServiceConfigSnapshotDao;
import org.apache.bigtop.manager.dao.repository.ServiceDao;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.model.converter.ServiceConfigConverter;
import org.apache.bigtop.manager.server.model.converter.ServiceConfigSnapshotConverter;
import org.apache.bigtop.manager.server.model.converter.ServiceConverter;
import org.apache.bigtop.manager.server.model.dto.PropertyDTO;
import org.apache.bigtop.manager.server.model.dto.QuickLinkDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceConfigDTO;
import org.apache.bigtop.manager.server.model.req.ServiceConfigReq;
import org.apache.bigtop.manager.server.model.req.ServiceConfigSnapshotReq;
import org.apache.bigtop.manager.server.model.vo.QuickLinkVO;
import org.apache.bigtop.manager.server.model.vo.ServiceConfigSnapshotVO;
import org.apache.bigtop.manager.server.model.vo.ServiceConfigVO;
import org.apache.bigtop.manager.server.model.vo.ServiceVO;
import org.apache.bigtop.manager.server.service.ServiceService;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ServiceServiceImpl implements ServiceService {

    @Resource
    private ServiceDao serviceDao;

    @Resource
    private HostComponentDao hostComponentDao;

    @Resource
    private ServiceConfigDao serviceConfigDao;

    @Resource
    private ServiceConfigSnapshotDao serviceConfigSnapshotDao;

    @Override
    public List<ServiceVO> list(Long clusterId) {
        List<ServiceVO> res = new ArrayList<>();

        List<ServicePO> servicePOList = serviceDao.findAllByClusterId(clusterId);
        List<HostComponentPO> allByClusterId = hostComponentDao.findAllByClusterId(clusterId);
        Map<Long, List<HostComponentPO>> serviceIdToHostComponent =
                allByClusterId.stream().collect(Collectors.groupingBy(HostComponentPO::getServiceId));
        Map<Long, List<HostComponentPO>> componentIdToHostComponent =
                allByClusterId.stream().collect(Collectors.groupingBy(HostComponentPO::getComponentId));

        for (ServicePO servicePO : servicePOList) {
            Long serviceId = servicePO.getId();
            ServiceVO serviceVO = ServiceConverter.INSTANCE.fromPO2VO(servicePO);
            List<ComponentPO> components = servicePO.getComponents();
            serviceVO.setQuickLinks(new ArrayList<>());

            for (ComponentPO componentPO : components) {
                Long componentId = componentPO.getId();
                List<HostComponentPO> hostComponentPOList = componentIdToHostComponent.get(componentId);

                String quickLink = componentPO.getQuickLink();
                if (StringUtils.isNotBlank(quickLink)) {
                    List<QuickLinkVO> quickLinkVOList =
                            resolveQuickLink(hostComponentPOList, quickLink, clusterId, serviceId);
                    serviceVO.getQuickLinks().addAll(quickLinkVOList);
                }
            }
            boolean isClient = components.stream()
                    .allMatch(componentPO -> componentPO.getCategory().equalsIgnoreCase(ComponentCategories.CLIENT));
            boolean isHealthy = serviceIdToHostComponent.get(serviceId).stream().allMatch(hostComponentPO -> {
                MaintainState expectedState = hostComponentPO.getCategory().equalsIgnoreCase(ComponentCategories.CLIENT)
                        ? MaintainState.INSTALLED
                        : MaintainState.STARTED;
                return hostComponentPO.getState().equals(expectedState.getName());
            });

            serviceVO.setIsClient(isClient);
            serviceVO.setIsHealthy(isHealthy);
            res.add(serviceVO);
        }

        return res;
    }

    @Override
    public ServiceVO get(Long id) {
        ServicePO servicePO =
                serviceDao.findByIdJoin(id).orElseThrow(() -> new ApiException(ApiExceptionEnum.SERVICE_NOT_FOUND));
        return ServiceConverter.INSTANCE.fromPO2VO(servicePO);
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

    private List<QuickLinkVO> resolveQuickLink(
            List<HostComponentPO> hostComponentPOList, String quickLinkJson, Long clusterId, Long serviceId) {
        List<QuickLinkVO> quickLinkVOList = new ArrayList<>();

        QuickLinkDTO quickLinkDTO = JsonUtils.readFromString(quickLinkJson, QuickLinkDTO.class);

        List<ServiceConfigPO> serviceConfigPOList = serviceConfigDao.findByServiceId(serviceId);

        String httpPort = quickLinkDTO.getHttpPortDefault();
        // Use HTTP for now, need to handle https in the future
        for (ServiceConfigPO serviceConfigPO : serviceConfigPOList) {
            ServiceConfigDTO serviceConfigDTO = ServiceConfigConverter.INSTANCE.fromPO2DTO(serviceConfigPO);
            for (PropertyDTO propertyDTO : serviceConfigDTO.getProperties()) {
                if (propertyDTO.getName().equals(quickLinkDTO.getHttpPortProperty())) {

                    httpPort = propertyDTO.getValue().contains(":")
                            ? propertyDTO.getValue().split(":")[1]
                            : propertyDTO.getValue();
                }
            }
        }

        for (HostComponentPO hostComponentPO : hostComponentPOList) {
            QuickLinkVO quickLinkVO = new QuickLinkVO();
            quickLinkVO.setDisplayName(quickLinkDTO.getDisplayName());
            String url = "http://" + hostComponentPO.getHostname() + ":" + httpPort;
            quickLinkVO.setUrl(url);
            quickLinkVOList.add(quickLinkVO);
        }

        return quickLinkVOList;
    }
}
