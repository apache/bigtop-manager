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
import org.apache.bigtop.manager.dao.mapper.ClusterMapper;
import org.apache.bigtop.manager.dao.mapper.RepoMapper;
import org.apache.bigtop.manager.dao.mapper.StackMapper;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.RepoPO;
import org.apache.bigtop.manager.dao.po.StackPO;
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

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ClusterServiceImpl implements ClusterService {

    @Resource
    private ClusterMapper clusterMapper;

    @Resource
    private RepoMapper repoMapper;

    @Resource
    private StackMapper stackMapper;

    @Resource
    private HostService hostService;

    @Override
    public List<ClusterVO> list() {
        List<ClusterVO> clusterVOList = new ArrayList<>();
        clusterMapper.findAll().forEach(cluster -> {
            ClusterVO clusterVO = ClusterConverter.INSTANCE.fromEntity2VO(cluster);
            clusterVOList.add(clusterVO);
        });

        return clusterVOList;
    }

    @Override
    public ClusterVO save(ClusterDTO clusterDTO) {
        // Save cluster
        StackPO stackPO =
                stackMapper.findByStackNameAndStackVersion(clusterDTO.getStackName(), clusterDTO.getStackVersion());
        StackDTO stackDTO = StackUtils.getStackKeyMap()
                .get(StackUtils.fullStackName(clusterDTO.getStackName(), clusterDTO.getStackVersion()))
                .getLeft();
        ClusterPO clusterPO = ClusterConverter.INSTANCE.fromDTO2PO(clusterDTO, stackDTO, stackPO);
        clusterPO.setSelected(clusterMapper.count() == 0);
        clusterPO.setState(MaintainState.UNINSTALLED);

        ClusterPO oldClusterPO =
                clusterMapper.findByClusterName(clusterDTO.getClusterName()).orElse(new ClusterPO());
        if (oldClusterPO.getId() != null) {
            clusterPO.setId(oldClusterPO.getId());
        }
        clusterMapper.save(clusterPO);

        hostService.batchSave(clusterPO.getId(), clusterDTO.getHostnames());

        // Save repo
        List<RepoPO> repoPOList = RepoConverter.INSTANCE.fromDTO2PO(clusterDTO.getRepoInfoList(), clusterPO);
        List<RepoPO> oldrepoPOList = repoMapper.findAllByClusterId(clusterPO.getId());

        for (RepoPO repoPO : repoPOList) {
            for (RepoPO oldRepoPO : oldrepoPOList) {
                if (oldRepoPO.getArch().equals(repoPO.getArch())
                        && oldRepoPO.getOs().equals(repoPO.getOs())) {
                    repoPO.setId(oldRepoPO.getId());
                }
            }
        }

        repoMapper.saveAll(repoPOList);
        return ClusterConverter.INSTANCE.fromEntity2VO(clusterPO);
    }

    @Override
    public ClusterVO get(Long id) {
        ClusterPO clusterPO =
                clusterMapper.findById(id).orElseThrow(() -> new ApiException(ApiExceptionEnum.CLUSTER_NOT_FOUND));

        return ClusterConverter.INSTANCE.fromEntity2VO(clusterPO);
    }

    @Override
    public ClusterVO update(Long id, ClusterDTO clusterDTO) {
        ClusterPO clusterPO = ClusterConverter.INSTANCE.fromDTO2PO(clusterDTO);
        clusterPO.setId(id);
        clusterMapper.updateById(clusterPO);

        return ClusterConverter.INSTANCE.fromEntity2VO(clusterPO);
    }
}
