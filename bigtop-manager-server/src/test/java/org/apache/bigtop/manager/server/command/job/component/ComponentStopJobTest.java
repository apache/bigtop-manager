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
import org.apache.bigtop.manager.server.command.stage.ComponentStopStage;
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
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ComponentStopJobTest {

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

    private ComponentStopJob componentStopJob;

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

        componentStopJob = mock(ComponentStopJob.class);

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

        doCallRealMethod().when(componentStopJob).setJobContextAndStagesForTest(any(), any());
        stages = new ArrayList<>();
        componentStopJob.setJobContextAndStagesForTest(jobContext, stages);

        doCallRealMethod().when(componentStopJob).injectBeans();
        componentStopJob.injectBeans();

        doCallRealMethod().when(componentStopJob).loadJobPO(any());
        lenient().when(componentStopJob.getJobPO()).thenCallRealMethod();
        componentStopJob.loadJobPO(jobPO);
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
        doCallRealMethod().when(componentStopJob).beforeCreateStages();
        componentStopJob.beforeCreateStages();
        verify(clusterDao, times(1)).findById(123L);
    }

    @Test
    public void testCreateStages() {
        try (MockedStatic<ComponentStageHelper> componentStageHelperMockedStatic =
                mockStatic(ComponentStageHelper.class)) {

            doCallRealMethod().when(componentStopJob).createStages();
            when(componentStopJob.getComponentHostsMap()).thenReturn(new HashMap<>());

            List<Stage> stageList = new ArrayList<>();
            stageList.add(mock(ComponentStopStage.class));

            when(ComponentStageHelper.createComponentStages(any(), any())).thenReturn(stageList);

            componentStopJob.createStages();
            doCallRealMethod().when(componentStopJob).getStages();
            assertEquals(componentStopJob.getStages().size(), 1);
            assertInstanceOf(
                    ComponentStopStage.class, componentStopJob.getStages().get(0));
        }
    }

    @Test
    public void testGetName() {
        doCallRealMethod().when(componentStopJob).getName();
        assertEquals("Stop components", componentStopJob.getName());
    }
}
