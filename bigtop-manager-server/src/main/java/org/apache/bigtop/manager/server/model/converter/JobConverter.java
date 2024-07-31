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

import org.apache.bigtop.manager.dao.po.JobPO;
import org.apache.bigtop.manager.server.config.MapStructSharedConfig;
import org.apache.bigtop.manager.server.model.vo.CommandVO;
import org.apache.bigtop.manager.server.model.vo.JobVO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(
        uses = {StageConverter.class, TaskConverter.class},
        config = MapStructSharedConfig.class)
public interface JobConverter {

    JobConverter INSTANCE = Mappers.getMapper(JobConverter.class);

    @Mapping(target = "createTime", source = "createTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(target = "updateTime", source = "updateTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    JobVO fromPO2VO(JobPO jobPO);

    List<JobVO> fromPO2VO(List<JobPO> jobPO);

    CommandVO fromPO2CommandVO(JobPO jobPO);
}
