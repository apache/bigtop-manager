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
package org.apache.bigtop.manager.server.model.converter;

import org.apache.bigtop.manager.common.message.entity.pojo.RepoInfo;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.RepoPO;
import org.apache.bigtop.manager.server.config.MapStructSharedConfig;
import org.apache.bigtop.manager.server.model.dto.RepoDTO;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(config = MapStructSharedConfig.class)
public interface RepoConverter {

    RepoConverter INSTANCE = Mappers.getMapper(RepoConverter.class);

    @Mapping(target = "clusterId", expression = "java(clusterPO.getId())")
    RepoPO fromDTO2PO(RepoDTO repoDTO, @Context ClusterPO clusterPO);

    @Mapping(target = "clusterId", expression = "java(clusterPO.getId())")
    List<RepoPO> fromDTO2PO(List<RepoDTO> repoDTOList, @Context ClusterPO clusterPO);

    RepoInfo fromPO2Message(RepoPO repoPO);

    List<RepoInfo> fromDTO2Message(List<RepoDTO> repoDTOs);
}
