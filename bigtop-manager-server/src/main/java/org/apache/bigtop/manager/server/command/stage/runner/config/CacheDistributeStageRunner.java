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
package org.apache.bigtop.manager.server.command.stage.runner.config;

import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.message.entity.payload.CacheMessagePayload;
import org.apache.bigtop.manager.common.message.entity.pojo.ClusterInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.ComponentInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.RepoInfo;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.po.HostComponentPO;
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.po.RepoPO;
import org.apache.bigtop.manager.dao.po.ServiceConfigPO;
import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.dao.po.SettingPO;
import org.apache.bigtop.manager.dao.po.TaskPO;
import org.apache.bigtop.manager.dao.po.TypeConfigPO;
import org.apache.bigtop.manager.dao.repository.ClusterRepository;
import org.apache.bigtop.manager.dao.repository.ComponentRepository;
import org.apache.bigtop.manager.dao.repository.HostComponentRepository;
import org.apache.bigtop.manager.dao.repository.HostRepository;
import org.apache.bigtop.manager.dao.repository.RepoRepository;
import org.apache.bigtop.manager.dao.repository.ServiceConfigRepository;
import org.apache.bigtop.manager.dao.repository.ServiceRepository;
import org.apache.bigtop.manager.dao.repository.SettingRepository;
import org.apache.bigtop.manager.grpc.generated.CommandRequest;
import org.apache.bigtop.manager.grpc.generated.CommandType;
import org.apache.bigtop.manager.grpc.utils.ProtobufUtil;
import org.apache.bigtop.manager.server.command.stage.factory.StageType;
import org.apache.bigtop.manager.server.command.stage.runner.AbstractStageRunner;
import org.apache.bigtop.manager.server.model.converter.RepoConverter;
import org.apache.bigtop.manager.server.model.dto.PropertyDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.StackDTO;
import org.apache.bigtop.manager.server.utils.StackConfigUtils;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.apache.commons.lang3.tuple.ImmutablePair;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.bigtop.manager.common.constants.Constants.ALL_HOST_KEY;

@Slf4j
@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CacheDistributeStageRunner extends AbstractStageRunner {

    @Resource
    private ClusterRepository clusterRepository;

    @Resource
    private HostComponentRepository hostComponentRepository;

    @Resource
    private ServiceRepository serviceRepository;

    @Resource
    private ServiceConfigRepository serviceConfigRepository;

    @Resource
    private RepoRepository repoRepository;

    @Resource
    private SettingRepository settingRepository;

    @Resource
    private HostRepository hostRepository;

    @Resource
    private ComponentRepository componentRepository;

    private ClusterInfo clusterInfo;

    private Map<String, ComponentInfo> componentInfoMap;

    private Map<String, Map<String, Object>> serviceConfigMap;

    private Map<String, Set<String>> hostMap;

    private List<RepoInfo> repoList;

    private Map<String, Set<String>> userMap;

    private Map<String, Object> settingsMap;

    @Override
    public StageType getStageType() {
        return StageType.CACHE_DISTRIBUTE;
    }

    @Override
    public void beforeRunTask(TaskPO taskPO) {
        super.beforeRunTask(taskPO);

        // Generate task content before execute
        updateTask(taskPO);
    }

    private void updateTask(TaskPO taskPO) {
        if (stageContext.getClusterId() == null) {
            genEmptyCaches();
        } else {
            genCaches();
        }

        CommandRequest request = getMessage(taskPO.getHostname());
        taskPO.setContent(ProtobufUtil.toJson(request));

        taskRepository.save(taskPO);
    }

    private void genCaches() {
        ClusterPO clusterPO = clusterRepository.getReferenceById(stageContext.getClusterId());

        Long clusterId = clusterPO.getId();
        String clusterName = clusterPO.getClusterName();
        String stackName = clusterPO.getStackPO().getStackName();
        String stackVersion = clusterPO.getStackPO().getStackVersion();

        List<ServicePO> servicePOList = serviceRepository.findAllByClusterPOId(clusterId);
        List<ServiceConfigPO> serviceConfigPOList =
                serviceConfigRepository.findAllByClusterPOAndSelectedIsTrue(clusterPO);
        List<HostComponentPO> hostComponentPOList = hostComponentRepository.findAllByComponentPOClusterPOId(clusterId);
        List<RepoPO> repoPOList = repoRepository.findAllByClusterPO(clusterPO);
        Iterable<SettingPO> settings = settingRepository.findAll();
        List<HostPO> hostPOList = hostRepository.findAllByClusterPOId(clusterId);

        clusterInfo = new ClusterInfo();
        clusterInfo.setClusterName(clusterName);
        clusterInfo.setStackName(stackName);
        clusterInfo.setStackVersion(stackVersion);
        clusterInfo.setUserGroup(clusterPO.getUserGroup());
        clusterInfo.setRepoTemplate(clusterPO.getRepoTemplate());
        clusterInfo.setRoot(clusterPO.getRoot());
        clusterInfo.setPackages(List.of(clusterPO.getPackages().split(",")));

        serviceConfigMap = new HashMap<>();
        for (ServiceConfigPO serviceConfigPO : serviceConfigPOList) {
            for (TypeConfigPO typeConfigPO : serviceConfigPO.getConfigs()) {
                List<PropertyDTO> properties =
                        JsonUtils.readFromString(typeConfigPO.getPropertiesJson(), new TypeReference<>() {});
                String configMapStr = JsonUtils.writeAsString(StackConfigUtils.extractConfigMap(properties));

                if (serviceConfigMap.containsKey(serviceConfigPO.getServicePO().getServiceName())) {
                    serviceConfigMap
                            .get(serviceConfigPO.getServicePO().getServiceName())
                            .put(typeConfigPO.getTypeName(), configMapStr);
                } else {
                    Map<String, Object> hashMap = new HashMap<>();
                    hashMap.put(typeConfigPO.getTypeName(), configMapStr);
                    serviceConfigMap.put(serviceConfigPO.getServicePO().getServiceName(), hashMap);
                }
            }
        }

        hostMap = new HashMap<>();
        hostComponentPOList.forEach(x -> {
            if (hostMap.containsKey(x.getComponentPO().getComponentName())) {
                hostMap.get(x.getComponentPO().getComponentName())
                        .add(x.getHostPO().getHostname());
            } else {
                Set<String> set = new HashSet<>();
                set.add(x.getHostPO().getHostname());
                hostMap.put(x.getComponentPO().getComponentName(), set);
            }
            hostMap.get(x.getComponentPO().getComponentName()).add(x.getHostPO().getHostname());
        });

        Set<String> hostNameSet = hostPOList.stream().map(HostPO::getHostname).collect(Collectors.toSet());
        hostMap.put(ALL_HOST_KEY, hostNameSet);

        repoList = new ArrayList<>();
        repoPOList.forEach(repoPO -> {
            RepoInfo repoInfo = RepoConverter.INSTANCE.fromPO2Message(repoPO);
            repoList.add(repoInfo);
        });

        userMap = new HashMap<>();
        servicePOList.forEach(x -> userMap.put(x.getServiceUser(), Set.of(x.getServiceGroup())));

        settingsMap = new HashMap<>();
        settings.forEach(x -> settingsMap.put(x.getTypeName(), x.getConfigData()));

        componentInfoMap = new HashMap<>();
        List<ComponentPO> componentPOList = componentRepository.findAll();
        componentPOList.forEach(c -> {
            ComponentInfo componentInfo = new ComponentInfo();
            componentInfo.setComponentName(c.getComponentName());
            componentInfo.setCommandScript(c.getCommandScript());
            componentInfo.setDisplayName(c.getDisplayName());
            componentInfo.setCategory(c.getCategory());
            componentInfo.setCustomCommands(c.getCustomCommands());
            componentInfo.setServiceName(c.getServicePO().getServiceName());
            componentInfoMap.put(c.getComponentName(), componentInfo);
        });
    }

    private void genEmptyCaches() {
        componentInfoMap = new HashMap<>();
        serviceConfigMap = new HashMap<>();
        hostMap = new HashMap<>();
        userMap = new HashMap<>();
        settingsMap = new HashMap<>();

        String fullStackName = StackUtils.fullStackName(stageContext.getStackName(), stageContext.getStackVersion());
        ImmutablePair<StackDTO, List<ServiceDTO>> immutablePair =
                StackUtils.getStackKeyMap().get(fullStackName);
        StackDTO stackDTO = immutablePair.getLeft();
        List<ServiceDTO> serviceDTOList = immutablePair.getRight();

        repoList = RepoConverter.INSTANCE.fromDTO2Message(stageContext.getRepoInfoList());
        clusterInfo = new ClusterInfo();
        clusterInfo.setClusterName(stageContext.getClusterName());
        clusterInfo.setStackName(stageContext.getStackName());
        clusterInfo.setStackVersion(stageContext.getStackVersion());
        clusterInfo.setUserGroup(stackDTO.getUserGroup());
        clusterInfo.setRepoTemplate(stackDTO.getRepoTemplate());
        clusterInfo.setRoot(stackDTO.getRoot());

        List<String> hostnames = stageContext.getHostnames();
        hostMap.put(Constants.ALL_HOST_KEY, new HashSet<>(hostnames));

        for (ServiceDTO serviceDTO : serviceDTOList) {
            userMap.put(serviceDTO.getServiceUser(), Set.of(serviceDTO.getServiceGroup()));
        }
    }

    private CommandRequest getMessage(String hostname) {
        CacheMessagePayload messagePayload = new CacheMessagePayload();
        messagePayload.setHostname(hostname);
        messagePayload.setClusterInfo(clusterInfo);
        messagePayload.setConfigurations(serviceConfigMap);
        messagePayload.setClusterHostInfo(hostMap);
        messagePayload.setRepoInfo(repoList);
        messagePayload.setSettings(settingsMap);
        messagePayload.setUserInfo(userMap);
        messagePayload.setComponentInfo(componentInfoMap);

        CommandRequest.Builder builder = CommandRequest.newBuilder();
        builder.setType(CommandType.CACHE_DISTRIBUTE);
        builder.setHostname(hostname);
        builder.setPayload(JsonUtils.writeAsString(messagePayload));

        return builder.build();
    }
}
