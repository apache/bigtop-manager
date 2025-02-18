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
package org.apache.bigtop.manager.stack.core.spi.script;

import org.apache.bigtop.manager.common.constants.MessageConstants;
import org.apache.bigtop.manager.common.shell.ShellResult;
import org.apache.bigtop.manager.stack.core.spi.param.Params;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class AbstractClientScriptTest {

    @Mock
    private Params params;

    @Test
    public void testStart() {
        MockClientScript clientScript = new MockClientScript();
        ShellResult result = clientScript.start(params);
        assertEquals(MessageConstants.SUCCESS_CODE, result.getExitCode());
    }

    @Test
    public void testStop() {
        MockClientScript clientScript = new MockClientScript();
        ShellResult result = clientScript.stop(params);
        assertEquals(MessageConstants.SUCCESS_CODE, result.getExitCode());
    }

    @Test
    public void testStatus() {
        MockClientScript clientScript = new MockClientScript();
        ShellResult result = clientScript.status(params);
        assertEquals(MessageConstants.SUCCESS_CODE, result.getExitCode());
    }

    private static class MockClientScript extends AbstractClientScript {

        @Override
        public ShellResult start(Params params) {
            return super.start(params);
        }

        @Override
        public ShellResult stop(Params params) {
            return super.stop(params);
        }

        @Override
        public ShellResult status(Params params) {
            return super.status(params);
        }

        @Override
        public String getComponentName() {
            return "MockClientScript";
        }
    }
}
