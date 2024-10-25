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
package org.apache.bigtop.manager.server.service;

import org.apache.bigtop.manager.dao.po.RepoPO;
import org.apache.bigtop.manager.dao.repository.RepoDao;
import org.apache.bigtop.manager.server.model.converter.RepoConverter;
import org.apache.bigtop.manager.server.model.dto.RepoDTO;
import org.apache.bigtop.manager.server.model.vo.RepoVO;
import org.apache.bigtop.manager.server.service.impl.RepoServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RepoServiceTest {

    @Mock
    private RepoDao repoDao;

    @Mock
    private RepoConverter repoConverter;

    @InjectMocks
    private RepoServiceImpl repoServiceImpl;

    @Test
    void listRepos_returnsRepoVOList() {
        List<RepoPO> repoPOList = new ArrayList<>();
        List<RepoVO> repoVOList = new ArrayList<>();
        when(repoDao.findAll()).thenReturn(repoPOList);

        List<RepoVO> result = repoServiceImpl.list();

        assertEquals(repoVOList, result);
    }

    @Test
    void updateRepos_returnsUpdatedRepoVOList() {
        List<RepoDTO> repoDTOList = new ArrayList<>();
        List<RepoVO> repoVOList = new ArrayList<>();

        List<RepoVO> result = repoServiceImpl.update(repoDTOList);

        assertEquals(repoVOList, result);
    }

    @Test
    void listRepos_notNull() {
        when(repoDao.findAll()).thenReturn(new ArrayList<>());

        List<RepoVO> result = repoServiceImpl.list();

        assertNotNull(result);
    }

    @Test
    void updateRepos_emptyRequest() {
        List<RepoDTO> repoDTOList = new ArrayList<>();
        List<RepoVO> repoVOList = new ArrayList<>();

        List<RepoVO> result = repoServiceImpl.update(repoDTOList);

        assertEquals(repoVOList, result);
    }
}
