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
package org.apache.bigtop.manager.server.command.helper;

import org.apache.bigtop.manager.common.constants.MessageConstants;
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
import org.apache.bigtop.manager.grpc.generated.JobCacheReply;
import org.apache.bigtop.manager.grpc.generated.JobCacheRequest;
import org.apache.bigtop.manager.grpc.generated.JobCacheServiceGrpc;
import org.apache.bigtop.manager.grpc.payload.JobCachePayload;
import org.apache.bigtop.manager.grpc.pojo.ClusterInfo;
import org.apache.bigtop.manager.grpc.pojo.RepoInfo;
import org.apache.bigtop.manager.server.exception.ServerException;
import org.apache.bigtop.manager.server.grpc.GrpcClient;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.StackDTO;
import org.apache.bigtop.manager.server.utils.StackUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class JobCacheHelper {

    private static ClusterDao clusterDao;
    private static ServiceConfigDao serviceConfigDao;
    private static RepoDao repoDao;
    private static HostDao hostDao;
    private static ComponentDao componentDao;

    private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

    private static void initialize() {
        clusterDao = SpringContextHolder.getBean(ClusterDao.class);
        serviceConfigDao = SpringContextHolder.getBean(ServiceConfigDao.class);
        repoDao = SpringContextHolder.getBean(RepoDao.class);
        hostDao = SpringContextHolder.getBean(HostDao.class);
        componentDao = SpringContextHolder.getBean(ComponentDao.class);

        INITIALIZED.set(true);
    }

    public static void sendJobCache(Long jobId, List<String> hostnames) {
        if (!INITIALIZED.get()) {
            initialize();
        }

        JobCachePayload payload = new JobCachePayload();
        genGlobalPayload(payload);

        // Sort by cluster id to avoid regenerating the same cluster payload
        List<HostPO> hostPOList = hostDao.findAllByHostnames(hostnames);
        hostPOList.sort(Comparator.comparing(HostPO::getClusterId));

        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (HostPO hostPO : hostPOList) {
            payload.setCurrentClusterId(hostPO.getClusterId());

            List<Long> clusterIds = new ArrayList<>();
            if (hostRequiresAllData(hostPO.getHostname())) {
                clusterIds.addAll(
                        clusterDao.findAll().stream().map(ClusterPO::getId).toList());
            } else {
                clusterIds.add(hostPO.getClusterId());
            }

            for (Long clusterId : clusterIds) {
                JobCachePayload copiedPayload =
                        JsonUtils.readFromString(JsonUtils.writeAsString(payload), JobCachePayload.class);
                genClusterPayload(copiedPayload, clusterId);
                JobCacheRequest request = JobCacheRequest.newBuilder()
                        .setJobId(jobId)
                        .setPayload(JsonUtils.writeAsString(copiedPayload))
                        .build();
                futures.add(CompletableFuture.supplyAsync(() -> {
                    JobCacheServiceGrpc.JobCacheServiceBlockingStub stub = GrpcClient.getBlockingStub(
                            hostPO.getHostname(),
                            hostPO.getGrpcPort(),
                            JobCacheServiceGrpc.JobCacheServiceBlockingStub.class);
                    JobCacheReply reply = stub.save(request);
                    return reply != null && reply.getCode() == MessageConstants.SUCCESS_CODE;
                }));
            }
        }

        List<Boolean> results = futures.stream()
                .map((future) -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .toList();

        boolean allSuccess = results.stream().allMatch(Boolean::booleanValue);
        if (!allSuccess) {
            throw new ServerException("Failed to send job cache");
        }
    }

    private static void genClusterPayload(JobCachePayload payload, Long clusterId) {
        if (Objects.equals(payload.getClusterId(), clusterId)) {
            return;
        }

        ClusterPO clusterPO = clusterDao.findById(clusterId);

        ClusterInfo clusterInfo = new ClusterInfo();
        clusterInfo.setName(clusterPO.getName());
        clusterInfo.setUserGroup(clusterPO.getUserGroup());
        clusterInfo.setRootDir(clusterPO.getRootDir());

        Map<String, Map<String, String>> serviceConfigMap = payload.getConfigurations();
        serviceConfigMap.putAll(getServiceConfigMap(clusterId));

        Map<String, List<String>> componentHostMap = payload.getComponentHosts();
        componentHostMap.putAll(getComponentHostMap(clusterId));

        List<String> hosts = hostDao.findAllByClusterId(clusterId).stream()
                .map(HostPO::getHostname)
                .toList();

        payload.setClusterId(clusterId);
        payload.setClusterInfo(clusterInfo);
        payload.setConfigurations(serviceConfigMap);
        payload.setComponentHosts(componentHostMap);
        payload.setHosts(hosts);
    }

    private static void genGlobalPayload(JobCachePayload payload) {
        List<RepoPO> repoPOList = repoDao.findAll();
        Map<String, Map<String, String>> serviceConfigMap = getServiceConfigMap(0L);

        Map<String, List<String>> componentHostMap = new HashMap<>(getComponentHostMap(0L));

        List<RepoInfo> repoList = new ArrayList<>();
        repoPOList.forEach(repoPO -> {
            RepoInfo repoInfo = new RepoInfo();
            repoInfo.setName(repoPO.getName());
            repoInfo.setArch(repoPO.getArch());
            repoInfo.setBaseUrl(repoPO.getBaseUrl());
            repoInfo.setPkgName(repoPO.getPkgName());
            repoInfo.setChecksum(repoPO.getChecksum());
            repoInfo.setType(repoPO.getType());
            repoList.add(repoInfo);
        });

        Map<String, String> userMap = new HashMap<>();
        for (StackDTO stackDTO : StackUtils.getAllStacks()) {
            for (ServiceDTO serviceDTO : StackUtils.getServiceDTOList(stackDTO)) {
                userMap.put(serviceDTO.getName(), serviceDTO.getUser());
            }
        }

        payload.setConfigurations(serviceConfigMap);
        payload.setComponentHosts(componentHostMap);
        payload.setRepoInfo(repoList);
        payload.setUserInfo(userMap);
    }

    private static Map<String, Map<String, String>> getServiceConfigMap(Long clusterId) {
        List<ServiceConfigPO> serviceConfigPOList = serviceConfigDao.findByClusterId(clusterId);
        Map<String, Map<String, String>> serviceConfigMap = new HashMap<>();
        for (ServiceConfigPO serviceConfigPO : serviceConfigPOList) {
            List<Map<String, Object>> properties = JsonUtils.readFromString(serviceConfigPO.getPropertiesJson());
            Map<String, String> kvMap = properties.stream()
                    .collect(Collectors.toMap(
                            property -> (String) property.get("name"), property -> (String) property.get("value")));
            String kvString = JsonUtils.writeAsString(kvMap);

            if (serviceConfigMap.containsKey(serviceConfigPO.getServiceName())) {
                serviceConfigMap.get(serviceConfigPO.getServiceName()).put(serviceConfigPO.getName(), kvString);
            } else {
                Map<String, String> hashMap = new HashMap<>();
                hashMap.put(serviceConfigPO.getName(), kvString);
                serviceConfigMap.put(serviceConfigPO.getServiceName(), hashMap);
            }
        }

        return serviceConfigMap;
    }

    private static Map<String, List<String>> getComponentHostMap(Long clusterId) {
        ComponentQuery query = ComponentQuery.builder().clusterId(clusterId).build();
        List<ComponentPO> componentPOList = componentDao.findByQuery(query);
        Map<String, List<String>> hostMap = new HashMap<>();
        componentPOList.forEach(x -> {
            if (hostMap.containsKey(x.getName())) {
                hostMap.get(x.getName()).add(x.getHostname());
            } else {
                List<String> list = new ArrayList<>();
                list.add(x.getHostname());
                hostMap.put(x.getName(), list);
            }
        });

        return hostMap;
    }

    private static Boolean hostRequiresAllData(String hostname) {
        // Some services like prometheus requires all clusters info to collect metrics.
        List<ComponentPO> components = componentDao.findByQuery(
                ComponentQuery.builder().hostname(hostname).build());
        for (ComponentPO component : components) {
            ServiceDTO serviceDTO = StackUtils.getServiceDTOByComponentName(component.getName());
            StackDTO stack = StackUtils.getServiceStack(serviceDTO.getName());
            if (stack.getStackName().equals("infra")) {
                return true;
            }
        }

        return false;
    }
}
