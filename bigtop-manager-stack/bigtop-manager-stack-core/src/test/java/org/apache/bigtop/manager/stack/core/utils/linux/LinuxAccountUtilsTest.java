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
package org.apache.bigtop.manager.stack.core.utils.linux;

import org.apache.bigtop.manager.common.shell.ShellResult;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mockStatic;

public class LinuxAccountUtilsTest {

    private MockedStatic<LinuxAccountUtils> mockStatic;

    @BeforeEach
    public void setUp() {
        mockStatic = mockStatic(LinuxAccountUtils.class);

        // Mock sudoExecCmd method to simulate successful command execution
        mockStatic.when(() -> LinuxAccountUtils.sudoExecCmd(anyList())).thenAnswer(invocation -> {
            List<String> cmd = invocation.getArgument(0);
            return ShellResult.success(cmd.toString());
        });

        // Mock isUserExists and isGroupExists methods to return false by default
        mockStatic.when(() -> LinuxAccountUtils.isUserExists(anyString())).thenReturn(false);
        mockStatic.when(() -> LinuxAccountUtils.isGroupExists(anyString())).thenReturn(false);
    }

    @AfterEach
    public void tearDown() {
        mockStatic.close();
    }

    @Test
    public void testUserDel() {
        String user = "testUser";

        // Mock isUserExists to return true for the test user
        mockStatic.when(() -> LinuxAccountUtils.isUserExists(user)).thenReturn(true);
        mockStatic.when(() -> LinuxAccountUtils.userDel(anyString())).thenCallRealMethod();

        // Capture the arguments passed to sudoExecCmd
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        // Call the method under test
        LinuxAccountUtils.userDel(user);

        // Verify that sudoExecCmd was called and capture the arguments
        mockStatic.verify(() -> LinuxAccountUtils.sudoExecCmd(captor.capture()));

        // Get the captured arguments and perform assertions
        List<String> builderParameters = captor.getValue();
        assertNotNull(builderParameters);
        assertEquals(2, builderParameters.size());
        assertTrue(builderParameters.contains("/usr/sbin/userdel"));
        assertTrue(builderParameters.contains(user));
    }

    @Test
    public void testUserAdd() {
        String user = "testUser";
        String group = "testGroup";

        // Mock isGroupExists to return true for the test group
        mockStatic.when(() -> LinuxAccountUtils.isGroupExists(group)).thenReturn(true);
        mockStatic
                .when(() -> LinuxAccountUtils.userAdd(anyString(), anyString()))
                .thenCallRealMethod();
        mockStatic
                .when(() -> LinuxAccountUtils.userAdd(
                        anyString(), anyString(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(false)))
                .thenCallRealMethod();

        // Capture the arguments passed to sudoExecCmd
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        // Call the method under test
        LinuxAccountUtils.userAdd(user, group);

        // Verify that sudoExecCmd was called and capture the arguments
        mockStatic.verify(() -> LinuxAccountUtils.sudoExecCmd(captor.capture()));

        // Get the captured arguments and perform assertions
        List<String> builderParameters = captor.getValue();
        assertNotNull(builderParameters);
        assertTrue(builderParameters.contains("/usr/sbin/useradd"));
        assertTrue(builderParameters.contains("-m"));
        assertTrue(builderParameters.contains("-g"));
        assertTrue(builderParameters.contains(group));
        assertTrue(builderParameters.contains(user));
    }

    @Test
    public void testGroupDel() {
        String group = "testGroup";

        // Mock isGroupExists to return true for the test group
        mockStatic.when(() -> LinuxAccountUtils.isGroupExists(group)).thenReturn(true);
        mockStatic.when(() -> LinuxAccountUtils.groupDel(anyString())).thenCallRealMethod();

        // Capture the arguments passed to sudoExecCmd
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        // Call the method under test
        LinuxAccountUtils.groupDel(group);

        // Verify that sudoExecCmd was called and capture the arguments
        mockStatic.verify(() -> LinuxAccountUtils.sudoExecCmd(captor.capture()));

        // Get the captured arguments and perform assertions
        List<String> builderParameters = captor.getValue();
        assertNotNull(builderParameters);
        assertEquals(2, builderParameters.size());
        assertTrue(builderParameters.contains("/usr/sbin/groupdel"));
        assertTrue(builderParameters.contains(group));
    }

    @Test
    public void testGroupAdd() {
        String group = "testGroup";

        // Mock isGroupExists to return false, so groupAdd will be called
        mockStatic.when(() -> LinuxAccountUtils.isGroupExists(group)).thenReturn(false);
        mockStatic.when(() -> LinuxAccountUtils.groupAdd(anyString())).thenCallRealMethod();
        mockStatic
                .when(() -> LinuxAccountUtils.groupAdd(anyString(), isNull(), isNull()))
                .thenCallRealMethod();

        // Capture the arguments passed to sudoExecCmd
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        // Call the method under test
        LinuxAccountUtils.groupAdd(group);

        // Verify that sudoExecCmd was called and capture the arguments
        mockStatic.verify(() -> LinuxAccountUtils.sudoExecCmd(captor.capture()));

        // Get the captured arguments and perform assertions
        List<String> builderParameters = captor.getValue();
        assertNotNull(builderParameters);
        assertEquals(2, builderParameters.size());
        assertTrue(builderParameters.contains("/usr/sbin/groupadd"));
        assertTrue(builderParameters.contains(group));
    }

    @Test
    public void testIsUserExists() throws IOException {
        String user = "testUser";

        // Mock sudoExecCmd to simulate successful user existence check
        mockStatic.when(() -> LinuxAccountUtils.isUserExists(user)).thenCallRealMethod();
        mockStatic.when(() -> LinuxAccountUtils.sudoExecCmd(anyList())).thenReturn(new ShellResult(0, "", ""));

        // Call the method under test
        boolean exists = LinuxAccountUtils.isUserExists(user);

        // Perform assertions
        assertTrue(exists);

        // Capture the arguments passed to sudoExecCmd
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        mockStatic.verify(() -> LinuxAccountUtils.sudoExecCmd(captor.capture()));

        // Get the captured arguments and perform assertions
        List<String> builderParameters = captor.getValue();
        assertNotNull(builderParameters);
        assertTrue(builderParameters.contains("sh"));
        assertTrue(builderParameters.contains("-c"));
        assertTrue(builderParameters.get(2).contains("grep"));
    }

    @Test
    public void testIsGroupExists() throws IOException {
        String group = "testGroup";

        // Mock sudoExecCmd to simulate successful group existence check
        mockStatic.when(() -> LinuxAccountUtils.isGroupExists(group)).thenCallRealMethod();
        mockStatic.when(() -> LinuxAccountUtils.sudoExecCmd(anyList())).thenReturn(new ShellResult(0, "", ""));

        // Call the method under test
        boolean exists = LinuxAccountUtils.isGroupExists(group);

        // Perform assertions
        assertTrue(exists);

        // Capture the arguments passed to sudoExecCmd
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        mockStatic.verify(() -> LinuxAccountUtils.sudoExecCmd(captor.capture()));

        // Get the captured arguments and perform assertions
        List<String> builderParameters = captor.getValue();
        assertNotNull(builderParameters);
        assertTrue(builderParameters.contains("sh"));
        assertTrue(builderParameters.contains("-c"));
        assertTrue(builderParameters.get(2).contains("grep"));
    }
}
