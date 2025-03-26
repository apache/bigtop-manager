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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SetupJdkTaskTest {

    private MockedStatic<SpringContextHolder> springContextHolderMockedStatic;

    @Mock
    private HostDao hostDao;

    @Mock
    private TaskDao taskDao;

    @Spy
    private TaskContext taskContext;

    @Spy
    private TaskPO taskPO;

    private SetupJdkTask setupJdkTask;

    @BeforeEach
    public void setUp() {
        springContextHolderMockedStatic = mockStatic(SpringContextHolder.class);
        when(SpringContextHolder.getBean(HostDao.class)).thenReturn(hostDao);
        when(SpringContextHolder.getBean(TaskDao.class)).thenReturn(taskDao);

        setupJdkTask = mock(SetupJdkTask.class);

        taskContext.setComponentDisplayName("TestComponentDisplayName");
        taskContext.setHostname("TestHostname");
        taskContext.setServiceName("TestServiceName");
        taskContext.setServiceUser("TestServiceUser");
        taskContext.setComponentName("TestComponentName");
        taskContext.setClusterId(123L);

        ReflectionTestUtils.setField(setupJdkTask, "taskContext", taskContext);

        doCallRealMethod().when(setupJdkTask).injectBeans();
        setupJdkTask.injectBeans();

        doCallRealMethod().when(setupJdkTask).loadTaskPO(any());
        lenient().when(setupJdkTask.getTaskPO()).thenCallRealMethod();
        setupJdkTask.loadTaskPO(taskPO);
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
    public void testGetCommand() {
        doCallRealMethod().when(setupJdkTask).getCommand();
        Command command = setupJdkTask.getCommand();
        assertEquals("custom", command.getCode());
        assertEquals("Custom", command.getName());
    }

    @Test
    public void testGetCustomCommand() {
        doCallRealMethod().when(setupJdkTask).getCustomCommand();
        assertEquals("setup_jdk", setupJdkTask.getCustomCommand());
    }

    @Test
    public void testGetTaskPO() {
        doCallRealMethod().when(setupJdkTask).getCustomCommand();
        doCallRealMethod().when(setupJdkTask).getName();
        doCallRealMethod().when(setupJdkTask).getCommand();

        setupJdkTask.loadTaskPO(null);
        TaskPO result = setupJdkTask.getTaskPO();

        assertEquals("Setup jdk for TestHostname", result.getName());
        assertEquals("Custom", result.getCommand());

        assertEquals(JsonUtils.writeAsString(taskContext), result.getContext());
        assertEquals("TestHostname", result.getHostname());
        assertEquals("TestServiceName", result.getServiceName());
        assertEquals("TestServiceUser", result.getServiceUser());
        assertEquals("TestComponentName", result.getComponentName());
        assertEquals("setup_jdk", result.getCustomCommand());
    }

    @Test
    public void testGetName() {
        doCallRealMethod().when(setupJdkTask).getName();
        assertEquals("Setup jdk for TestHostname", setupJdkTask.getName());
    }
}
