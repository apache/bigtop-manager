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
package org.apache.bigtop.manager.server.command.task;

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.po.TaskPO;
import org.apache.bigtop.manager.dao.repository.ComponentDao;
import org.apache.bigtop.manager.dao.repository.HostDao;
import org.apache.bigtop.manager.dao.repository.TaskDao;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ComponentAddTaskTest {

    private MockedStatic<SpringContextHolder> springContextHolderMockedStatic;

    @Mock
    private HostDao hostDao;

    @Mock
    private TaskDao taskDao;

    @Mock
    private ComponentDao componentDao;

    @Spy
    private TaskContext taskContext;

    @Spy
    private TaskPO taskPO;

    private ComponentAddTask componentAddTask;

    @BeforeEach
    public void setUp() {
        springContextHolderMockedStatic = mockStatic(SpringContextHolder.class);
        when(SpringContextHolder.getBean(HostDao.class)).thenReturn(hostDao);
        when(SpringContextHolder.getBean(TaskDao.class)).thenReturn(taskDao);
        when(SpringContextHolder.getBean(ComponentDao.class)).thenReturn(componentDao);

        componentAddTask = mock(ComponentAddTask.class);

        taskContext.setComponentDisplayName("TestComponentDisplayName");
        taskContext.setHostname("TestHostname");
        taskContext.setServiceName("TestServiceName");
        taskContext.setServiceUser("TestServiceUser");
        taskContext.setComponentName("TestComponentName");
        taskContext.setClusterId(123L);

        ReflectionTestUtils.setField(componentAddTask, "taskContext", taskContext);

        doCallRealMethod().when(componentAddTask).injectBeans();
        componentAddTask.injectBeans();

        doCallRealMethod().when(componentAddTask).loadTaskPO(any());
        lenient().when(componentAddTask.getTaskPO()).thenCallRealMethod();
        componentAddTask.loadTaskPO(taskPO);
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
    public void testGetCommand() {
        doCallRealMethod().when(componentAddTask).getCommand();
        Command command = componentAddTask.getCommand();
        assertEquals("add", command.getCode());
        assertEquals("Add", command.getName());
    }

    @Test
    public void testGetCustomCommand() {
        doCallRealMethod().when(componentAddTask).getCustomCommand();
        assertDoesNotThrow(() -> componentAddTask.getCustomCommand());
    }

    @Test
    public void testBeforeRun() {
        doCallRealMethod().when(componentAddTask).beforeRun();
        componentAddTask.beforeRun();
        verify(taskDao, times(1)).partialUpdateById(any());
    }

    @Test
    public void testGetTaskContext() {
        doCallRealMethod().when(componentAddTask).getTaskContext();
        assertEquals(taskContext, componentAddTask.getTaskContext());
    }

    @Test
    public void testGetTaskPO() {
        doCallRealMethod().when(componentAddTask).getName();
        doCallRealMethod().when(componentAddTask).getCommand();

        componentAddTask.loadTaskPO(null);
        TaskPO result = componentAddTask.getTaskPO();

        assertEquals("Add TestComponentDisplayName on TestHostname", result.getName());
        assertEquals("Add", result.getCommand());

        assertEquals(JsonUtils.writeAsString(taskContext), result.getContext());
        assertEquals("TestHostname", result.getHostname());
        assertEquals("TestServiceName", result.getServiceName());
        assertEquals("TestServiceUser", result.getServiceUser());
        assertEquals("TestComponentName", result.getComponentName());
        assertNull(result.getCustomCommand());
    }

    @Test
    public void testGetName() {
        doCallRealMethod().when(componentAddTask).getName();
        assertEquals("Add TestComponentDisplayName on TestHostname", componentAddTask.getName());
    }
}
