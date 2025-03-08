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
package org.apache.bigtop.manager.server.command.job.host;

import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.JobPO;
import org.apache.bigtop.manager.dao.repository.ClusterDao;
import org.apache.bigtop.manager.dao.repository.ComponentDao;
import org.apache.bigtop.manager.dao.repository.JobDao;
import org.apache.bigtop.manager.dao.repository.StageDao;
import org.apache.bigtop.manager.dao.repository.TaskDao;
import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.command.stage.HostCheckStage;
import org.apache.bigtop.manager.server.command.stage.SetupJdkStage;
import org.apache.bigtop.manager.server.command.stage.Stage;
import org.apache.bigtop.manager.server.command.stage.StageContext;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.dto.HostDTO;
import org.apache.bigtop.manager.server.model.dto.command.ClusterCommandDTO;
import org.apache.bigtop.manager.server.service.HostService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HostAddJobTest {

    private MockedStatic<SpringContextHolder> springContextHolderMockedStatic;

    @Mock
    private ClusterDao clusterDao;

    @Mock
    private JobDao jobDao;

    @Mock
    private StageDao stageDao;

    @Mock
    private TaskDao taskDao;

    @Mock
    private ComponentDao componentDao;

    @Mock
    private HostService hostService;

    @Spy
    private JobContext jobContext;

    private List<Stage> stages;

    @Mock
    private ClusterPO clusterPO;

    @Spy
    private JobPO jobPO;

    private HostAddJob hostAddJob;

    @BeforeEach
    public void setUp() {
        springContextHolderMockedStatic = mockStatic(SpringContextHolder.class);
        when(SpringContextHolder.getBean(ClusterDao.class)).thenReturn(clusterDao);
        when(SpringContextHolder.getBean(JobDao.class)).thenReturn(jobDao);
        when(SpringContextHolder.getBean(StageDao.class)).thenReturn(stageDao);
        when(SpringContextHolder.getBean(TaskDao.class)).thenReturn(taskDao);
        when(SpringContextHolder.getBean(HostService.class)).thenReturn(hostService);
        when(SpringContextHolder.getBean(ComponentDao.class)).thenReturn(componentDao);

        hostAddJob = mock(HostAddJob.class);

        ClusterCommandDTO clusterCommandDTO = new ClusterCommandDTO();
        clusterCommandDTO.setName("testName");
        clusterCommandDTO.setDisplayName("testDisplayName");
        clusterCommandDTO.setDesc("testDescription.");
        clusterCommandDTO.setType(1);
        clusterCommandDTO.setUserGroup("testUserGroup");
        clusterCommandDTO.setRootDir("/test/root/dir");

        HostDTO hostDTO1 = new HostDTO();
        HostDTO hostDTO2 = new HostDTO();

        List<HostDTO> hosts = new ArrayList<>();
        hosts.add(hostDTO1);
        hosts.add(hostDTO2);
        clusterCommandDTO.setHosts(hosts);

        CommandDTO commandDTO = new CommandDTO();
        commandDTO.setClusterId(123L);
        commandDTO.setClusterCommand(clusterCommandDTO);
        ;

        clusterPO.setId(1L);
        lenient().when(clusterDao.findById(123L)).thenReturn(clusterPO);
        lenient().when(clusterDao.findByName("testName")).thenReturn(clusterPO);

        jobContext.setCommandDTO(commandDTO);
        jobContext.setRetryFlag(false);

        doCallRealMethod().when(hostAddJob).setJobContextAndStagesForTest(any(), any());
        stages = new ArrayList<>();
        hostAddJob.setJobContextAndStagesForTest(jobContext, stages);

        doCallRealMethod().when(hostAddJob).injectBeans();
        hostAddJob.injectBeans();

        doCallRealMethod().when(hostAddJob).loadJobPO(any());
        lenient().when(hostAddJob.getJobPO()).thenCallRealMethod();
        hostAddJob.loadJobPO(jobPO);
    }

    @AfterEach
    public void tearDown() {
        springContextHolderMockedStatic.close();
    }

    @Test
    public void testInjectBeans() {
        springContextHolderMockedStatic.verify(() -> SpringContextHolder.getBean(any(Class.class)), times(6));
    }

    @Test
    public void testBeforeCreateStages() {
        doCallRealMethod().when(hostAddJob).beforeCreateStages();
        hostAddJob.beforeCreateStages();
        verify(clusterDao, times(1)).findById(123L);
    }

    @Test
    public void testCreateStages() {
        try (MockedStatic<StageContext> stageContextMockedStatic = mockStatic(StageContext.class)) {
            when(hostAddJob.getHostCheckStage(any())).thenReturn(mock(HostCheckStage.class));
            when(hostAddJob.getSetupJdkStage(any())).thenReturn(mock(SetupJdkStage.class));

            when(StageContext.fromCommandDTO(any())).thenReturn(mock(StageContext.class));
            doCallRealMethod().when(hostAddJob).createStages();

            hostAddJob.createStages();
            doCallRealMethod().when(hostAddJob).getStages();
            assertEquals(hostAddJob.getStages().size(), 2);
            assertInstanceOf(HostCheckStage.class, hostAddJob.getStages().get(0));
            assertInstanceOf(SetupJdkStage.class, hostAddJob.getStages().get(1));
        }
    }

    @Test
    public void testBeforeRun() {
        doCallRealMethod().when(hostAddJob).beforeRun();

        jobContext.setRetryFlag(true);
        hostAddJob.beforeRun();
        verify(jobDao, times(1)).partialUpdateById(any());
        verify(hostAddJob, never()).saveHosts();

        jobContext.setRetryFlag(false);
        hostAddJob.beforeRun();
        verify(hostAddJob, times(1)).saveHosts();
    }

    @Test
    public void testGetName() {
        doCallRealMethod().when(hostAddJob).getName();
        assertEquals("Add hosts", hostAddJob.getName());
    }
}
