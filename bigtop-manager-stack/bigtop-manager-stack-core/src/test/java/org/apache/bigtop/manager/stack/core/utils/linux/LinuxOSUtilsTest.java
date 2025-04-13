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

import org.apache.bigtop.manager.common.shell.ShellExecutor;
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
import static org.mockito.Mockito.mockStatic;

public class LinuxOSUtilsTest {

    private MockedStatic<LinuxAccountUtils> mockLinuxAccountUtils;
    private MockedStatic<ShellExecutor> mockShellExecutor;

    @BeforeEach
    public void setUp() {
        mockLinuxAccountUtils = mockStatic(LinuxAccountUtils.class);
        mockShellExecutor = mockStatic(ShellExecutor.class);
    }

    @AfterEach
    public void tearDown() {
        mockLinuxAccountUtils.close();
        mockShellExecutor.close();
    }

    @Test
    public void testSudoExecCmdWithTenant() throws IOException {
        String command = "echo Hello";
        String tenant = "testUser";

        // Mock the behavior of execCmd method
        mockLinuxAccountUtils.when(() -> LinuxAccountUtils.isUserExists(tenant)).thenReturn(true);
        mockShellExecutor.when(() -> ShellExecutor.execCommand(anyList())).thenReturn(new ShellResult(0, "Hello", ""));

        // Call the method under test
        ShellResult result = LinuxOSUtils.sudoExecCmd(command, tenant);

        // Capture the arguments passed to ShellExecutor.execCommand
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        mockShellExecutor.verify(() -> ShellExecutor.execCommand(captor.capture()));

        // Assert the result
        assertNotNull(result);
        assertEquals(0, result.getExitCode());
        assertTrue(result.getOutput().contains("Hello"));

        // Assert that sudoExecCmd passes the correct parameters
        List<String> commandArgs = captor.getValue();
        assertTrue(commandArgs.contains("sudo"));
        assertTrue(commandArgs.contains("-u"));
        assertTrue(commandArgs.contains(tenant));
        assertTrue(commandArgs.contains("sh"));
        assertTrue(commandArgs.contains("-c"));
        assertTrue(commandArgs.contains(command));
    }

    @Test
    public void testSudoExecCmdWithoutTenant() throws IOException {
        String command = "echo Hello";

        // Mock the behavior of execCmd method
        mockShellExecutor.when(() -> ShellExecutor.execCommand(anyList())).thenReturn(new ShellResult(0, "Hello", ""));

        // Call the method under test with null tenant
        ShellResult result = LinuxOSUtils.sudoExecCmd(command, null);

        // Capture the arguments passed to ShellExecutor.execCommand
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        mockShellExecutor.verify(() -> ShellExecutor.execCommand(captor.capture()));

        // Assert the result
        assertNotNull(result);
        assertEquals(0, result.getExitCode());
        assertTrue(result.getOutput().contains("Hello"));

        // Assert that sudoExecCmd passes "root" as the tenant when tenant is null
        List<String> commandArgs = captor.getValue();
        assertTrue(commandArgs.contains("sudo"));
        assertTrue(commandArgs.contains("-u"));
        assertTrue(commandArgs.contains("root"));
        assertTrue(commandArgs.contains("sh"));
        assertTrue(commandArgs.contains("-c"));
        assertTrue(commandArgs.contains(command));
    }

    @Test
    public void testGetTenantWithExistingUser() {
        String tenant = "testUser";

        // Mock the isUserExists method to return true for the test user
        mockLinuxAccountUtils.when(() -> LinuxAccountUtils.isUserExists(tenant)).thenReturn(true);

        // Call the method under test
        String result = LinuxOSUtils.getTenant(tenant);

        // Assert that the correct tenant is returned
        assertEquals(tenant, result);
    }

    @Test
    public void testGetTenantWithNonExistingUser() {
        String tenant = "nonExistentUser";

        // Mock the isUserExists method to return false for the non-existent user
        mockLinuxAccountUtils.when(() -> LinuxAccountUtils.isUserExists(tenant)).thenReturn(false);

        // Call the method under test
        String result = LinuxOSUtils.getTenant(tenant);

        // Assert that "root" is returned as the default tenant
        assertEquals("root", result);
    }

    @Test
    public void testCheckProcessWhenPidFileDoesNotExist() {
        String filepath = "/path/to/pidfile";

        // Mock the behavior of the file check
        mockShellExecutor.when(() -> LinuxFileUtils.readFile(filepath)).thenThrow(IOException.class);

        // Call the method under test
        ShellResult result = LinuxOSUtils.checkProcess(filepath);

        // Assert that the process is not running
        assertEquals(-1, result.getExitCode());
        assertTrue(result.getErrMsg().contains("Component is not running"));
    }

    @Test
    public void testExecCmd() throws IOException {
        String command = "echo Hello";

        // Mock the behavior of execCmd method
        mockShellExecutor.when(() -> ShellExecutor.execCommand(anyList())).thenReturn(new ShellResult(0, "Hello", ""));

        // Call the method under test
        ShellResult result = LinuxOSUtils.execCmd(command);

        // Assert the result
        assertNotNull(result);
        assertEquals(0, result.getExitCode());
        assertTrue(result.getOutput().contains("Hello"));
    }
}
