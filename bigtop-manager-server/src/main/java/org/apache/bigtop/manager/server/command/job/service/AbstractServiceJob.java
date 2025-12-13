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
package org.apache.bigtop.manager.server.command.job.service;

import org.apache.bigtop.manager.dao.po.ComponentPO;
import org.apache.bigtop.manager.dao.query.ComponentQuery;
import org.apache.bigtop.manager.dao.repository.ComponentDao;
import org.apache.bigtop.manager.dao.repository.HostDao;
import org.apache.bigtop.manager.dao.repository.ServiceConfigDao;
import org.apache.bigtop.manager.dao.repository.ServiceConfigSnapshotDao;
import org.apache.bigtop.manager.dao.repository.ServiceDao;
import org.apache.bigtop.manager.server.command.job.AbstractJob;
import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.dto.ServiceDTO;
import org.apache.bigtop.manager.server.model.dto.command.ServiceCommandDTO;
import org.apache.bigtop.manager.server.utils.StackUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public abstract class AbstractServiceJob extends AbstractJob {

    protected ServiceDao serviceDao;
    protected ServiceConfigDao serviceConfigDao;
    protected ServiceConfigSnapshotDao serviceConfigSnapshotDao;
    protected ComponentDao componentDao;
    protected HostDao hostDao;

    public AbstractServiceJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    protected void injectBeans() {
        super.injectBeans();

        this.serviceDao = SpringContextHolder.getBean(ServiceDao.class);
        this.serviceConfigDao = SpringContextHolder.getBean(ServiceConfigDao.class);
        this.serviceConfigSnapshotDao = SpringContextHolder.getBean(ServiceConfigSnapshotDao.class);
        this.componentDao = SpringContextHolder.getBean(ComponentDao.class);
        this.hostDao = SpringContextHolder.getBean(HostDao.class);
    }

    @Override
    protected void beforeCreateStages() {
        super.beforeCreateStages();
    }

    protected Map<String, List<String>> getComponentHostsMap() {
        List<String> serviceNames = getServiceNames();
        ComponentQuery componentQuery = ComponentQuery.builder()
                .clusterId(clusterPO == null ? 0L : clusterPO.getId())
                .serviceNames(serviceNames)
                .build();
        List<ComponentPO> componentPOList = componentDao.findByQuery(componentQuery);
        Map<String, List<String>> componentHostsMap = new HashMap<>();
        for (ComponentPO componentPO : componentPOList) {
            List<String> hosts = componentHostsMap.computeIfAbsent(componentPO.getName(), k -> new ArrayList<>());
            hosts.add(componentPO.getHostname());
        }

        return componentHostsMap;
    }

    /**
     * Filter component-hosts map for a single service by component membership.
     */
    protected Map<String, List<String>> filterComponentHostsByService(Map<String, List<String>> all, String serviceName) {
        Map<String, List<String>> result = new HashMap<>();
        ServiceDTO serviceDTO = StackUtils.getServiceDTO(serviceName);
        // Collect component names for this service
        List<String> componentNames = serviceDTO.getComponents().stream().map(c -> c.getName().toLowerCase()).toList();
        for (Map.Entry<String, List<String>> entry : all.entrySet()) {
            String comp = entry.getKey();
            if (componentNames.contains(comp.toLowerCase())) {
                result.put(comp, entry.getValue());
            }
        }
        return result;
    }

    /**
     * Order services per required-services in metainfo.xml.
     * For Start/Add: required services first; For Stop: reverse (dependents first).
     */
    protected List<String> getOrderedServiceNamesForCommand(org.apache.bigtop.manager.common.enums.Command command) {
        List<String> services = getServiceNames();
        // Build graph: edge from required -> service
        Map<String, List<String>> graph = new HashMap<>();
        Map<String, Integer> indegree = new HashMap<>();
        for (String s : services) {
            graph.putIfAbsent(s, new ArrayList<>());
            indegree.putIfAbsent(s, 0);
        }
        // Use transitive required services
        Map<String, List<String>> allReqMemo = new HashMap<>();
        for (String s : services) {
            List<String> allReq = getAllRequiredServices(s, allReqMemo);
            for (String r : allReq) {
                if (!services.contains(r)) {
                    // skip requirements not part of current command set
                    continue;
                }
                graph.computeIfAbsent(r, k -> new ArrayList<>()).add(s);
                indegree.put(s, indegree.getOrDefault(s, 0) + 1);
                indegree.putIfAbsent(r, indegree.getOrDefault(r, 0));
            }
        }
        // Kahn topological sort
        Queue<String> q = new LinkedList<>();
        for (Map.Entry<String, Integer> e : indegree.entrySet()) {
            if (e.getValue() == 0) q.add(e.getKey());
        }
        List<String> ordered = new ArrayList<>();
        while (!q.isEmpty()) {
            String u = q.poll();
            ordered.add(u);
            for (String v : graph.getOrDefault(u, List.of())) {
                indegree.put(v, indegree.get(v) - 1);
                if (indegree.get(v) == 0) q.add(v);
            }
        }
        // If cycle or missing, fall back preserving original order for remaining
        if (ordered.size() < services.size()) {
            HashSet<String> seen = new HashSet<>(ordered);
            for (String s : services) {
                if (!seen.contains(s)) ordered.add(s);
            }
        }
        // For STOP, reverse to stop dependents first
        if (command == org.apache.bigtop.manager.common.enums.Command.STOP) {
            List<String> reversed = new ArrayList<>();
            for (int i = ordered.size() - 1; i >= 0; i--) {
                reversed.add(ordered.get(i));
            }
            return reversed;
        }
        return ordered;
    }

    /**
     * Recursively collect transitive required services for a given service from stack metainfo.
     * Uses memoization and guards against cycles.
     */
    private List<String> getAllRequiredServices(String serviceName, Map<String, List<String>> memo) {
        if (memo.containsKey(serviceName)) {
            return memo.get(serviceName);
        }
        ServiceDTO dto = StackUtils.getServiceDTO(serviceName);
        List<String> direct = dto.getRequiredServices();
        if (direct == null) direct = List.of();
        HashSet<String> visited = new HashSet<>();
        List<String> result = new ArrayList<>();
        // DFS
        for (String dep : direct) {
            if (visited.add(dep)) {
                result.add(dep);
                // Recurse
                List<String> sub = getAllRequiredServices(dep, memo);
                for (String s : sub) {
                    if (visited.add(s)) {
                        result.add(s);
                    }
                }
            }
        }
        memo.put(serviceName, result);
        return result;
    }

    private List<String> getServiceNames() {
        return jobContext.getCommandDTO().getServiceCommands().stream()
                .map(ServiceCommandDTO::getServiceName)
                .toList();
    }
}
