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

import org.apache.bigtop.manager.common.enums.MaintainState;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.Host;
import org.apache.bigtop.manager.dao.repository.ClusterRepository;
import org.apache.bigtop.manager.dao.repository.HostRepository;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.grpc.GrpcClient;
import org.apache.bigtop.manager.server.model.converter.HostConverter;
import org.apache.bigtop.manager.server.model.dto.HostDTO;
import org.apache.bigtop.manager.server.model.vo.HostVO;
import org.apache.bigtop.manager.server.service.HostService;

import org.apache.commons.collections4.CollectionUtils;

import org.springframework.stereotype.Service;

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
    private ClusterRepository clusterRepository;

    @Resource
    private HostRepository hostRepository;

    @Override
    public List<HostVO> list(Long clusterId) {
        List<Host> hosts = hostRepository.findAllByClusterId(clusterId);
        if (CollectionUtils.isEmpty(hosts)) {
            throw new ApiException(ApiExceptionEnum.HOST_NOT_FOUND);
        }

        return HostConverter.INSTANCE.fromEntity2VO(hosts);
    }

    @Override
    public List<HostVO> batchSave(Long clusterId, List<String> hostnames) {
        ClusterPO clusterPO = clusterRepository.getReferenceById(clusterId);

        List<Host> hostnameIn = hostRepository.findAllByHostnameIn(hostnames);
        List<Host> hosts = new ArrayList<>();

        Map<String, Host> hostInMap = hostnameIn.stream().collect(Collectors.toMap(Host::getHostname, host -> host));

        for (String hostname : hostnames) {
            Host host = new Host();
            host.setHostname(hostname);
            host.setClusterPO(clusterPO);
            host.setState(MaintainState.INSTALLED);

            if (hostInMap.containsKey(hostname)) {
                host.setId(hostInMap.get(hostname).getId());
            }

            hosts.add(host);
        }

        hostRepository.saveAll(hosts);

        return HostConverter.INSTANCE.fromEntity2VO(hosts);
    }

    @Override
    public HostVO get(Long id) {
        Host host = hostRepository.findById(id).orElseThrow(() -> new ApiException(ApiExceptionEnum.HOST_NOT_FOUND));

        return HostConverter.INSTANCE.fromEntity2VO(host);
    }

    @Override
    public HostVO update(Long id, HostDTO hostDTO) {
        Host host = HostConverter.INSTANCE.fromDTO2Entity(hostDTO);
        host.setId(id);
        hostRepository.save(host);

        return HostConverter.INSTANCE.fromEntity2VO(host);
    }

    @Override
    public Boolean delete(Long id) {
        hostRepository.deleteById(id);
        return true;
    }

    @Override
    public Boolean checkConnection(List<String> hostnames) {
        for (String hostname : hostnames) {
            if (!GrpcClient.isChannelAlive(hostname)) {
                // An api exception will throw if connection fails to establish, we don't need to handle the return
                // value.
                GrpcClient.createChannel(hostname);
            }
        }

        return true;
    }
}
