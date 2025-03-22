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
package org.apache.bigtop.manager.server.command.job.service;

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.JobPO;
import org.apache.bigtop.manager.dao.po.ServicePO;
import org.apache.bigtop.manager.dao.repository.ClusterDao;
import org.apache.bigtop.manager.dao.repository.ComponentDao;
import org.apache.bigtop.manager.dao.repository.HostDao;
import org.apache.bigtop.manager.dao.repository.JobDao;
import org.apache.bigtop.manager.dao.repository.ServiceConfigDao;
import org.apache.bigtop.manager.dao.repository.ServiceConfigSnapshotDao;
import org.apache.bigtop.manager.dao.repository.ServiceDao;
import org.apache.bigtop.manager.dao.repository.StageDao;
import org.apache.bigtop.manager.dao.repository.TaskDao;
import org.apache.bigtop.manager.server.command.helper.ComponentStageHelper;
import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.command.stage.ComponentAddStage;
import org.apache.bigtop.manager.server.command.stage.ComponentCheckStage;
import org.apache.bigtop.manager.server.command.stage.ComponentConfigureStage;
import org.apache.bigtop.manager.server.command.stage.ComponentInitStage;
import org.apache.bigtop.manager.server.command.stage.ComponentPrepareStage;
import org.apache.bigtop.manager.server.command.stage.ComponentStartStage;
import org.apache.bigtop.manager.server.command.stage.Stage;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.dto.command.ServiceCommandDTO;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ServiceAddJobTest {

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
    private ServiceDao serviceDao;

    @Mock
    private ServiceConfigDao serviceConfigDao;

    @Mock
    private ServiceConfigSnapshotDao serviceConfigSnapshotDao;

    @Mock
    private ComponentDao componentDao;

    @Mock
    private HostDao hostDao;

    @Spy
    private JobContext jobContext;

    private List<Stage> stages;

    @Mock
    private ClusterPO clusterPO;

    @Spy
    private JobPO jobPO;

    private ServiceAddJob serviceAddJob;

    @BeforeEach
    public void setUp() {
        springContextHolderMockedStatic = mockStatic(SpringContextHolder.class);
        when(SpringContextHolder.getBean(ClusterDao.class)).thenReturn(clusterDao);
        when(SpringContextHolder.getBean(JobDao.class)).thenReturn(jobDao);
        when(SpringContextHolder.getBean(StageDao.class)).thenReturn(stageDao);
        when(SpringContextHolder.getBean(TaskDao.class)).thenReturn(taskDao);
        when(SpringContextHolder.getBean(ServiceDao.class)).thenReturn(serviceDao);
        when(SpringContextHolder.getBean(ServiceConfigDao.class)).thenReturn(serviceConfigDao);
        when(SpringContextHolder.getBean(ServiceConfigSnapshotDao.class)).thenReturn(serviceConfigSnapshotDao);
        when(SpringContextHolder.getBean(ComponentDao.class)).thenReturn(componentDao);
        when(SpringContextHolder.getBean(HostDao.class)).thenReturn(hostDao);

        serviceAddJob = mock(ServiceAddJob.class);

        ServiceCommandDTO serviceCommandDTO = new ServiceCommandDTO();
        serviceCommandDTO.setServiceName("testName");
        serviceCommandDTO.setInstalled(false);

        ArrayList<ServiceCommandDTO> serviceCommands = new ArrayList<>();
        serviceCommands.add(serviceCommandDTO);

        CommandDTO commandDTO = new CommandDTO();
        commandDTO.setClusterId(123L);
        commandDTO.setServiceCommands(serviceCommands);

        clusterPO.setId(1L);
        lenient().when(clusterDao.findById(123L)).thenReturn(clusterPO);
        lenient().when(clusterDao.findByName("testName")).thenReturn(clusterPO);

        jobContext.setCommandDTO(commandDTO);
        jobContext.setRetryFlag(false);

        doCallRealMethod().when(serviceAddJob).setJobContextAndStagesForTest(any(), any());
        stages = new ArrayList<>();
        serviceAddJob.setJobContextAndStagesForTest(jobContext, stages);

        doCallRealMethod().when(serviceAddJob).injectBeans();
        serviceAddJob.injectBeans();

        doCallRealMethod().when(serviceAddJob).loadJobPO(any());
        lenient().when(serviceAddJob.getJobPO()).thenCallRealMethod();
        serviceAddJob.loadJobPO(jobPO);
    }

    @AfterEach
    public void tearDown() {
        springContextHolderMockedStatic.close();
    }

    @Test
    public void testInjectBeans() {
        springContextHolderMockedStatic.verify(() -> SpringContextHolder.getBean(any(Class.class)), times(9));
    }

    @Test
    public void testBeforeCreateStages() {
        doCallRealMethod().when(serviceAddJob).beforeCreateStages();
        serviceAddJob.beforeCreateStages();
        verify(clusterDao, times(1)).findById(123L);
    }

    @Test
    public void testCreateStages() {
        try (MockedStatic<ComponentStageHelper> componentStageHelperMockedStatic =
                mockStatic(ComponentStageHelper.class)) {

            doCallRealMethod().when(serviceAddJob).createStages();
            when(serviceAddJob.getComponentHostsMap()).thenReturn(new HashMap<>());

            List<Stage> stageList1 = new ArrayList<>();
            stageList1.add(mock(ComponentAddStage.class));

            List<Stage> stageList2 = new ArrayList<>();
            stageList2.add(mock(ComponentConfigureStage.class));

            List<Command> commands = List.of(Command.INIT, Command.START, Command.PREPARE);
            List<Stage> stageList3 = new ArrayList<>();
            stageList3.add(mock(ComponentInitStage.class));
            stageList3.add(mock(ComponentStartStage.class));
            stageList3.add(mock(ComponentPrepareStage.class));

            List<Stage> stageList4 = new ArrayList<>();
            stageList4.add(mock(ComponentCheckStage.class));

            when(ComponentStageHelper.createComponentStages(any(), eq(Command.ADD), any()))
                    .thenReturn(stageList1);
            when(ComponentStageHelper.createComponentStages(any(), eq(Command.CONFIGURE), any()))
                    .thenReturn(stageList2);
            when(ComponentStageHelper.createComponentStages(any(), eq(commands), any()))
                    .thenReturn(stageList3);
            when(ComponentStageHelper.createComponentStages(any(), eq(Command.CHECK), any()))
                    .thenReturn(stageList4);

            serviceAddJob.createStages();
            doCallRealMethod().when(serviceAddJob).getStages();
            assertEquals(serviceAddJob.getStages().size(), 6);
            assertInstanceOf(ComponentAddStage.class, serviceAddJob.getStages().get(0));
            assertInstanceOf(
                    ComponentConfigureStage.class, serviceAddJob.getStages().get(1));
            assertInstanceOf(ComponentInitStage.class, serviceAddJob.getStages().get(2));
            assertInstanceOf(
                    ComponentStartStage.class, serviceAddJob.getStages().get(3));
            assertInstanceOf(
                    ComponentPrepareStage.class, serviceAddJob.getStages().get(4));
            assertInstanceOf(
                    ComponentCheckStage.class, serviceAddJob.getStages().get(5));
        }
    }

    @Test
    public void testBeforeRun() {
        doCallRealMethod().when(serviceAddJob).beforeRun();

        jobContext.setRetryFlag(true);
        serviceAddJob.beforeRun();
        verify(jobDao, times(1)).partialUpdateById(any());
        verify(serviceAddJob, never()).saveService(any());

        jobContext.setRetryFlag(false);
        serviceAddJob.beforeRun();
        verify(serviceAddJob, times(1)).saveService(any());
    }

    @Test
    public void testOnSuccess() {
        doCallRealMethod().when(serviceAddJob).onSuccess();
        when(serviceDao.findByClusterIdAndName(any(), any())).thenReturn(new ServicePO());

        serviceAddJob.onSuccess();
        verify(jobDao, times(1)).partialUpdateById(any());
        verify(serviceDao, times(1)).partialUpdateByIds(any());
    }

    @Test
    public void testGetName() {
        doCallRealMethod().when(serviceAddJob).getName();
        assertEquals("Add services", serviceAddJob.getName());
    }
}
