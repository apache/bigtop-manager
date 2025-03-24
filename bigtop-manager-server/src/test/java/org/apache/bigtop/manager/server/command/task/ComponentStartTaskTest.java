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
import org.apache.bigtop.manager.dao.po.ComponentPO;
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

import java.util.ArrayList;
import java.util.List;

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
public class ComponentStartTaskTest {

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

    private ComponentStartTask componentStartTask;

    @BeforeEach
    public void setUp() {
        springContextHolderMockedStatic = mockStatic(SpringContextHolder.class);
        when(SpringContextHolder.getBean(HostDao.class)).thenReturn(hostDao);
        when(SpringContextHolder.getBean(TaskDao.class)).thenReturn(taskDao);
        when(SpringContextHolder.getBean(ComponentDao.class)).thenReturn(componentDao);

        componentStartTask = mock(ComponentStartTask.class);

        taskContext.setComponentDisplayName("TestComponentDisplayName");
        taskContext.setHostname("TestHostname");
        taskContext.setServiceName("TestServiceName");
        taskContext.setServiceUser("TestServiceUser");
        taskContext.setComponentName("TestComponentName");
        taskContext.setClusterId(123L);

        doCallRealMethod().when(componentStartTask).setTaskContextForTest(any());
        componentStartTask.setTaskContextForTest(taskContext);

        doCallRealMethod().when(componentStartTask).injectBeans();
        componentStartTask.injectBeans();

        doCallRealMethod().when(componentStartTask).loadTaskPO(any());
        lenient().when(componentStartTask.getTaskPO()).thenCallRealMethod();
        componentStartTask.loadTaskPO(taskPO);
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
        doCallRealMethod().when(componentStartTask).getCommand();
        Command command = componentStartTask.getCommand();
        assertEquals("start", command.getCode());
        assertEquals("Start", command.getName());
    }

    @Test
    public void testOnSuccess() {
        doCallRealMethod().when(componentStartTask).onSuccess();
        List<ComponentPO> componentPOS = new ArrayList<>();
        componentPOS.add(new ComponentPO());
        when(componentDao.findByQuery(any())).thenReturn(componentPOS);

        componentStartTask.onSuccess();
        verify(taskDao, times(1)).partialUpdateById(any());
        verify(componentDao, times(1)).partialUpdateById(any());
    }


    @Test
    public void tesGetTaskPO() {
        doCallRealMethod().when(componentStartTask).getName();
        doCallRealMethod().when(componentStartTask).getCommand();

        componentStartTask.loadTaskPO(null);
        TaskPO result = componentStartTask.getTaskPO();

        assertEquals("Start TestComponentDisplayName on TestHostname", result.getName());
        assertEquals("Start", result.getCommand());

        assertEquals(JsonUtils.writeAsString(taskContext), result.getContext());
        assertEquals("TestHostname", result.getHostname());
        assertEquals("TestServiceName", result.getServiceName());
        assertEquals("TestServiceUser", result.getServiceUser());
        assertEquals("TestComponentName", result.getComponentName());
        assertNull(result.getCustomCommand());
    }

    @Test
    public void testGetName() {
        doCallRealMethod().when(componentStartTask).getName();
        assertEquals("Start TestComponentDisplayName on TestHostname", componentStartTask.getName());
    }
}
