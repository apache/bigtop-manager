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
package org.apache.bigtop.manager.server.command.job.cluster;

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
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClusterAddJobTest {

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

    private ClusterAddJob clusterAddJob;

    @BeforeEach
    public void setUp() {
        springContextHolderMockedStatic = mockStatic(SpringContextHolder.class);
        when(SpringContextHolder.getBean(ClusterDao.class)).thenReturn(clusterDao);
        when(SpringContextHolder.getBean(JobDao.class)).thenReturn(jobDao);
        when(SpringContextHolder.getBean(StageDao.class)).thenReturn(stageDao);
        when(SpringContextHolder.getBean(TaskDao.class)).thenReturn(taskDao);
        when(SpringContextHolder.getBean(ComponentDao.class)).thenReturn(componentDao);
        when(SpringContextHolder.getBean(HostService.class)).thenReturn(hostService);

        clusterAddJob = mock(ClusterAddJob.class);

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

        clusterPO.setId(1L);
        lenient().when(clusterDao.findById(123L)).thenReturn(clusterPO);
        lenient().when(clusterDao.findByName("testName")).thenReturn(clusterPO);

        jobContext.setCommandDTO(commandDTO);
        jobContext.setRetryFlag(false);

        doCallRealMethod().when(clusterAddJob).setJobContextAndStagesForTest(any(), any());
        stages = new ArrayList<>();
        clusterAddJob.setJobContextAndStagesForTest(jobContext, stages);

        doCallRealMethod().when(clusterAddJob).injectBeans();
        clusterAddJob.injectBeans();

        doCallRealMethod().when(clusterAddJob).loadJobPO(any());
        lenient().when(clusterAddJob.getJobPO()).thenCallRealMethod();
        clusterAddJob.loadJobPO(jobPO);
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
        doCallRealMethod().when(clusterAddJob).beforeCreateStages();

        clusterAddJob.beforeCreateStages();
        verify(clusterDao, times(1)).findById(123L);

        jobContext.setRetryFlag(true);
        clusterAddJob.beforeCreateStages();
        verify(clusterDao, times(1)).findByName("testName");
    }

    @Test
    public void testCreateStages() {
        try (MockedStatic<StageContext> stageContextMockedStatic = mockStatic(StageContext.class)) {
            when(clusterAddJob.getHostCheckStage(any())).thenReturn(mock(HostCheckStage.class));
            when(clusterAddJob.getSetupJdkStage(any())).thenReturn(mock(SetupJdkStage.class));

            when(StageContext.fromCommandDTO(any())).thenReturn(mock(StageContext.class));
            doCallRealMethod().when(clusterAddJob).createStages();

            clusterAddJob.createStages();
            doCallRealMethod().when(clusterAddJob).getStages();
            assertEquals(clusterAddJob.getStages().size(), 2);
            assertInstanceOf(HostCheckStage.class, clusterAddJob.getStages().get(0));
            assertInstanceOf(SetupJdkStage.class, clusterAddJob.getStages().get(1));
        }
    }

    @Test
    public void testBeforeRun() {
        doCallRealMethod().when(clusterAddJob).beforeRun();
        doNothing().when(clusterAddJob).saveCluster();
        doNothing().when(clusterAddJob).saveHosts();
        doNothing().when(clusterAddJob).linkJobToCluster();

        jobContext.setRetryFlag(true);
        clusterAddJob.beforeRun();
        verify(jobDao, times(1)).partialUpdateById(any());
        verify(clusterAddJob, never()).saveCluster();
        verify(clusterAddJob, never()).saveHosts();
        verify(clusterAddJob, never()).linkJobToCluster();

        jobContext.setRetryFlag(false);
        clusterAddJob.beforeRun();
        verify(clusterAddJob, times(1)).saveCluster();
        verify(clusterAddJob, times(1)).saveHosts();
        verify(clusterAddJob, times(1)).linkJobToCluster();
    }

    @Test
    public void testGetName() {
        doCallRealMethod().when(clusterAddJob).getName();
        assertEquals("Add cluster", clusterAddJob.getName());
    }

    @Test
    public void testSaveCluster() {
        doCallRealMethod().when(clusterAddJob).saveCluster();
        clusterAddJob.saveCluster();
        verify(clusterDao, times(1)).save(any());
    }

    @Test
    public void testSaveHosts() {
        doCallRealMethod().when(clusterAddJob).beforeCreateStages();
        doCallRealMethod().when(clusterAddJob).saveHosts();
        clusterAddJob.beforeCreateStages();
        clusterAddJob.saveHosts();
        verify(hostService, times(2)).add(any());
    }

    @Test
    public void testLinkJobToCluster() {
        doCallRealMethod().when(clusterAddJob).beforeCreateStages();
        doCallRealMethod().when(clusterAddJob).linkJobToCluster();
        clusterAddJob.beforeCreateStages();
        clusterAddJob.linkJobToCluster();
        verify(jobDao, times(1)).partialUpdateById(any());
    }

    @Test
    public void testGetJobContext() {
        doCallRealMethod().when(clusterAddJob).getJobContext();
        assertEquals(jobContext, clusterAddJob.getJobContext());
    }

    @Test
    public void testGetStages() {
        doCallRealMethod().when(clusterAddJob).getStages();
        assertEquals(stages, clusterAddJob.getStages());
    }

    @Test
    public void testOnSuccess() {
        doCallRealMethod().when(clusterAddJob).onSuccess();
        clusterAddJob.onSuccess();
        verify(jobDao, times(1)).partialUpdateById(any());
    }

    @Test
    public void testOnFailure() {
        doCallRealMethod().when(clusterAddJob).onFailure();
        clusterAddJob.onFailure();
        verify(taskDao, times(1)).partialUpdateByIds(any());
        verify(stageDao, times(1)).partialUpdateByIds(any());
        verify(jobDao, times(1)).partialUpdateById(any());
    }
}
