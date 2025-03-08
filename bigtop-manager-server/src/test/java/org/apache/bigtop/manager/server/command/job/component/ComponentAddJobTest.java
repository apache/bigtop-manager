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
package org.apache.bigtop.manager.server.command.job.component;

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.po.JobPO;
import org.apache.bigtop.manager.dao.repository.ClusterDao;
import org.apache.bigtop.manager.dao.repository.ComponentDao;
import org.apache.bigtop.manager.dao.repository.HostDao;
import org.apache.bigtop.manager.dao.repository.JobDao;
import org.apache.bigtop.manager.dao.repository.ServiceDao;
import org.apache.bigtop.manager.dao.repository.StageDao;
import org.apache.bigtop.manager.dao.repository.TaskDao;
import org.apache.bigtop.manager.server.command.helper.ComponentStageHelper;
import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.command.stage.ComponentAddStage;
import org.apache.bigtop.manager.server.command.stage.ComponentConfigureStage;
import org.apache.bigtop.manager.server.command.stage.ComponentInitStage;
import org.apache.bigtop.manager.server.command.stage.ComponentPrepareStage;
import org.apache.bigtop.manager.server.command.stage.ComponentStartStage;
import org.apache.bigtop.manager.server.command.stage.Stage;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.dto.HostDTO;
import org.apache.bigtop.manager.server.model.dto.command.ClusterCommandDTO;
import org.apache.bigtop.manager.server.model.dto.command.ComponentCommandDTO;

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
public class ComponentAddJobTest {

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
    private HostDao hostDao;

    @Mock
    private ServiceDao serviceDao;

    @Mock
    private ComponentDao componentDao;

    @Spy
    private JobContext jobContext;

    private List<Stage> stages;

    @Mock
    private ClusterPO clusterPO;

    @Spy
    private JobPO jobPO;

    private ComponentAddJob componentAddJob;

    @BeforeEach
    public void setUp() {
        springContextHolderMockedStatic = mockStatic(SpringContextHolder.class);
        when(SpringContextHolder.getBean(ClusterDao.class)).thenReturn(clusterDao);
        when(SpringContextHolder.getBean(JobDao.class)).thenReturn(jobDao);
        when(SpringContextHolder.getBean(StageDao.class)).thenReturn(stageDao);
        when(SpringContextHolder.getBean(TaskDao.class)).thenReturn(taskDao);
        when(SpringContextHolder.getBean(HostDao.class)).thenReturn(hostDao);
        when(SpringContextHolder.getBean(ServiceDao.class)).thenReturn(serviceDao);
        when(SpringContextHolder.getBean(ComponentDao.class)).thenReturn(componentDao);

        componentAddJob = mock(ComponentAddJob.class);

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

        List<ComponentCommandDTO> componentCommands = new ArrayList<>();

        CommandDTO commandDTO = new CommandDTO();
        commandDTO.setClusterId(123L);
        commandDTO.setClusterCommand(clusterCommandDTO);
        commandDTO.setComponentCommands(componentCommands);

        clusterPO.setId(1L);
        lenient().when(clusterDao.findById(123L)).thenReturn(clusterPO);
        lenient().when(clusterDao.findByName("testName")).thenReturn(clusterPO);

        jobContext.setCommandDTO(commandDTO);
        jobContext.setRetryFlag(false);

        doCallRealMethod().when(componentAddJob).setJobContextAndStagesForTest(any(), any());
        stages = new ArrayList<>();
        componentAddJob.setJobContextAndStagesForTest(jobContext, stages);

        doCallRealMethod().when(componentAddJob).injectBeans();
        componentAddJob.injectBeans();

        doCallRealMethod().when(componentAddJob).loadJobPO(any());
        lenient().when(componentAddJob.getJobPO()).thenCallRealMethod();
        componentAddJob.loadJobPO(jobPO);
    }

    @AfterEach
    public void tearDown() {
        springContextHolderMockedStatic.close();
    }

    @Test
    public void testInjectBeans() {
        springContextHolderMockedStatic.verify(() -> SpringContextHolder.getBean(any(Class.class)), times(7));
    }

    @Test
    public void testBeforeCreateStages() {
        doCallRealMethod().when(componentAddJob).beforeCreateStages();
        componentAddJob.beforeCreateStages();
        verify(clusterDao, times(1)).findById(123L);
    }

    @Test
    public void testCreateStages() {
        try (MockedStatic<ComponentStageHelper> componentStageHelperMockedStatic =
                mockStatic(ComponentStageHelper.class)) {

            doCallRealMethod().when(componentAddJob).createStages();
            when(componentAddJob.getComponentHostsMap()).thenReturn(new HashMap<>());

            List<Stage> stageList1 = new ArrayList<>();
            stageList1.add(mock(ComponentAddStage.class));

            List<Stage> stageList2 = new ArrayList<>();
            stageList2.add(mock(ComponentConfigureStage.class));

            List<Command> commands = List.of(Command.INIT, Command.START, Command.PREPARE);
            List<Stage> stageList3 = new ArrayList<>();
            stageList3.add(mock(ComponentInitStage.class));
            stageList3.add(mock(ComponentStartStage.class));
            stageList3.add(mock(ComponentPrepareStage.class));

            when(ComponentStageHelper.createComponentStages(any(), eq(Command.ADD), any()))
                    .thenReturn(stageList1);
            when(ComponentStageHelper.createComponentStages(any(), eq(Command.CONFIGURE), any()))
                    .thenReturn(stageList2);
            when(ComponentStageHelper.createComponentStages(any(), eq(commands), any()))
                    .thenReturn(stageList3);

            componentAddJob.createStages();
            doCallRealMethod().when(componentAddJob).getStages();
            assertEquals(componentAddJob.getStages().size(), 5);
            assertInstanceOf(
                    ComponentAddStage.class, componentAddJob.getStages().get(0));
            assertInstanceOf(
                    ComponentConfigureStage.class, componentAddJob.getStages().get(1));
            assertInstanceOf(
                    ComponentInitStage.class, componentAddJob.getStages().get(2));
            assertInstanceOf(
                    ComponentStartStage.class, componentAddJob.getStages().get(3));
            assertInstanceOf(
                    ComponentPrepareStage.class, componentAddJob.getStages().get(4));
        }
    }

    @Test
    public void testBeforeRun() {
        doCallRealMethod().when(componentAddJob).beforeRun();

        jobContext.setRetryFlag(true);
        componentAddJob.beforeRun();
        verify(jobDao, times(1)).partialUpdateById(any());
        verify(jobContext, never()).getCommandDTO();

        jobContext.setRetryFlag(false);
        componentAddJob.beforeRun();
        verify(jobContext, times(1)).getCommandDTO();
    }

    @Test
    public void testGetName() {
        doCallRealMethod().when(componentAddJob).getName();
        assertEquals("Add components", componentAddJob.getName());
    }
}
