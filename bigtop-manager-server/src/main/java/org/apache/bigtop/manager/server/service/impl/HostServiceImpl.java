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
import org.apache.bigtop.manager.common.utils.InstanceUtils;
import org.apache.bigtop.manager.common.utils.ProjectPathUtils;
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.query.ComponentQuery;
import org.apache.bigtop.manager.dao.query.HostQuery;
import org.apache.bigtop.manager.dao.repository.ComponentDao;
import org.apache.bigtop.manager.dao.repository.HostDao;
import org.apache.bigtop.manager.dao.repository.RepoDao;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.enums.HealthyStatusEnum;
import org.apache.bigtop.manager.server.enums.HostAuthTypeEnum;
import org.apache.bigtop.manager.server.enums.InstalledStatusEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.model.converter.ComponentConverter;
import org.apache.bigtop.manager.server.model.converter.HostConverter;
import org.apache.bigtop.manager.server.model.dto.HostDTO;
import org.apache.bigtop.manager.server.model.query.PageQuery;
import org.apache.bigtop.manager.server.model.vo.ComponentVO;
import org.apache.bigtop.manager.server.model.vo.HostVO;
import org.apache.bigtop.manager.server.model.vo.InstalledStatusVO;
import org.apache.bigtop.manager.server.model.vo.PageVO;
import org.apache.bigtop.manager.server.service.HostService;
import org.apache.bigtop.manager.server.utils.PageUtils;
import org.apache.bigtop.manager.server.utils.RemoteSSHUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private HostDao hostDao;

    @Resource
    private ComponentDao componentDao;

    @Resource
    private RepoDao repoDao;

    private final List<InstalledStatusVO> installedStatus = new CopyOnWriteArrayList<>();

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    private static final Integer DEFAULT_GRPC_PORT = 8835;
    private static final Integer DEFAULT_SSH_PORT = 22;
    private static final String DEFAULT_AGENT_DIR = "/opt";

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
        setDefaultValues(hostDTO);
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
        HostPO hostPO = hostDao.findById(id);
        HostPO convertedHostPO = HostConverter.INSTANCE.fromDTO2PO(hostDTO);
        BeanUtils.copyProperties(convertedHostPO, hostPO, InstanceUtils.getNullProperties(convertedHostPO));
        switch (HostAuthTypeEnum.fromCode(hostPO.getAuthType())) {
            case PASSWORD -> {
                hostPO.setSshPassword(hostDTO.getSshPassword());
                hostPO.setSshKeyString(null);
                hostPO.setSshKeyFilename(null);
                hostPO.setSshKeyPassword(null);
            }
            case KEY -> {
                hostPO.setSshPassword(null);
                hostPO.setSshKeyString(hostDTO.getSshKeyString());
                hostPO.setSshKeyFilename(hostDTO.getSshKeyFilename());
                hostPO.setSshKeyPassword(hostDTO.getSshKeyPassword());
            }
            case NO_AUTH -> {
                hostPO.setSshPassword(null);
                hostPO.setSshKeyString(null);
                hostPO.setSshKeyFilename(null);
                hostPO.setSshKeyPassword(null);
            }
        }

        hostDao.updateById(hostPO);
        return get(id);
    }

    @Override
    public Boolean remove(Long id) {
        return batchRemove(List.of(id));
    }

    @Override
    public List<ComponentVO> components(Long id) {
        ComponentQuery query = ComponentQuery.builder().hostId(id).build();
        List<ComponentPO> componentPOList = componentDao.findByQuery(query);
        return ComponentConverter.INSTANCE.fromPO2VO(componentPOList);
    }

    @Override
    public Boolean batchRemove(List<Long> ids) {
        for (Long id : ids) {
            if (componentDao.countByHostId(id) > 0) {
                HostPO hostPO = hostDao.findById(id);
                throw new ApiException(ApiExceptionEnum.HOST_HAS_COMPONENTS, hostPO.getHostname());
            }
        }

        return hostDao.deleteByIds(ids);
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
        // Clear cache list
        installedStatus.clear();

        for (HostDTO hostDTO : hostDTOList) {
            setDefaultValues(hostDTO);
            for (String hostname : hostDTO.getHostnames()) {
                InstalledStatusVO installedStatusVO = new InstalledStatusVO();
                installedStatusVO.setHostname(hostname);
                installedStatusVO.setStatus(InstalledStatusEnum.INSTALLING);
                installedStatus.add(installedStatusVO);

                // Async install dependencies
                executorService.submit(() -> {
                    try {
                        installDependencies(hostDTO, hostname, installedStatusVO);
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

    @Override
    public Boolean startAgent(Long hostId) {
        return execAgentScript(hostId, "start");
    }

    @Override
    public Boolean stopAgent(Long hostId) {
        return execAgentScript(hostId, "stop");
    }

    @Override
    public Boolean restartAgent(Long hostId) {
        return execAgentScript(hostId, "restart");
    }

    @Override
    public Boolean checkDuplicate(HostDTO hostDTO) {
        List<HostPO> existsHostList = hostDao.findAllByHostnames(hostDTO.getHostnames());
        if (CollectionUtils.isNotEmpty(existsHostList)) {
            List<String> existsHostnames =
                    existsHostList.stream().map(HostPO::getHostname).toList();
            throw new ApiException(ApiExceptionEnum.HOST_ASSIGNED, String.join(",", existsHostnames));
        }
        return true;
    }

    public void installDependencies(HostDTO hostDTO, String hostname, InstalledStatusVO installedStatusVO) {
        String path = hostDTO.getAgentDir();
        String repoUrl = repoDao.findByName("agent").getBaseUrl();
        int grpcPort = hostDTO.getGrpcPort();

        String command;
        try {
            String script = ProjectPathUtils.getServerScriptPath() + File.separator + "setup-agent.sh";
            String content = Files.readString(Path.of(script));
            command = "cat << 'EOF' > ./setup-agent.sh\n" + content + "\nEOF\n" + "chmod +x ./setup-agent.sh";
            ShellResult result = execCommandOnRemoteHost(hostDTO, hostname, command);
            if (result.getExitCode() != MessageConstants.SUCCESS_CODE) {
                log.error("Unable to write agent script, hostname: {}, msg: {}", hostname, result);
                installedStatusVO.setStatus(InstalledStatusEnum.FAILED);
                installedStatusVO.setMessage(result.getErrMsg());
                return;
            }
        } catch (IOException e) {
            log.error("Unable to write agent script, hostname: {}, msg: {}", hostname, e.getMessage());
            installedStatusVO.setStatus(InstalledStatusEnum.FAILED);
            installedStatusVO.setMessage(e.getMessage());
            return;
        }

        command = "./setup-agent.sh " + path + " " + repoUrl + " " + grpcPort;
        ShellResult result = execCommandOnRemoteHost(hostDTO, hostname, command);
        if (result.getExitCode() != MessageConstants.SUCCESS_CODE) {
            log.error("Unable to setup agent, hostname: {}, msg: {}", hostname, result);
            installedStatusVO.setStatus(InstalledStatusEnum.FAILED);
            installedStatusVO.setMessage(result.getErrMsg());
            return;
        }

        installedStatusVO.setStatus(InstalledStatusEnum.SUCCESS);
    }

    private Boolean execAgentScript(Long hostId, String action) {
        HostPO hostPO = hostDao.findById(hostId);
        if (hostPO == null) {
            throw new ApiException(ApiExceptionEnum.HOST_NOT_FOUND);
        }

        HostDTO hostDTO = HostConverter.INSTANCE.fromPO2DTO(hostPO);
        String path = hostDTO.getAgentDir();
        String hostname = hostDTO.getHostname();
        int grpcPort = hostDTO.getGrpcPort();

        String command = path + "/bigtop-manager-agent/bin/agent.sh " + action;
        command = "export GRPC_PORT=" + grpcPort + " ; " + command;

        ShellResult result = execCommandOnRemoteHost(hostDTO, hostname, command);
        if (result.getExitCode() != MessageConstants.SUCCESS_CODE) {
            log.error("Unable to {} agent, hostname: {}, msg: {}", action, hostname, result);
            throw new ApiException(ApiExceptionEnum.OPERATION_FAILED);
        }

        // Update host status
        if (action.equals("stop")) {
            hostPO.setStatus(HealthyStatusEnum.UNHEALTHY.getCode());
        } else {
            hostPO.setStatus(HealthyStatusEnum.HEALTHY.getCode());
        }

        hostDao.updateById(hostPO);
        return true;
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

    private void setDefaultValues(HostDTO hostDTO) {
        if (hostDTO == null) {
            return;
        }
        if (hostDTO.getGrpcPort() == null) {
            hostDTO.setGrpcPort(DEFAULT_GRPC_PORT);
        }
        if (hostDTO.getSshPort() == null) {
            hostDTO.setSshPort(DEFAULT_SSH_PORT);
        }
        if (StringUtils.isBlank(hostDTO.getAgentDir())) {
            hostDTO.setAgentDir(DEFAULT_AGENT_DIR);
        }
    }
}
