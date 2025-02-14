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
package org.apache.bigtop.manager.stack.core.spi.hook;

import org.apache.bigtop.manager.stack.core.spi.param.Params;
import org.apache.bigtop.manager.stack.core.utils.linux.LinuxAccountUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AddHookTest {

    @Mock
    private AddHook addHook;

    @Mock
    private Params params;

    @Test
    public void testDoBefore() {
        doNothing().when(addHook).addUserAndGroup(any());
        doCallRealMethod().when(addHook).doBefore(any());
        addHook.doBefore(params);
        verify(addHook, times(1)).addUserAndGroup(params);
    }

    @Test
    public void testDoAfter() {
        doCallRealMethod().when(addHook).doAfter(any());
        addHook.doAfter(params);
        verify(addHook, never()).addUserAndGroup(params);
    }

    @Test
    public void testGetName() {
        doCallRealMethod().when(addHook).getName();
        String name = addHook.getName();
        assert name.equals("add");
    }

    @Test
    public void testAddUserAndGroup() {
        try (MockedStatic<LinuxAccountUtils> linuxAccountUtilsMockedStatic = mockStatic(LinuxAccountUtils.class)) {
            when(params.user()).thenReturn("testUser");
            when(params.group()).thenReturn("testGroup1");
            doCallRealMethod().when(addHook).addUserAndGroup(any());

            linuxAccountUtilsMockedStatic
                    .when(() -> LinuxAccountUtils.userAdd(any(), any()))
                    .thenAnswer(invocation -> null);
            linuxAccountUtilsMockedStatic
                    .when(() -> LinuxAccountUtils.groupAdd(any()))
                    .thenAnswer(invocation -> null);

            linuxAccountUtilsMockedStatic
                    .when(() -> LinuxAccountUtils.getUserPrimaryGroup(any()))
                    .thenReturn("testGroup1");
            addHook.addUserAndGroup(params);
            linuxAccountUtilsMockedStatic.verify(() -> LinuxAccountUtils.userAdd(any(), any()), never());

            linuxAccountUtilsMockedStatic
                    .when(() -> LinuxAccountUtils.getUserPrimaryGroup(any()))
                    .thenReturn("testGroup2");
            addHook.addUserAndGroup(params);
            linuxAccountUtilsMockedStatic.verify(() -> LinuxAccountUtils.userAdd(any(), any()), times(1));
        }
    }
}
