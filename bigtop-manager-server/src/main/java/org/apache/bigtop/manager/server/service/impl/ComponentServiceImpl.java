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
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.po.ServiceConfigPO;
import org.apache.bigtop.manager.dao.query.ComponentQuery;
import org.apache.bigtop.manager.dao.repository.ComponentDao;
import org.apache.bigtop.manager.dao.repository.ServiceConfigDao;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.enums.HealthyStatusEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.model.converter.ComponentConverter;
import org.apache.bigtop.manager.server.model.converter.ServiceConfigConverter;
import org.apache.bigtop.manager.server.model.dto.ComponentDTO;
import org.apache.bigtop.manager.server.model.dto.PropertyDTO;
import org.apache.bigtop.manager.server.model.dto.QuickLinkDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceConfigDTO;
import org.apache.bigtop.manager.server.model.query.PageQuery;
import org.apache.bigtop.manager.server.model.vo.ComponentVO;
import org.apache.bigtop.manager.server.model.vo.PageVO;
import org.apache.bigtop.manager.server.model.vo.QuickLinkVO;
import org.apache.bigtop.manager.server.service.ComponentService;
import org.apache.bigtop.manager.server.utils.PageUtils;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class ComponentServiceImpl implements ComponentService {

    @Resource
    private ServiceConfigDao serviceConfigDao;

    @Resource
    private ComponentDao componentDao;

    @Override
    public PageVO<ComponentVO> list(ComponentQuery query) {
        PageQuery pageQuery = PageUtils.getPageQuery();
        try (Page<?> ignored =
                PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize(), pageQuery.getOrderBy())) {
            List<ComponentVO> componentVOList = new ArrayList<>();
            List<ComponentPO> componentPOList = componentDao.findByQuery(query);
            PageInfo<ComponentPO> pageInfo = new PageInfo<>(componentPOList);
            componentPOList.forEach(component -> {
                ComponentVO componentVO = ComponentConverter.INSTANCE.fromPO2VO(component);
                componentVO.setQuickLink(getQuickLink(component));
                componentVOList.add(componentVO);
            });

            return PageVO.of(componentVOList, pageInfo.getTotal());
        } finally {
            PageHelper.clearPage();
        }
    }

    @Override
    public ComponentVO get(Long id) {
        ComponentPO componentPO = componentDao.findDetailsById(id);
        ComponentVO componentVO = ComponentConverter.INSTANCE.fromPO2VO(componentPO);
        componentVO.setQuickLink(getQuickLink(componentPO));
        return componentVO;
    }

    @Override
    public Boolean remove(Long id) {
        ComponentPO componentPO = componentDao.findById(id);
        ComponentDTO componentDTO = StackUtils.getComponentDTO(componentPO.getName());

        // Only server component should be stopped before remove, client component can be removed directly.
        if (componentDTO.getCategory().equals(ComponentCategories.SERVER)
                && Objects.equals(componentPO.getStatus(), HealthyStatusEnum.HEALTHY.getCode())) {
            throw new ApiException(ApiExceptionEnum.COMPONENT_IS_RUNNING);
        }

        return componentDao.deleteById(id);
    }

    private QuickLinkVO getQuickLink(ComponentPO componentPO) {
        ComponentDTO componentDTO = StackUtils.getComponentDTO(componentPO.getName());

        QuickLinkDTO quickLinkDTO = componentDTO.getQuickLink();
        if (quickLinkDTO == null) {
            return null;
        }

        // Use HTTP for now, need to handle https in the future
        List<ServiceConfigPO> serviceConfigPOList = serviceConfigDao.findByServiceId(componentPO.getServiceId());
        String httpPort = quickLinkDTO.getHttpPortDefault();
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

        QuickLinkVO quickLinkVO = new QuickLinkVO();
        quickLinkVO.setDisplayName(quickLinkDTO.getDisplayName());
        String url = "http://" + componentPO.getHostname() + ":" + httpPort;
        quickLinkVO.setUrl(url);
        return quickLinkVO;
    }
}
