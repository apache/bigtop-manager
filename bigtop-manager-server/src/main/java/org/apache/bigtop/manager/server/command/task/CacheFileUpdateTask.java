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
package org.apache.bigtop.manager.server.command.task;

import org.apache.bigtop.manager.common.constants.Constants;
import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.common.message.entity.payload.CacheMessagePayload;
import org.apache.bigtop.manager.common.message.entity.pojo.ClusterInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.ComponentInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.RepoInfo;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.mapper.ClusterMapper;
import org.apache.bigtop.manager.dao.mapper.ComponentMapper;
import org.apache.bigtop.manager.dao.mapper.HostComponentMapper;
import org.apache.bigtop.manager.dao.mapper.HostMapper;
import org.apache.bigtop.manager.dao.mapper.RepoMapper;
import org.apache.bigtop.manager.dao.mapper.ServiceConfigMapper;
import org.apache.bigtop.manager.dao.mapper.ServiceMapper;
import org.apache.bigtop.manager.dao.mapper.SettingMapper;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.po.HostComponentPO;
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.po.RepoPO;
import org.apache.bigtop.manager.dao.po.ServiceConfigPO;
import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.dao.po.SettingPO;
import org.apache.bigtop.manager.dao.po.TypeConfigPO;
import org.apache.bigtop.manager.grpc.generated.CommandRequest;
import org.apache.bigtop.manager.grpc.generated.CommandType;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.converter.RepoConverter;
import org.apache.bigtop.manager.server.model.dto.PropertyDTO;
import org.apache.bigtop.manager.server.model.dto.RepoDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.StackDTO;
import org.apache.bigtop.manager.server.utils.StackConfigUtils;
import org.apache.bigtop.manager.server.utils.StackUtils;

import org.apache.commons.lang3.tuple.ImmutablePair;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.bigtop.manager.common.constants.Constants.ALL_HOST_KEY;

public class CacheFileUpdateTask extends AbstractTask {

    private ClusterMapper clusterMapper;
    private HostComponentMapper hostComponentMapper;
    private ServiceMapper serviceMapper;
    private ServiceConfigMapper serviceConfigMapper;
    private RepoMapper repoMapper;
    private SettingMapper settingMapper;
    private HostMapper hostMapper;
    private ComponentMapper componentMapper;

    private ClusterInfo clusterInfo;
    private Map<String, ComponentInfo> componentInfoMap;
    private Map<String, Map<String, Object>> serviceConfigMap;
    private Map<String, Set<String>> hostMap;
    private List<RepoInfo> repoList;
    private Map<String, String> userMap;
    private Map<String, Object> settingsMap;

    public CacheFileUpdateTask(TaskContext taskContext) {
        super(taskContext);
    }

    @Override
    protected void injectBeans() {
        super.injectBeans();

        this.clusterMapper = SpringContextHolder.getBean(ClusterMapper.class);
        this.hostComponentMapper = SpringContextHolder.getBean(HostComponentMapper.class);
        this.serviceMapper = SpringContextHolder.getBean(ServiceMapper.class);
        this.serviceConfigMapper = SpringContextHolder.getBean(ServiceConfigMapper.class);
        this.repoMapper = SpringContextHolder.getBean(RepoMapper.class);
        this.settingMapper = SpringContextHolder.getBean(SettingMapper.class);
        this.hostMapper = SpringContextHolder.getBean(HostMapper.class);
        this.componentMapper = SpringContextHolder.getBean(ComponentMapper.class);
    }

    @Override
    public void beforeRun() {
        super.beforeRun();

        genCaches();
    }

    private void genCaches() {
        if (taskContext.getClusterId() == null) {
            genEmptyCaches();
        } else {
            genFullCaches();
        }
    }

    private void genFullCaches() {
        ClusterPO clusterPO = clusterMapper.findByIdJoin(taskContext.getClusterId());

        Long clusterId = clusterPO.getId();
        String clusterName = clusterPO.getClusterName();

        String stackName = clusterPO.getStackName();
        String stackVersion = clusterPO.getStackVersion();

        List<ServicePO> servicePOList = serviceMapper.findAllByClusterId(clusterId);
        List<ServiceConfigPO> serviceConfigPOList =
                serviceConfigMapper.findAllByClusterIdAndSelectedIsTrue(clusterPO.getId());
        List<HostComponentPO> hostComponentPOList = hostComponentMapper.findAllByClusterId(clusterId);
        List<RepoPO> repoPOList = repoMapper.findAllByClusterId(clusterPO.getId());
        Iterable<SettingPO> settings = settingMapper.findAll();
        List<HostPO> hostPOList = hostMapper.findAllByClusterId(clusterId);

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

                if (serviceConfigMap.containsKey(serviceConfigPO.getServiceName())) {
                    serviceConfigMap
                            .get(serviceConfigPO.getServiceName())
                            .put(typeConfigPO.getTypeName(), configMapStr);
                } else {
                    Map<String, Object> hashMap = new HashMap<>();
                    hashMap.put(typeConfigPO.getTypeName(), configMapStr);
                    serviceConfigMap.put(serviceConfigPO.getServiceName(), hashMap);
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
        servicePOList.forEach(x -> userMap.put(x.getServiceName(), x.getServiceUser()));

        settingsMap = new HashMap<>();
        settings.forEach(x -> settingsMap.put(x.getTypeName(), x.getConfigData()));

        componentInfoMap = new HashMap<>();
        List<ComponentPO> componentPOList = componentMapper.findAllJoinService();
        componentPOList.forEach(c -> {
            ComponentInfo componentInfo = new ComponentInfo();
            componentInfo.setComponentName(c.getComponentName());
            componentInfo.setCommandScript(c.getCommandScript());
            componentInfo.setDisplayName(c.getDisplayName());
            componentInfo.setCategory(c.getCategory());
            componentInfo.setCustomCommands(c.getCustomCommands());
            componentInfo.setServiceName(c.getServiceName());
            componentInfoMap.put(c.getComponentName(), componentInfo);
        });
    }

    @SuppressWarnings("unchecked")
    private void genEmptyCaches() {
        componentInfoMap = new HashMap<>();
        serviceConfigMap = new HashMap<>();
        hostMap = new HashMap<>();
        userMap = new HashMap<>();
        settingsMap = new HashMap<>();

        String fullStackName = StackUtils.fullStackName(taskContext.getStackName(), taskContext.getStackVersion());
        ImmutablePair<StackDTO, List<ServiceDTO>> immutablePair =
                StackUtils.getStackKeyMap().get(fullStackName);
        StackDTO stackDTO = immutablePair.getLeft();

        Map<String, Object> properties = taskContext.getProperties();

        repoList = RepoConverter.INSTANCE.fromDTO2Message((List<RepoDTO>) properties.get("repoInfoList"));
        clusterInfo = new ClusterInfo();
        clusterInfo.setClusterName(taskContext.getClusterName());
        clusterInfo.setStackName(taskContext.getStackName());
        clusterInfo.setStackVersion(taskContext.getStackVersion());
        clusterInfo.setUserGroup(stackDTO.getUserGroup());
        clusterInfo.setRepoTemplate(stackDTO.getRepoTemplate());
        clusterInfo.setRoot(stackDTO.getRoot());
        clusterInfo.setPackages(List.of(stackDTO.getPackages().split(",")));

        List<String> hostnames = (List<String>) properties.get("hostnames");
        hostMap.put(Constants.ALL_HOST_KEY, new HashSet<>(hostnames));
    }

    @Override
    protected Command getCommand() {
        return Command.CUSTOM;
    }

    @Override
    protected String getCustomCommand() {
        return "update_cache_files";
    }

    @Override
    protected CommandRequest getCommandRequest() {
        CacheMessagePayload messagePayload = new CacheMessagePayload();
        messagePayload.setHostname(taskContext.getHostname());
        messagePayload.setClusterInfo(clusterInfo);
        messagePayload.setConfigurations(serviceConfigMap);
        messagePayload.setClusterHostInfo(hostMap);
        messagePayload.setRepoInfo(repoList);
        messagePayload.setSettings(settingsMap);
        messagePayload.setUserInfo(userMap);
        messagePayload.setComponentInfo(componentInfoMap);

        CommandRequest.Builder builder = CommandRequest.newBuilder();
        builder.setType(CommandType.UPDATE_CACHE_FILES);
        builder.setHostname(taskContext.getHostname());
        builder.setPayload(JsonUtils.writeAsString(messagePayload));

        return builder.build();
    }

    @Override
    public String getName() {
        return "Update cache files on " + taskContext.getHostname();
    }
}
