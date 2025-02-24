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
package org.apache.bigtop.manager.server.command.stage;

import org.apache.bigtop.manager.dao.po.StagePO;
import org.apache.bigtop.manager.dao.repository.ClusterDao;
import org.apache.bigtop.manager.dao.repository.HostDao;
import org.apache.bigtop.manager.dao.repository.StageDao;
import org.apache.bigtop.manager.server.command.task.Task;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ComponentStageTest {

    private MockedStatic<SpringContextHolder> springContextHolderMockedStatic;

    @Mock
    private StageDao stageDao;

    @Mock
    private HostDao hostDao;

    @Mock
    private ClusterDao clusterDao;

    @Spy
    private StageContext stageContext;

    @Mock
    private List<Task> tasks;

    @Spy
    private StagePO stagePO;

    private ComponentAddStage componentStage;

    @BeforeEach
    public void setUp() {
        springContextHolderMockedStatic = mockStatic(SpringContextHolder.class);
        when(SpringContextHolder.getBean(StageDao.class)).thenReturn(stageDao);
        when(SpringContextHolder.getBean(HostDao.class)).thenReturn(hostDao);
        when(SpringContextHolder.getBean(ClusterDao.class)).thenReturn(clusterDao);

        componentStage = mock(ComponentAddStage.class);

        stageContext.setComponentName("TestComponentName");
        stageContext.setServiceName("TestServiceName");
        stageContext.setClusterId(123L);

        doCallRealMethod().when(componentStage).setStageContextAndTasksForTest(any(), any());
        componentStage.setStageContextAndTasksForTest(stageContext, tasks);

        doCallRealMethod().when(componentStage).injectBeans();
        componentStage.injectBeans();

        doCallRealMethod().when(componentStage).loadStagePO(any());
        lenient().when(componentStage.getStagePO()).thenCallRealMethod();
        componentStage.loadStagePO(stagePO);
    }

    @AfterEach
    public void tearDown() {
        springContextHolderMockedStatic.close();
    }

    @Test
    public void testInjectBeans() {
        springContextHolderMockedStatic.verify(() -> SpringContextHolder.getBean(any(Class.class)), times(3));
    }

    @Test
    public void testBeforeCreateTasks() {
        doCallRealMethod().when(componentStage).beforeCreateTasks();
        componentStage.beforeCreateTasks();
        verify(clusterDao, times(1)).findById(123L);
    }

    @Test
    public void tesGetServiceName() {
        doCallRealMethod().when(componentStage).getServiceName();
        assertEquals("TestServiceName", componentStage.getServiceName());
    }

    @Test
    public void tesGetComponentName() {
        doCallRealMethod().when(componentStage).getComponentName();
        assertEquals("TestComponentName", componentStage.getComponentName());
    }

    @Test
    public void testBeforeRun() {
        doCallRealMethod().when(componentStage).beforeRun();
        componentStage.beforeRun();
        verify(stageDao, times(1)).partialUpdateById(any());
    }

    @Test
    public void testOnSuccess() {
        doCallRealMethod().when(componentStage).onSuccess();
        componentStage.onSuccess();
        verify(stageDao, times(1)).partialUpdateById(any());
    }

    @Test
    public void testOnFailure() {
        doCallRealMethod().when(componentStage).onFailure();
        componentStage.onFailure();
        verify(stageDao, times(1)).partialUpdateById(any());
    }

    @Test
    public void testGetStageContext() {
        doCallRealMethod().when(componentStage).getStageContext();
        assertEquals(stageContext, componentStage.getStageContext());
    }

    @Test
    public void testGetTasks() {
        doCallRealMethod().when(componentStage).getTasks();
        assertEquals(tasks, componentStage.getTasks());
    }
}
