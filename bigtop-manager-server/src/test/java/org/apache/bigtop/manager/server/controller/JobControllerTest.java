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

import org.apache.bigtop.manager.server.model.vo.JobVO;
import org.apache.bigtop.manager.server.model.vo.PageVO;
import org.apache.bigtop.manager.server.service.JobService;
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

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobControllerTest {

    @Mock
    private JobService jobService;

    @InjectMocks
    private JobController jobController;

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
    void listReturnsAllJobs() {
        Long clusterId = 1L;
        PageVO<JobVO> jobs = PageVO.of(Arrays.asList(new JobVO(), new JobVO()), 2L);
        when(jobService.list(clusterId)).thenReturn(jobs);

        ResponseEntity<PageVO<JobVO>> response = jobController.list(clusterId);

        assertTrue(response.isSuccess());
        assertEquals(jobs, response.getData());
    }

    @Test
    void getReturnsJobById() {
        Long id = 1L;
        Long clusterId = 1L;
        JobVO job = new JobVO();
        when(jobService.get(id)).thenReturn(job);

        ResponseEntity<JobVO> response = jobController.get(id, clusterId);

        assertTrue(response.isSuccess());
        assertEquals(job, response.getData());
    }

    @Test
    void retryRetriesJob() {
        Long id = 1L;
        Long clusterId = 1L;
        JobVO job = new JobVO();
        when(jobService.retry(id)).thenReturn(job);

        ResponseEntity<JobVO> response = jobController.retry(id, clusterId);

        assertTrue(response.isSuccess());
        assertEquals(job, response.getData());
    }

    @Test
    void getReturnsNotFoundForInvalidId() {
        Long id = 999L;
        Long clusterId = 1L;
        when(jobService.get(id)).thenReturn(null);

        ResponseEntity<JobVO> response = jobController.get(id, clusterId);

        assertTrue(response.isSuccess());
        assertNull(response.getData());
    }
}
