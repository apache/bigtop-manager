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

import org.apache.bigtop.manager.dao.po.RepoPO;
import org.apache.bigtop.manager.dao.repository.RepoDao;
import org.apache.bigtop.manager.server.model.converter.RepoConverter;
import org.apache.bigtop.manager.server.model.dto.RepoDTO;
import org.apache.bigtop.manager.server.model.vo.RepoVO;
import org.apache.bigtop.manager.server.service.RepoService;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class RepoServiceImpl implements RepoService {

    @Resource
    private RepoDao repoDao;

    @Override
    public List<RepoVO> list() {
        return RepoConverter.INSTANCE.fromPO2VO(repoDao.findAll());
    }

    @Override
    public List<RepoVO> update(List<RepoDTO> repoDTOList) {
        List<RepoPO> repoPOList = RepoConverter.INSTANCE.fromDTO2PO(repoDTOList);
        repoDao.partialUpdateByIds(repoPOList);
        return list();
    }
}
