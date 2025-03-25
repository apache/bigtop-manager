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
import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.server.command.task.TaskContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AbstractComponentStageTest {

    @Mock
    private AbstractComponentStage stage;

    @Test
    @SuppressWarnings("unchecked")
    public void testCreateTaskContext() {
        StageContext stageContext = new StageContext();
        stageContext.setServiceName("zookeeper");
        stageContext.setComponentName("zookeeper_server");

        ClusterPO clusterPO = new ClusterPO();
        clusterPO.setId(1L);
        clusterPO.setName("test");
        clusterPO.setRootDir("/opt");
        clusterPO.setUserGroup("test");

        ReflectionTestUtils.setField(stage, "stageContext", stageContext);
        ReflectionTestUtils.setField(stage, "clusterPO", clusterPO);

        MockedStatic<Environments> mocked = mockStatic(Environments.class);
        when(Environments.isDevMode()).thenReturn(true);

        Map<String, List<String>> componentHosts = new HashMap<>();
        componentHosts.put("test", List.of("host1"));

        when(stage.getClusterHosts()).thenReturn(componentHosts);
        doCallRealMethod().when(stage).createTaskContext(any());
        TaskContext taskContext = stage.createTaskContext("host1");

        assertEquals(1L, taskContext.getClusterId());
        assertEquals("test", taskContext.getClusterName());
        assertEquals("zookeeper", taskContext.getServiceName());
        assertEquals("zookeeper_server", taskContext.getComponentName());
        assertEquals("ZooKeeper Server", taskContext.getComponentDisplayName());
        assertEquals("zookeeper", taskContext.getServiceUser());
        assertEquals("test", taskContext.getUserGroup());
        assertEquals("/opt", taskContext.getRootDir());

        Map<String, Object> properties = taskContext.getProperties();
        Map<String, List<String>> clusterHosts = (Map<String, List<String>>) properties.get("clusterHosts");
        assertEquals(1, taskContext.getProperties().size());
        assertEquals(1, clusterHosts.size());
        assertEquals("host1", clusterHosts.get("test").get(0));

        mocked.close();
    }
}
