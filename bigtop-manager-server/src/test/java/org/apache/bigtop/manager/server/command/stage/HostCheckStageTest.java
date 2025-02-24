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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HostCheckStageTest {

    private MockedStatic<SpringContextHolder> springContextHolderMockedStatic;

    @Mock
    private StageDao stageDao;

    @Mock
    private HostDao hostDao;

    @Spy
    private StageContext stageContext;

    @Mock
    private List<Task> tasks;

    @Spy
    private StagePO stagePO;

    private HostCheckStage hostCheckStage;

    @BeforeEach
    public void setUp() {
        springContextHolderMockedStatic = mockStatic(SpringContextHolder.class);
        when(SpringContextHolder.getBean(StageDao.class)).thenReturn(stageDao);
        when(SpringContextHolder.getBean(HostDao.class)).thenReturn(hostDao);

        hostCheckStage = mock(HostCheckStage.class);

        doCallRealMethod().when(hostCheckStage).setStageContextAndTasksForTest(any(), any());
        hostCheckStage.setStageContextAndTasksForTest(stageContext, tasks);

        doCallRealMethod().when(hostCheckStage).injectBeans();
        hostCheckStage.injectBeans();

        doCallRealMethod().when(hostCheckStage).loadStagePO(any());
        lenient().when(hostCheckStage.getStagePO()).thenCallRealMethod();
        hostCheckStage.loadStagePO(stagePO);
    }

    @AfterEach
    public void tearDown() {
        springContextHolderMockedStatic.close();
    }

    @Test
    public void testInjectBeans() {
        springContextHolderMockedStatic.verify(() -> SpringContextHolder.getBean(any(Class.class)), times(2));
    }

    @Test
    public void testBeforeCreateTasks() {
        doCallRealMethod().when(hostCheckStage).beforeCreateTasks();
        assertDoesNotThrow(() -> hostCheckStage.beforeCreateTasks());
    }

    @Test
    public void tesGetName() {
        doCallRealMethod().when(hostCheckStage).getName();
        assertEquals("Check hosts", hostCheckStage.getName());
    }
}
