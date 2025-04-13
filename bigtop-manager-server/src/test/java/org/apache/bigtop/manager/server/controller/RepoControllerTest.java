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
package org.apache.bigtop.manager.server.controller;

import org.apache.bigtop.manager.server.model.req.RepoReq;
import org.apache.bigtop.manager.server.model.vo.RepoVO;
import org.apache.bigtop.manager.server.service.RepoService;
import org.apache.bigtop.manager.server.utils.MessageSourceUtils;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RepoControllerTest {

    @Mock
    private RepoService repoService;

    @InjectMocks
    private RepoController repoController;

    private MockedStatic<MessageSourceUtils> mockedMessageSourceUtils;

    @BeforeEach
    void setUp() {
        mockedMessageSourceUtils = Mockito.mockStatic(MessageSourceUtils.class);
        when(MessageSourceUtils.getMessage(any())).thenReturn("Mocked message");
    }

    @AfterEach
    void tearDown() {
        mockedMessageSourceUtils.close();
    }

    @Test
    void listRepos() {
        List<RepoVO> repos = new ArrayList<>();
        when(repoService.list()).thenReturn(repos);

        ResponseEntity<List<RepoVO>> response = repoController.list();

        assertTrue(response.isSuccess());
        assertEquals(repos, response.getData());
    }

    @Test
    void updateRepos() {
        List<RepoReq> repoReqList = new ArrayList<>();
        List<RepoVO> updatedRepos = new ArrayList<>();
        when(repoService.update(any())).thenReturn(updatedRepos);

        ResponseEntity<List<RepoVO>> response = repoController.update(repoReqList);

        assertTrue(response.isSuccess());
        assertEquals(updatedRepos, response.getData());
    }

    @Test
    void listRepos_notNull() {
        when(repoService.list()).thenReturn(new ArrayList<>());

        ResponseEntity<List<RepoVO>> response = repoController.list();

        assertNotNull(response.getData());
    }

    @Test
    void updateRepos_emptyRequest() {
        List<RepoReq> repoReqList = new ArrayList<>();
        List<RepoVO> updatedRepos = new ArrayList<>();
        when(repoService.update(any())).thenReturn(updatedRepos);

        ResponseEntity<List<RepoVO>> response = repoController.update(repoReqList);

        assertTrue(response.isSuccess());
        assertEquals(updatedRepos, response.getData());
    }
}
