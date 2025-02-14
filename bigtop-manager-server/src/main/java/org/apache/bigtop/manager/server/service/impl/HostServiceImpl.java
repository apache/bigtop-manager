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

import org.apache.bigtop.manager.common.constants.MessageConstants;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.po.RepoPO;
import org.apache.bigtop.manager.dao.query.HostQuery;
import org.apache.bigtop.manager.dao.repository.ComponentDao;
import org.apache.bigtop.manager.dao.repository.HostDao;
import org.apache.bigtop.manager.dao.repository.RepoDao;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.enums.HealthyStatusEnum;
import org.apache.bigtop.manager.server.enums.HostAuthTypeEnum;
import org.apache.bigtop.manager.server.enums.InstalledStatusEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.model.converter.HostConverter;
import org.apache.bigtop.manager.server.model.dto.HostDTO;
import org.apache.bigtop.manager.server.model.query.PageQuery;
import org.apache.bigtop.manager.server.model.vo.HostVO;
import org.apache.bigtop.manager.server.model.vo.InstalledStatusVO;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HostServiceImpl implements HostService {

    @Resource
    private RepoDao repoDao;

    @Resource
    private HostDao hostDao;

    @Resource
    private ComponentDao componentDao;

    private final List<InstalledStatusVO> installedStatus = new CopyOnWriteArrayList<>();

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

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
            hostPO.setStatus(HealthyStatusEnum.HEALTHY.getCode());
        }

        hostDao.saveAll(hostPOList);
        return HostConverter.INSTANCE.fromPO2VO(hostPOList);
    }

    @Override
    public List<HostVO> batchSave(Long clusterId, List<String> hostnames) {
        List<HostPO> hostnameIn = hostDao.findAllByHostnames(hostnames);
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
    public Boolean remove(Long id) {
        if (componentDao.countByHostId(id) > 0) {
            throw new ApiException(ApiExceptionEnum.HOST_HAS_COMPONENTS);
        }

        return hostDao.deleteById(id);
    }

    @Override
    public Boolean checkConnection(HostDTO hostDTO) {
        String command = "hostname";
        for (String hostname : hostDTO.getHostnames()) {
            try {
                ShellResult result = execCommandOnRemoteHost(hostDTO, hostname, command);
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

    @Override
    public Boolean installDependencies(List<HostDTO> hostDTOList) {
        List<RepoPO> repoPOList = repoDao.findAll();
        Map<String, RepoPO> archRepoMap = repoPOList.stream()
                .filter(repoPO -> repoPO.getType() == 2)
                .collect(Collectors.toMap(RepoPO::getArch, repo -> repo));

        // Clear cache list
        installedStatus.clear();

        for (HostDTO hostDTO : hostDTOList) {
            for (String hostname : hostDTO.getHostnames()) {
                InstalledStatusVO installedStatusVO = new InstalledStatusVO();
                installedStatusVO.setHostname(hostname);
                installedStatusVO.setStatus(InstalledStatusEnum.INSTALLING);
                installedStatus.add(installedStatusVO);

                // Async install dependencies
                executorService.submit(() -> {
                    try {
                        installDependencies(archRepoMap, hostDTO, hostname, installedStatusVO);
                    } catch (Exception e) {
                        log.error("Unable to install dependencies on host, hostname: {}", hostname, e);
                        installedStatusVO.setStatus(InstalledStatusEnum.FAILED);
                        installedStatusVO.setMessage(e.getMessage());
                    }
                });
            }
        }

        return true;
    }

    @Override
    public List<InstalledStatusVO> installedStatus() {
        return installedStatus;
    }

    public void installDependencies(
            Map<String, RepoPO> archRepoMap, HostDTO hostDTO, String hostname, InstalledStatusVO installedStatusVO) {
        String path = hostDTO.getAgentDir();
        // Get host arch
        String arch =
                execCommandOnRemoteHost(hostDTO, hostname, "arch").getOutput().trim();
        arch = arch.equals("arm64") ? "aarch64" : arch;

        // Download & Extract agent tarball
        String repoUrl = archRepoMap.get(arch).getBaseUrl();
        String tarballUrl = repoUrl + "/bigtop-manager-agent.tar.gz";
        String command = "sudo mkdir -p " + path + " &&"
                + " sudo chown -R " + hostDTO.getSshUser() + ":" + hostDTO.getSshUser() + " " + path
                + " && curl -L " + tarballUrl + " | tar -xz -C " + path;
        ShellResult result = execCommandOnRemoteHost(hostDTO, hostname, command);
        if (result.getExitCode() != MessageConstants.SUCCESS_CODE) {
            log.error(
                    "Unable to download & extract agent tarball, hostname: {}, msg: {}", hostname, result.getErrMsg());

            installedStatusVO.setStatus(InstalledStatusEnum.FAILED);
            installedStatusVO.setMessage(result.getErrMsg());
            return;
        }

        // Update agent conf
        // Current only grpc port needs to be updated if it's not default port
        if (hostDTO.getGrpcPort() != 8835) {
            command = "sed -i 's/port: 8835/port: " + hostDTO.getGrpcPort() + "/' " + path
                    + "/bigtop-manager-agent/conf/application.yml";
            result = execCommandOnRemoteHost(hostDTO, hostname, command);
            if (result.getExitCode() != MessageConstants.SUCCESS_CODE) {
                log.error("Unable to update agent config, hostname: {}, msg: {}", hostname, result.getErrMsg());

                installedStatusVO.setStatus(InstalledStatusEnum.FAILED);
                installedStatusVO.setMessage(result.getErrMsg());
                return;
            }
        }

        // Run agent in background
        command = "nohup " + path + "/bigtop-manager-agent/bin/start.sh --debug > /dev/null 2>&1 &";
        result = execCommandOnRemoteHost(hostDTO, hostname, command);
        if (result.getExitCode() != MessageConstants.SUCCESS_CODE) {
            log.error("Unable to start agent, hostname: {}, msg: {}", hostname, result.getErrMsg());

            installedStatusVO.setStatus(InstalledStatusEnum.FAILED);
            installedStatusVO.setMessage(result.getErrMsg());
            return;
        }

        // Check the process, the agent may encounter some errors and exit when starting
        // So we need to wait for a while before the check
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            log.error("Thread sleep interrupted", e);
        }
        command = "ps -ef | grep bigtop-manager-agent | grep -v grep";
        result = execCommandOnRemoteHost(hostDTO, hostname, command);
        if (result.getExitCode() != MessageConstants.SUCCESS_CODE
                || !result.getOutput().contains("bigtop-manager-agent")) {
            log.error("Unable to start agent process, hostname: {}", hostname);

            installedStatusVO.setStatus(InstalledStatusEnum.FAILED);
            installedStatusVO.setMessage("Unable to start agent, please check the log.");
            return;
        }

        installedStatusVO.setStatus(InstalledStatusEnum.SUCCESS);
    }

    private ShellResult execCommandOnRemoteHost(HostDTO hostDTO, String hostname, String command) {
        HostAuthTypeEnum authType = HostAuthTypeEnum.fromCode(hostDTO.getAuthType());
        try {
            return switch (authType) {
                case PASSWORD -> RemoteSSHUtils.executeCommand(
                        hostname, hostDTO.getSshPort(), hostDTO.getSshUser(), hostDTO.getSshPassword(), command);
                case KEY -> RemoteSSHUtils.executeCommand(
                        hostname,
                        hostDTO.getSshPort(),
                        hostDTO.getSshUser(),
                        hostDTO.getSshKeyFilename(),
                        hostDTO.getSshKeyString(),
                        hostDTO.getSshKeyPassword(),
                        command);
                case NO_AUTH -> RemoteSSHUtils.executeCommand(
                        hostname, hostDTO.getSshPort(), hostDTO.getSshUser(), command);
            };
        } catch (Exception e) {
            log.error("Unable to exec command on host, hostname: {}, command: {}", hostname, command, e);
            throw new RuntimeException(e);
        }
    }
}
