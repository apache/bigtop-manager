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

import org.apache.bigtop.manager.common.utils.Environments;
import org.apache.bigtop.manager.server.command.task.ComponentCheckTask;
import org.apache.bigtop.manager.server.command.task.Task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ComponentCheckStageTest {

    @Mock
    private ComponentCheckStage stage;

    @Test
    public void testCreateTask() {
        MockedConstruction<?> mocked = mockConstruction(ComponentCheckTask.class);

        doCallRealMethod().when(stage).createTask(any());
        Task task = stage.createTask("host1");

        assertEquals(1, mocked.constructed().size());
        assertInstanceOf(ComponentCheckTask.class, task);

        mocked.close();
    }

    @Test
    public void testGetName() {
        StageContext stageContext = new StageContext();
        stageContext.setServiceName("zookeeper");
        stageContext.setComponentName("zookeeper_server");

        ReflectionTestUtils.setField(stage, "stageContext", stageContext);

        MockedStatic<Environments> mocked = mockStatic(Environments.class);
        when(Environments.isDevMode()).thenReturn(true);

        doCallRealMethod().when(stage).getName();
        assertEquals("Check ZooKeeper Server", stage.getName());

        mocked.close();
    }
}
