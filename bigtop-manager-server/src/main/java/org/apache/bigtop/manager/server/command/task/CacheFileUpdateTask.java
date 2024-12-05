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

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.common.message.entity.payload.CacheMessagePayload;
import org.apache.bigtop.manager.common.message.entity.pojo.ClusterInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.ComponentInfo;
import org.apache.bigtop.manager.common.message.entity.pojo.RepoInfo;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.po.RepoPO;
import org.apache.bigtop.manager.dao.po.ServiceConfigPO;
import org.apache.bigtop.manager.dao.query.ComponentQuery;
import org.apache.bigtop.manager.dao.repository.ClusterDao;
import org.apache.bigtop.manager.dao.repository.ComponentDao;
import org.apache.bigtop.manager.dao.repository.HostDao;
import org.apache.bigtop.manager.dao.repository.RepoDao;
import org.apache.bigtop.manager.dao.repository.ServiceConfigDao;
import org.apache.bigtop.manager.grpc.generated.CommandRequest;
import org.apache.bigtop.manager.grpc.generated.CommandType;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.converter.RepoConverter;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.StackDTO;
import org.apache.bigtop.manager.server.utils.StackUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.bigtop.manager.common.constants.Constants.ALL_HOST_KEY;

public class CacheFileUpdateTask extends AbstractTask {

    private ClusterDao clusterDao;
    private ServiceConfigDao serviceConfigDao;
    private RepoDao repoDao;
    private HostDao hostDao;
    private ComponentDao componentDao;

    private ClusterInfo clusterInfo;
    private Map<String, ComponentInfo> componentInfoMap;
    private Map<String, Map<String, Object>> serviceConfigMap;
    private Map<String, Set<String>> hostMap;
    private List<RepoInfo> repoList;
    private Map<String, String> userMap;

    public CacheFileUpdateTask(TaskContext taskContext) {
        super(taskContext);
    }

    @Override
    protected void injectBeans() {
        super.injectBeans();

        this.clusterDao = SpringContextHolder.getBean(ClusterDao.class);
        this.serviceConfigDao = SpringContextHolder.getBean(ServiceConfigDao.class);
        this.repoDao = SpringContextHolder.getBean(RepoDao.class);
        this.hostDao = SpringContextHolder.getBean(HostDao.class);
        this.componentDao = SpringContextHolder.getBean(ComponentDao.class);
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

    @SuppressWarnings("unchecked")
    private void genFullCaches() {
        Long clusterId = taskContext.getClusterId();
        List<Long> hostIds = (List<Long>) taskContext.getProperties().get("hostIds");
        ClusterPO clusterPO = clusterDao.findById(clusterId);

        ComponentQuery componentQuery =
                ComponentQuery.builder().clusterId(clusterId).build();

        List<ServiceConfigPO> serviceConfigPOList = serviceConfigDao.findByClusterId(clusterPO.getId());
        List<ComponentPO> componentPOList = componentDao.findByQuery(componentQuery);
        List<RepoPO> repoPOList = repoDao.findAll();
        List<HostPO> hostPOList = hostDao.findByIds(hostIds);

        clusterInfo = new ClusterInfo();
        clusterInfo.setUserGroup(clusterPO.getUserGroup());
        clusterInfo.setRootDir(clusterPO.getRootDir());

        serviceConfigMap = new HashMap<>();
        for (ServiceConfigPO serviceConfigPO : serviceConfigPOList) {
            List<Map<String, Object>> properties = JsonUtils.readFromString(serviceConfigPO.getPropertiesJson());
            Map<String, String> kvMap = properties.stream()
                    .collect(Collectors.toMap(
                            property -> (String) property.get("name"), property -> (String) property.get("value")));
            String kvString = JsonUtils.writeAsString(kvMap);

            if (serviceConfigMap.containsKey(serviceConfigPO.getServiceName())) {
                serviceConfigMap.get(serviceConfigPO.getServiceName()).put(serviceConfigPO.getName(), kvString);
            } else {
                Map<String, Object> hashMap = new HashMap<>();
                hashMap.put(serviceConfigPO.getName(), kvString);
                serviceConfigMap.put(serviceConfigPO.getServiceName(), hashMap);
            }
        }

        hostMap = new HashMap<>();
        componentPOList.forEach(x -> {
            if (hostMap.containsKey(x.getName())) {
                hostMap.get(x.getName()).add(x.getHostname());
            } else {
                Set<String> set = new HashSet<>();
                set.add(x.getHostname());
                hostMap.put(x.getName(), set);
            }
            hostMap.get(x.getName()).add(x.getHostname());
        });

        Set<String> hostNameSet = hostPOList.stream().map(HostPO::getHostname).collect(Collectors.toSet());
        hostMap.put(ALL_HOST_KEY, hostNameSet);

        repoList = new ArrayList<>();
        repoPOList.forEach(repoPO -> {
            RepoInfo repoInfo = RepoConverter.INSTANCE.fromPO2Message(repoPO);
            repoList.add(repoInfo);
        });

        userMap = new HashMap<>();
        for (StackDTO stackDTO : StackUtils.getAllStacks()) {
            for (ServiceDTO serviceDTO : StackUtils.getServiceDTOList(stackDTO)) {
                userMap.put(serviceDTO.getName(), serviceDTO.getUser());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void genEmptyCaches() {
        List<Long> hostIds = (List<Long>) taskContext.getProperties().get("hostIds");

        List<RepoPO> repoPOList = repoDao.findAll();
        List<HostPO> hostPOList = hostDao.findByIds(hostIds);

        componentInfoMap = new HashMap<>();
        serviceConfigMap = new HashMap<>();

        clusterInfo = new ClusterInfo();
        clusterInfo.setUserGroup(taskContext.getUserGroup());
        clusterInfo.setRootDir(taskContext.getRootDir());

        hostMap = new HashMap<>();
        Set<String> hostNameSet = hostPOList.stream().map(HostPO::getHostname).collect(Collectors.toSet());
        hostMap.put(ALL_HOST_KEY, hostNameSet);

        repoList = new ArrayList<>();
        repoPOList.forEach(repoPO -> {
            RepoInfo repoInfo = RepoConverter.INSTANCE.fromPO2Message(repoPO);
            repoList.add(repoInfo);
        });

        userMap = new HashMap<>();
        for (StackDTO stackDTO : StackUtils.getAllStacks()) {
            for (ServiceDTO serviceDTO : StackUtils.getServiceDTOList(stackDTO)) {
                userMap.put(serviceDTO.getName(), serviceDTO.getUser());
            }
        }
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
        messagePayload.setClusterInfo(clusterInfo);
        messagePayload.setConfigurations(serviceConfigMap);
        messagePayload.setClusterHostInfo(hostMap);
        messagePayload.setRepoInfo(repoList);
        messagePayload.setUserInfo(userMap);
        messagePayload.setComponentInfo(componentInfoMap);

        CommandRequest.Builder builder = CommandRequest.newBuilder();
        builder.setType(CommandType.UPDATE_CACHE_FILES);
        builder.setPayload(JsonUtils.writeAsString(messagePayload));

        return builder.build();
    }

    @Override
    public String getName() {
        return "Update cache files on " + taskContext.getHostDTO().getHostname();
    }
}
