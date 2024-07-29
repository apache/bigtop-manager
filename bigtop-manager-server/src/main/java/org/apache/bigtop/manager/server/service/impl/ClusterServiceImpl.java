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
import org.apache.bigtop.manager.dao.entity.Cluster;
import org.apache.bigtop.manager.dao.entity.Repo;
import org.apache.bigtop.manager.dao.entity.Stack;
import org.apache.bigtop.manager.dao.repository.ClusterRepository;
import org.apache.bigtop.manager.dao.repository.RepoRepository;
import org.apache.bigtop.manager.dao.repository.StackRepository;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.model.converter.ClusterConverter;
import org.apache.bigtop.manager.server.model.converter.RepoConverter;
import org.apache.bigtop.manager.server.model.dto.ClusterDTO;
import org.apache.bigtop.manager.server.model.dto.StackDTO;
import org.apache.bigtop.manager.server.model.vo.ClusterVO;
import org.apache.bigtop.manager.server.service.ClusterService;
import org.apache.bigtop.manager.server.service.HostService;
import org.apache.bigtop.manager.server.utils.StackUtils;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@org.springframework.stereotype.Service
public class ClusterServiceImpl implements ClusterService {

    @Resource
    private ClusterRepository clusterRepository;

    @Resource
    private RepoRepository repoRepository;

    @Resource
    private StackRepository stackRepository;

    @Resource
    private HostService hostService;

    @Override
    public List<ClusterVO> list() {
        List<ClusterVO> clusterVOList = new ArrayList<>();
        clusterRepository.findAll().forEach(cluster -> {
            ClusterVO clusterVO = ClusterConverter.INSTANCE.fromEntity2VO(cluster);
            clusterVOList.add(clusterVO);
        });

        return clusterVOList;
    }

    @Override
    public ClusterVO save(ClusterDTO clusterDTO) {
        // Save cluster
        Stack stack =
                stackRepository.findByStackNameAndStackVersion(clusterDTO.getStackName(), clusterDTO.getStackVersion());
        StackDTO stackDTO = StackUtils.getStackKeyMap()
                .get(StackUtils.fullStackName(clusterDTO.getStackName(), clusterDTO.getStackVersion()))
                .getLeft();
        Cluster cluster = ClusterConverter.INSTANCE.fromDTO2Entity(clusterDTO, stackDTO, stack);
        cluster.setSelected(clusterRepository.count() == 0);
        cluster.setState(MaintainState.UNINSTALLED);

        Cluster oldCluster =
                clusterRepository.findByClusterName(clusterDTO.getClusterName()).orElse(new Cluster());
        if (oldCluster.getId() != null) {
            cluster.setId(oldCluster.getId());
        }
        clusterRepository.save(cluster);

        hostService.batchSave(cluster.getId(), clusterDTO.getHostnames());

        // Save repo
        List<Repo> repos = RepoConverter.INSTANCE.fromDTO2Entity(clusterDTO.getRepoInfoList(), cluster);
        List<Repo> oldRepos = repoRepository.findAllByCluster(cluster);

        for (Repo repo : repos) {
            for (Repo oldRepo : oldRepos) {
                if (oldRepo.getArch().equals(repo.getArch()) && oldRepo.getOs().equals(repo.getOs())) {
                    repo.setId(oldRepo.getId());
                }
            }
        }

        repoRepository.saveAll(repos);
        return ClusterConverter.INSTANCE.fromEntity2VO(cluster);
    }

    @Override
    public ClusterVO get(Long id) {
        Cluster cluster =
                clusterRepository.findById(id).orElseThrow(() -> new ApiException(ApiExceptionEnum.CLUSTER_NOT_FOUND));

        return ClusterConverter.INSTANCE.fromEntity2VO(cluster);
    }

    @Override
    public ClusterVO update(Long id, ClusterDTO clusterDTO) {
        Cluster cluster = ClusterConverter.INSTANCE.fromDTO2Entity(clusterDTO);
        cluster.setId(id);
        clusterRepository.save(cluster);

        return ClusterConverter.INSTANCE.fromEntity2VO(cluster);
    }
}
