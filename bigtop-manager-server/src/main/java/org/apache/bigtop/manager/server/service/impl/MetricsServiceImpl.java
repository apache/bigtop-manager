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

import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.query.ComponentQuery;
import org.apache.bigtop.manager.dao.repository.ComponentDao;
import org.apache.bigtop.manager.dao.repository.HostDao;
import org.apache.bigtop.manager.server.proxy.PrometheusProxy;
import org.apache.bigtop.manager.server.service.MetricsService;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class MetricsServiceImpl implements MetricsService {

    @Resource
    private HostDao hostDao;

    @Resource
    private ComponentDao componentDao;

    @Override
    public JsonNode queryAgentsHealthyStatus() {
        PrometheusProxy proxy = getProxy();
        if (proxy == null) {
            return new ObjectMapper().createObjectNode();
        }

        return proxy.queryAgentsHealthyStatus();
    }

    @Override
    public JsonNode queryAgentsInfo(Long id, String interval) {
        PrometheusProxy proxy = getProxy();
        if (proxy == null) {
            return new ObjectMapper().createObjectNode();
        }

        String ipv4 = hostDao.findById(id).getIpv4();
        return proxy.queryAgentsInfo(ipv4, interval);
    }

    @Override
    public JsonNode queryClustersInfo(Long clusterId, String interval) {
        PrometheusProxy proxy = getProxy();
        if (proxy == null) {
            return new ObjectMapper().createObjectNode();
        }

        List<String> ipv4s = hostDao.findAllByClusterId(clusterId).stream()
                .map(HostPO::getIpv4)
                .toList();
        return proxy.queryClustersInfo(ipv4s, interval);
    }

    private PrometheusProxy getProxy() {
        ComponentQuery query =
                ComponentQuery.builder().name("prometheus_server").build();
        List<ComponentPO> componentPOList = componentDao.findByQuery(query);
        if (componentPOList.isEmpty()) {
            return null;
        } else {
            ComponentPO componentPO = componentPOList.get(0);
            HostPO hostPO = hostDao.findById(componentPO.getHostId());
            return new PrometheusProxy(hostPO.getHostname());
        }
    }
}
