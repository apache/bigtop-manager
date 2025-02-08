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
package org.apache.bigtop.manager.server.enums;

import org.apache.bigtop.manager.server.utils.MessageSourceUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class ApiExceptionEnumTest {

    @AfterEach
    void tearDown() {
        for (ApiExceptionEnum exception : ApiExceptionEnum.values()) {
            exception.setArgs(null);
        }
    }

    @Test
    void testEnumProperties() {
        assertEquals(10000, ApiExceptionEnum.NEED_LOGIN.getCode());
        assertEquals(LocaleKeys.LOGIN_REQUIRED, ApiExceptionEnum.NEED_LOGIN.getKey());

        assertEquals(11000, ApiExceptionEnum.CLUSTER_NOT_FOUND.getCode());
        assertEquals(LocaleKeys.CLUSTER_NOT_FOUND, ApiExceptionEnum.CLUSTER_NOT_FOUND.getKey());

        assertEquals(12000, ApiExceptionEnum.HOST_NOT_FOUND.getCode());
        assertEquals(LocaleKeys.HOST_NOT_FOUND, ApiExceptionEnum.HOST_NOT_FOUND.getKey());
    }

    @Test
    void testGetMessageWithoutArgs() {
        try (MockedStatic<MessageSourceUtils> mocked = mockStatic(MessageSourceUtils.class)) {
            mocked.when(() -> MessageSourceUtils.getMessage(LocaleKeys.CLUSTER_NOT_FOUND))
                    .thenReturn("Cluster not found");

            String message = ApiExceptionEnum.CLUSTER_NOT_FOUND.getMessage();
            assertEquals("Cluster not found", message);
        }
    }

    @Test
    void testGetMessageWithArgs() {
        try (MockedStatic<MessageSourceUtils> mocked = mockStatic(MessageSourceUtils.class)) {
            String[] args = {"example.txt"};
            mocked.when(() -> MessageSourceUtils.getMessage(LocaleKeys.FILE_UPLOAD_FAILED, args))
                    .thenReturn("File upload failed: example.txt");

            ApiExceptionEnum.FILE_UPLOAD_FAILED.setArgs(args);
            String message = ApiExceptionEnum.FILE_UPLOAD_FAILED.getMessage();
            assertEquals("File upload failed: example.txt", message);
        }
    }

    @Test
    void testSetArgs() {
        String[] args = {"testArg"};
        ApiExceptionEnum.FILE_UPLOAD_FAILED.setArgs(args);
        assertArrayEquals(args, ApiExceptionEnum.FILE_UPLOAD_FAILED.getArgs());
    }

    @Test
    void testNoArgsAfterReset() {
        ApiExceptionEnum.FILE_UPLOAD_FAILED.setArgs(new String[] {"temp"});
        tearDown();
        assertNull(ApiExceptionEnum.FILE_UPLOAD_FAILED.getArgs());
    }
}
