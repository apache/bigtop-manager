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
package org.apache.bigtop.manager.common.shell;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class ProcessContainerTest {

    @AfterEach
    void tearDown() {
        // Clean up remaining processes in the container
        for (Process process : ProcessContainer.getInstance().values()) {
            ProcessContainer.removeProcess(process);
        }
    }

    @Test
    void testPutAndRemoveProcess() {
        // Mock a Process object
        Process process = Mockito.mock(Process.class);

        // Add process to the container
        ProcessContainer.putProcess(process);

        // Verify that the process count in the container increases
        assertEquals(1, ProcessContainer.processSize());

        // Remove the process
        ProcessContainer.removeProcess(process);

        // Verify that the process count in the container decreases
        assertEquals(0, ProcessContainer.processSize());
    }

    @Test
    void testDestroyAllProcess() {
        // Mock Process objects
        Process process1 = Mockito.mock(Process.class);
        Process process2 = Mockito.mock(Process.class);

        // Add two processes
        ProcessContainer.putProcess(process1);
        ProcessContainer.putProcess(process2);

        // Call the destroyAllProcess method
        ProcessContainer.destroyAllProcess();

        // Verify that the destroy method is called
        verify(process1).destroy();
        verify(process2).destroy();
    }
}
