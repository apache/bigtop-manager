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
import org.apache.bigtop.manager.common.message.entity.payload.CacheMessagePayload;
import org.apache.bigtop.manager.common.message.entity.pojo.ClusterInfo;
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
import org.apache.bigtop.manager.grpc.generated.JobCacheReply;
import org.apache.bigtop.manager.grpc.generated.JobCacheRequest;
import org.apache.bigtop.manager.grpc.generated.JobCacheServiceGrpc;
import org.apache.bigtop.manager.server.exception.ServerException;
import org.apache.bigtop.manager.server.grpc.GrpcClient;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.apache.bigtop.manager.common.constants.Constants.ALL_HOST_KEY;

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

    public static void saveJobCache(Long clusterId, Long jobId, List<String> hostnames) {
        CacheMessagePayload payload = genPayload(clusterId);
        JobCacheRequest request = JobCacheRequest.newBuilder()
                .setJobId(jobId)
                .setPayload(JsonUtils.writeAsString(payload))
                .build();
        List<HostPO> hostPOList = hostDao.findAllByHostnames(hostnames);
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        for (HostPO hostPO : hostPOList) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                JobCacheServiceGrpc.JobCacheServiceBlockingStub stub = GrpcClient.getBlockingStub(
                        hostPO.getHostname(),
                        hostPO.getGrpcPort(),
                        JobCacheServiceGrpc.JobCacheServiceBlockingStub.class);
                JobCacheReply reply = stub.save(request);
                return reply != null && reply.getCode() == MessageConstants.SUCCESS_CODE;
            }));
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
            throw new ServerException("Failed to save job cache");
        }
    }

    private static CacheMessagePayload genPayload(Long clusterId) {
        if (!INITIALIZED.get()) {
            initialize();
        }

        ClusterPO clusterPO = clusterDao.findById(clusterId);

        ComponentQuery componentQuery =
                ComponentQuery.builder().clusterId(clusterId).build();

        List<ServiceConfigPO> serviceConfigPOList = serviceConfigDao.findByClusterId(clusterPO.getId());
        List<ComponentPO> componentPOList = componentDao.findByQuery(componentQuery);
        List<RepoPO> repoPOList = repoDao.findAll();
        List<HostPO> hostPOList = hostDao.findAll();

        ClusterInfo clusterInfo = new ClusterInfo();
        clusterInfo.setName(clusterPO.getName());
        clusterInfo.setUserGroup(clusterPO.getUserGroup());
        clusterInfo.setRootDir(clusterPO.getRootDir());

        Map<String, Map<String, Object>> serviceConfigMap = new HashMap<>();
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

        Map<String, Set<String>> hostMap = new HashMap<>();
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

        List<RepoInfo> repoList = new ArrayList<>();
        repoPOList.forEach(repoPO -> {
            RepoInfo repoInfo = RepoConverter.INSTANCE.fromPO2Message(repoPO);
            repoList.add(repoInfo);
        });

        Map<String, String> userMap = new HashMap<>();
        for (StackDTO stackDTO : StackUtils.getAllStacks()) {
            for (ServiceDTO serviceDTO : StackUtils.getServiceDTOList(stackDTO)) {
                userMap.put(serviceDTO.getName(), serviceDTO.getUser());
            }
        }

        CacheMessagePayload payload = new CacheMessagePayload();
        payload.setClusterInfo(clusterInfo);
        payload.setConfigurations(serviceConfigMap);
        payload.setClusterHostInfo(hostMap);
        payload.setRepoInfo(repoList);
        payload.setUserInfo(userMap);
        return payload;
    }
}
