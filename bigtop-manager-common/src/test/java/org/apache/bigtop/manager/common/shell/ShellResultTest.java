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

import org.apache.bigtop.manager.common.constants.MessageConstants;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShellResultTest {

    @Test
    void testExecCommandSuccess() throws IOException {
        // Simulate successful shell command execution
        ShellResult result = ShellResult.success();
        assertEquals(MessageConstants.SUCCESS_CODE, result.getExitCode());
        assertEquals("Run shell success.", result.getOutput());
        assertEquals("", result.getErrMsg());
    }

    @Test
    void testExecCommandFailure() throws IOException {
        // Simulate failed shell command execution
        ShellResult result = ShellResult.fail();
        assertEquals(MessageConstants.FAIL_CODE, result.getExitCode());
        assertEquals("Run shell fail.", result.getOutput());
        assertEquals("", result.getErrMsg());
    }
}
