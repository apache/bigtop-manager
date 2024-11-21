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

import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.query.HostQuery;
import org.apache.bigtop.manager.dao.repository.HostComponentDao;
import org.apache.bigtop.manager.dao.repository.HostDao;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.enums.HealthyStatusEnum;
import org.apache.bigtop.manager.server.enums.HostAuthTypeEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.model.converter.HostConverter;
import org.apache.bigtop.manager.server.model.dto.HostDTO;
import org.apache.bigtop.manager.server.model.query.PageQuery;
import org.apache.bigtop.manager.server.model.vo.HostVO;
import org.apache.bigtop.manager.server.model.vo.PageVO;
import org.apache.bigtop.manager.server.service.HostService;
import org.apache.bigtop.manager.server.utils.PageUtils;
import org.apache.bigtop.manager.server.utils.RemoteSSHUtils;

import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HostServiceImpl implements HostService {

    @Resource
    private HostDao hostDao;

    @Resource
    private HostComponentDao hostComponentDao;

    @Override
    public PageVO<HostVO> list(HostQuery hostQuery) {
        PageQuery pageQuery = PageUtils.getPageQuery();
        try (Page<?> ignored =
                PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize(), pageQuery.getOrderBy())) {
            List<HostPO> hostPOList = hostDao.findByQuery(hostQuery);
            PageInfo<HostPO> pageInfo = new PageInfo<>(hostPOList);
            return PageVO.of(pageInfo);
        } finally {
            PageHelper.clearPage();
        }
    }

    @Override
    public List<HostVO> add(HostDTO hostDTO) {
        List<HostPO> hostPOList = HostConverter.INSTANCE.fromDTO2POListUsingHostnames(hostDTO);
        for (HostPO hostPO : hostPOList) {
            hostPO.setStatus(HealthyStatusEnum.UNKNOWN.getCode());
        }

        hostDao.saveAll(hostPOList);
        return HostConverter.INSTANCE.fromPO2VO(hostPOList);
    }

    @Override
    public List<HostVO> batchSave(Long clusterId, List<String> hostnames) {
        List<HostPO> hostnameIn = hostDao.findAllByHostnameIn(hostnames);
        List<HostPO> hostPOList = new ArrayList<>();

        Map<String, HostPO> hostInMap =
                hostnameIn.stream().collect(Collectors.toMap(HostPO::getHostname, host -> host));

        for (String hostname : hostnames) {
            HostPO hostPO = new HostPO();
            hostPO.setHostname(hostname);
            hostPO.setClusterId(clusterId);
            hostPO.setStatus(HealthyStatusEnum.UNKNOWN.getCode());

            if (hostInMap.containsKey(hostname)) {
                hostPO.setId(hostInMap.get(hostname).getId());
            }

            hostPOList.add(hostPO);
        }

        hostDao.saveAll(hostPOList);

        return HostConverter.INSTANCE.fromPO2VO(hostPOList);
    }

    @Override
    public HostVO get(Long id) {
        HostPO hostPO = hostDao.findDetailsById(id);
        if (hostPO == null) {
            throw new ApiException(ApiExceptionEnum.HOST_NOT_FOUND);
        }

        return HostConverter.INSTANCE.fromPO2VO(hostPO);
    }

    @Override
    public HostVO update(Long id, HostDTO hostDTO) {
        HostPO hostPO = HostConverter.INSTANCE.fromDTO2PO(hostDTO);
        hostPO.setId(id);
        hostDao.partialUpdateById(hostPO);
        return get(id);
    }

    @Override
    public Boolean delete(Long id) {
        if (hostComponentDao.countByHostId(id) > 0) {
            throw new ApiException(ApiExceptionEnum.HOST_HAS_COMPONENTS);
        }

        hostDao.deleteById(id);
        return true;
    }

    @Override
    public Boolean checkConnection(HostDTO hostDTO) {
        String command = "hostname";
        HostAuthTypeEnum authType = HostAuthTypeEnum.fromCode(hostDTO.getAuthType());
        for (String hostname : hostDTO.getHostnames()) {
            try {
                ShellResult result = null;
                switch (authType) {
                    case PASSWORD -> result = RemoteSSHUtils.executeCommand(
                            hostname, hostDTO.getSshPort(), hostDTO.getSshUser(), hostDTO.getSshPassword(), command);
                    case KEY -> result = RemoteSSHUtils.executeCommand(
                            hostname,
                            hostDTO.getSshPort(),
                            hostDTO.getSshUser(),
                            hostDTO.getSshKeyFilename(),
                            hostDTO.getSshKeyString(),
                            hostDTO.getSshKeyPassword(),
                            command);
                    case NO_AUTH -> result = RemoteSSHUtils.executeCommand(
                            hostname, hostDTO.getSshPort(), hostDTO.getSshUser(), command);
                }

                if (result.getExitCode() != 0) {
                    log.error("Unable to connect to host, hostname: {}, msg: {}", hostname, result.getErrMsg());
                    throw new ApiException(ApiExceptionEnum.HOST_UNABLE_TO_CONNECT, hostname);
                } else {
                    log.info("Successfully connected to host, hostname: {}, res: {}", hostname, result.getOutput());
                }
            } catch (Exception e) {
                log.error("Unable to connect to host, hostname: {}", hostname, e);
                throw new ApiException(ApiExceptionEnum.HOST_UNABLE_TO_CONNECT, hostname);
            }
        }

        return true;
    }
}
