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
package org.apache.bigtop.manager.agent.grpc.interceptor;

import org.apache.bigtop.manager.common.utils.ProjectPathUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class TaskInterceptorTest {

    @Mock
    private Logger log;

    private TaskInterceptor taskInterceptor;
    private File tempFile;

    @BeforeEach
    public void setUp() {
        taskInterceptor = new TaskInterceptor();
    }

    @AfterEach
    public void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    public void testTruncateLogFileFileExists() throws IOException {
        Long taskId = 1L;
        tempFile = File.createTempFile("logfile", ".log");

        try (MockedStatic<ProjectPathUtils> projectPathUtils = mockStatic(ProjectPathUtils.class)) {
            projectPathUtils.when(() -> ProjectPathUtils.getLogFilePath(taskId)).thenReturn(tempFile.getAbsolutePath());

            taskInterceptor.truncateLogFile(taskId);

            projectPathUtils.verify(() -> ProjectPathUtils.getLogFilePath(taskId), times(1));
            assertTrue(tempFile.exists());
            assertTrue(tempFile.length() == 0);
        }
    }

    @Test
    public void testTruncateLogFileFileDoesNotExist() throws IOException {
        Long taskId = 1L;
        String filePath = "path/to/nonexistentfile.log";

        try (MockedStatic<ProjectPathUtils> projectPathUtils = mockStatic(ProjectPathUtils.class)) {
            projectPathUtils.when(() -> ProjectPathUtils.getLogFilePath(taskId)).thenReturn(filePath);

            taskInterceptor.truncateLogFile(taskId);

            projectPathUtils.verify(() -> ProjectPathUtils.getLogFilePath(taskId), times(1));
            verifyNoInteractions(log);
        }
    }

    @Test
    public void testIsTaskRequestTaskRequest() {
        TaskLogRequest taskLogRequest = new TaskLogRequest();

        Boolean result = taskInterceptor.isTaskRequest(taskLogRequest);

        assertTrue(result);
    }

    @Test
    public void testIsTaskRequestTaskWithGetTaskIdMethod() {
        Task task = new Task();

        Boolean result = taskInterceptor.isTaskRequest(task);

        assertTrue(result);
    }

    @Test
    public void testIsTaskRequestTaskWithoutGetTaskIdMethod() {
        Object obj = new Object();

        Boolean result = taskInterceptor.isTaskRequest(obj);

        assertFalse(result);
    }

    @Test
    public void testIsTaskRequestNullObject() {
        Boolean result = taskInterceptor.isTaskRequest(null);

        assertFalse(result);
    }

    private static class Task {
        public Long getTaskId() {
            return 1L;
        }
    }

    private static class TaskLogRequest {
        public Long getTaskId() {
            return 1L;
        }
    }
}
