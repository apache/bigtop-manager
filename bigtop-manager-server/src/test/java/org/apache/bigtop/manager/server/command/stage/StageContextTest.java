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

import org.apache.bigtop.manager.dao.po.ClusterPO;
import org.apache.bigtop.manager.dao.repository.ClusterDao;
import org.apache.bigtop.manager.server.enums.CommandLevel;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.dto.HostDTO;
import org.apache.bigtop.manager.server.model.dto.command.ClusterCommandDTO;
import org.apache.bigtop.manager.server.model.dto.command.HostCommandDTO;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StageContextTest {

    @Mock
    private ClusterDao clusterDao;

    private MockedStatic<SpringContextHolder> springContextHolderMockedStatic;

    @BeforeEach
    void setUp() {
        springContextHolderMockedStatic = mockStatic(SpringContextHolder.class);
        when(SpringContextHolder.getBean(ClusterDao.class)).thenReturn(clusterDao);
    }

    @AfterEach
    void tearDown() {
        springContextHolderMockedStatic.close();
    }

    @Test
    void testWithClusterIdAndClusterCommand() {
        // Mock database cluster info
        ClusterPO mockCluster = new ClusterPO();
        mockCluster.setId(1L);
        mockCluster.setName("test-cluster");
        mockCluster.setUserGroup("test-user-group");
        mockCluster.setRootDir("/data/bigtop");
        when(clusterDao.findById(1L)).thenReturn(mockCluster);

        // Create test HostDTOs
        HostDTO host1 = new HostDTO();
        host1.setHostnames(List.of("host1.example.com"));
        HostDTO host2 = new HostDTO();
        host2.setHostnames(List.of("host2.example.com"));

        // Build CLUSTER-level command
        ClusterCommandDTO clusterCommand = new ClusterCommandDTO();
        clusterCommand.setHosts(List.of(host1, host2));
        clusterCommand.setName("should-not-use-this-name");
        clusterCommand.setUserGroup("should-not-use-this-group");
        clusterCommand.setRootDir("/should/not/use/this");

        CommandDTO commandDTO = new CommandDTO();
        commandDTO.setClusterId(1L);
        commandDTO.setCommandLevel(CommandLevel.CLUSTER);
        commandDTO.setClusterCommand(clusterCommand);

        // Execute conversion
        StageContext context = StageContext.fromCommandDTO(commandDTO);

        // Verify cluster info comes from database
        assertEquals(1L, context.getClusterId());
        assertEquals("test-cluster", context.getClusterName());
        assertEquals("test-user-group", context.getUserGroup());
        assertEquals("/data/bigtop", context.getRootDir());

        // Verify hostnames not set (skip processing when clusterId exists)
        assertNull(context.getHostnames());

        // Ensure redundant command info is not used
        assertEquals("test-cluster", context.getClusterName());
    }

    @Test
    void testClusterCommandWithoutClusterId() {
        // Create test HostDTOs
        HostDTO host1 = new HostDTO();
        host1.setHostnames(List.of("hostA.example.com"));
        HostDTO host2 = new HostDTO();
        host2.setHostnames(List.of("hostB.example.com"));

        // Build CLUSTER-level command (no clusterId)
        ClusterCommandDTO clusterCommand = new ClusterCommandDTO();
        clusterCommand.setHosts(List.of(host1, host2));
        clusterCommand.setName("new-cluster");
        clusterCommand.setUserGroup("new-group");
        clusterCommand.setRootDir("/new/root");

        CommandDTO commandDTO = new CommandDTO();
        commandDTO.setClusterId(null);
        commandDTO.setCommandLevel(CommandLevel.CLUSTER);
        commandDTO.setClusterCommand(clusterCommand);

        // Execute conversion
        StageContext context = StageContext.fromCommandDTO(commandDTO);

        // Verify info comes from command payload
        assertNull(context.getClusterId());
        assertEquals(List.of("hostA.example.com", "hostB.example.com"), context.getHostnames());
        assertEquals("new-cluster", context.getClusterName());
        assertEquals("new-group", context.getUserGroup());
        assertEquals("/new/root", context.getRootDir());
    }

    @Test
    void testHostCommand() {
        // Create HostCommandDTO test objects
        HostCommandDTO host1 = new HostCommandDTO();
        host1.setHostnames(List.of("host-01.example.com", "host-01-alias.example.com"));
        HostCommandDTO host2 = new HostCommandDTO();
        host2.setHostnames(List.of("host-02.example.com"));

        CommandDTO commandDTO = new CommandDTO();
        commandDTO.setCommandLevel(CommandLevel.HOST);
        commandDTO.setHostCommands(List.of(host1, host2));

        // Execute conversion
        StageContext context = StageContext.fromCommandDTO(commandDTO);

        // Verify merged hostnames
        assertEquals(
                List.of("host-01.example.com", "host-01-alias.example.com", "host-02.example.com"),
                context.getHostnames());

        // Verify other fields not set
        assertNull(context.getClusterId());
        assertNull(context.getClusterName());
    }

    @Test
    void testServiceCommand() {
        // Build SERVICE-level command
        CommandDTO commandDTO = new CommandDTO();
        commandDTO.setCommandLevel(CommandLevel.SERVICE);

        StageContext context = StageContext.fromCommandDTO(commandDTO);

        // Verify no other fields set
        assertNull(context.getClusterId());
        assertNull(context.getHostnames());
    }

    @Test
    void testComponentCommand() {
        // Build component level command
        CommandDTO commandDTO = new CommandDTO();
        commandDTO.setCommandLevel(CommandLevel.COMPONENT);

        StageContext context = StageContext.fromCommandDTO(commandDTO);

        // Verify no other fields set
        assertNull(context.getClusterId());
        assertNull(context.getHostnames());
    }
}
