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

import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.po.HostPO;
import org.apache.bigtop.manager.dao.po.ServiceConfigPO;
import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.dao.query.ComponentQuery;
import org.apache.bigtop.manager.dao.repository.ClusterDao;
import org.apache.bigtop.manager.dao.repository.ComponentDao;
import org.apache.bigtop.manager.dao.repository.HostDao;
import org.apache.bigtop.manager.dao.repository.ServiceConfigDao;
import org.apache.bigtop.manager.dao.repository.ServiceDao;
import org.apache.bigtop.manager.server.model.converter.ServiceConfigConverter;
import org.apache.bigtop.manager.server.model.dto.PropertyDTO;
import org.apache.bigtop.manager.server.model.dto.ServiceConfigDTO;
import org.apache.bigtop.manager.server.model.vo.ClusterMetricsVO;
import org.apache.bigtop.manager.server.model.vo.HostMetricsVO;
import org.apache.bigtop.manager.server.model.vo.ServiceMetricsVO;
import org.apache.bigtop.manager.server.prometheus.PrometheusProxy;
import org.apache.bigtop.manager.server.service.MetricsService;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class MetricsServiceImpl implements MetricsService {

    @Resource
    private ClusterDao clusterDao;

    @Resource
    private HostDao hostDao;

    @Resource
    private ComponentDao componentDao;

    @Resource
    private ServiceDao serviceDao;

    @Resource
    private ServiceConfigDao serviceConfigDao;

    @Override
    public HostMetricsVO hostMetrics(Long id, String interval) {
        PrometheusProxy proxy = getProxy();
        if (proxy == null) {
            return new HostMetricsVO();
        }

        String ipv4 = hostDao.findById(id).getIpv4();
        return proxy.queryHostMetrics(ipv4, interval);
    }

    @Override
    public ClusterMetricsVO clusterMetrics(Long clusterId, String interval) {
        PrometheusProxy proxy = getProxy();
        if (proxy == null) {
            return new ClusterMetricsVO();
        }

        List<String> ipv4s = hostDao.findAllByClusterId(clusterId).stream()
                .map(HostPO::getIpv4)
                .toList();
        return proxy.queryClusterMetrics(ipv4s, interval);
    }

    @Override
    public ServiceMetricsVO serviceMetrics(Long serviceId, String interval) {
        PrometheusProxy proxy = getProxy();
        if (proxy == null) {
            return new ServiceMetricsVO();
        }

        ServicePO servicePO = serviceDao.findById(serviceId);
        ClusterPO clusterPO = clusterDao.findById(servicePO.getClusterId());
        return proxy.queryServiceMetrics(clusterPO.getName(), servicePO.getName(), interval);
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
            ServiceConfigPO serviceConfigPO =
                    serviceConfigDao.findByServiceIdAndName(componentPO.getServiceId(), "prometheus");
            int port = 9090;
            ServiceConfigDTO serviceConfigDTO = ServiceConfigConverter.INSTANCE.fromPO2DTO(serviceConfigPO);
            for (PropertyDTO property : serviceConfigDTO.getProperties()) {
                if ("port".equals(property.getName())) {
                    port = Integer.parseInt(property.getValue());
                    if (port <= 0) {
                        log.warn("Invalid port {} for Prometheus server, using default port 9090", port);
                        port = 9090; // Default Prometheus port
                    }
                }
            }

            return new PrometheusProxy(hostPO.getHostname(), port);
        }
    }
}
