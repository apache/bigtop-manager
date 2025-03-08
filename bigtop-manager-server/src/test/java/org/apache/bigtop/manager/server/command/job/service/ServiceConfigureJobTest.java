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
import org.apache.bigtop.manager.dao.po.StagePO;
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
import org.apache.bigtop.manager.server.command.stage.ComponentConfigureStage;
import org.apache.bigtop.manager.server.command.stage.ComponentStartStage;
import org.apache.bigtop.manager.server.command.stage.ComponentStopStage;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ServiceConfigureJobTest {

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

    private ServiceConfigureJob serviceConfigureJob;

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

        serviceConfigureJob = mock(ServiceConfigureJob.class);

        ServiceCommandDTO serviceCommandDTO = new ServiceCommandDTO();
        serviceCommandDTO.setServiceName("testName");

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

        Stage stage = mock(Stage.class);
        lenient().when(stage.getStagePO()).thenReturn(new StagePO());
        lenient().when(stage.getTasks()).thenReturn(new ArrayList<>());
        ;

        doCallRealMethod().when(serviceConfigureJob).setJobContextAndStagesForTest(any(), any());
        stages = new ArrayList<>();
        serviceConfigureJob.setJobContextAndStagesForTest(jobContext, stages);

        doCallRealMethod().when(serviceConfigureJob).injectBeans();
        serviceConfigureJob.injectBeans();

        doCallRealMethod().when(serviceConfigureJob).loadJobPO(any());
        lenient().when(serviceConfigureJob.getJobPO()).thenCallRealMethod();
        serviceConfigureJob.loadJobPO(jobPO);
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
        doCallRealMethod().when(serviceConfigureJob).beforeCreateStages();
        serviceConfigureJob.beforeCreateStages();
        verify(clusterDao, times(1)).findById(123L);
    }

    @Test
    public void testCreateStages() {
        try (MockedStatic<ComponentStageHelper> componentStageHelperMockedStatic =
                mockStatic(ComponentStageHelper.class)) {

            doCallRealMethod().when(serviceConfigureJob).createStages();
            when(serviceConfigureJob.getComponentHostsMap()).thenReturn(new HashMap<>());

            List<Stage> stageList1 = new ArrayList<>();
            stageList1.add(mock(ComponentConfigureStage.class));

            List<Stage> stageList2 = new ArrayList<>();
            stageList2.add(mock(ComponentStopStage.class));

            List<Stage> stageList3 = new ArrayList<>();
            stageList3.add(mock(ComponentStartStage.class));

            when(ComponentStageHelper.createComponentStages(any(), eq(Command.CONFIGURE), any()))
                    .thenReturn(stageList1);
            when(ComponentStageHelper.createComponentStages(any(), eq(Command.STOP), any()))
                    .thenReturn(stageList2);
            when(ComponentStageHelper.createComponentStages(any(), eq(Command.START), any()))
                    .thenReturn(stageList3);

            serviceConfigureJob.createStages();
            doCallRealMethod().when(serviceConfigureJob).getStages();
            assertEquals(serviceConfigureJob.getStages().size(), 3);
            assertInstanceOf(
                    ComponentConfigureStage.class,
                    serviceConfigureJob.getStages().get(0));
            assertInstanceOf(
                    ComponentStopStage.class, serviceConfigureJob.getStages().get(1));
            assertInstanceOf(
                    ComponentStartStage.class, serviceConfigureJob.getStages().get(2));
        }
    }

    @Test
    public void testGetName() {
        doCallRealMethod().when(serviceConfigureJob).getName();
        assertEquals("Configure services", serviceConfigureJob.getName());
    }
}
