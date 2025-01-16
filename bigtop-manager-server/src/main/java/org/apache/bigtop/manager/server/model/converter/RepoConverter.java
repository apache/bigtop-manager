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

import org.apache.bigtop.manager.grpc.pojo.RepoInfo;
import org.apache.bigtop.manager.dao.po.RepoPO;
import org.apache.bigtop.manager.server.config.MapStructSharedConfig;
import org.apache.bigtop.manager.server.model.dto.RepoDTO;
import org.apache.bigtop.manager.server.model.req.RepoReq;
import org.apache.bigtop.manager.server.model.vo.RepoVO;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(config = MapStructSharedConfig.class)
public interface RepoConverter {

    RepoConverter INSTANCE = Mappers.getMapper(RepoConverter.class);

    RepoDTO fromReq2DTO(RepoReq repoReq);

    List<RepoDTO> fromReq2DTO(List<RepoReq> repoReqList);

    RepoPO fromDTO2PO(RepoDTO repoDTO);

    List<RepoPO> fromDTO2PO(List<RepoDTO> repoDTOList);

    RepoVO fromPO2VO(RepoPO repoPO);

    List<RepoVO> fromPO2VO(List<RepoPO> repoPOList);

    RepoInfo fromPO2Message(RepoPO repoPO);

    List<RepoInfo> fromDTO2Message(List<RepoDTO> repoDTOs);
}
