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
import org.apache.bigtop.manager.dao.po.Cluster;
import org.apache.bigtop.manager.dao.po.Repo;
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

    @Mapping(target = "cluster", expression = "java(cluster)")
    Repo fromDTO2Entity(RepoDTO repoDTO, @Context Cluster cluster);

    @Mapping(target = "cluster", expression = "java(cluster)")
    List<Repo> fromDTO2Entity(List<RepoDTO> repoDTOList, @Context Cluster cluster);

    RepoInfo fromEntity2Message(Repo repo);

    List<RepoInfo> fromDTO2Message(List<RepoDTO> repoDTOs);
}
