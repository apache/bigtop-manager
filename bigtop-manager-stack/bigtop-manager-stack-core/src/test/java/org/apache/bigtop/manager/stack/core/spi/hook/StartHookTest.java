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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class StartHookTest {

    @Mock
    private StartHook startHook;

    @Mock
    private Params params;

    @Test
    public void testDoBefore() {
        doCallRealMethod().when(startHook).doBefore(any());
        startHook.doBefore(params);
        verify(startHook, times(1)).doBefore(params);
    }

    @Test
    public void testDoAfter() {
        doCallRealMethod().when(startHook).doAfter(any());
        startHook.doAfter(params);
        verify(startHook, times(1)).doAfter(params);
    }

    @Test
    public void testGetName() {
        doCallRealMethod().when(startHook).getName();
        String name = startHook.getName();
        assert name.equals("start");
    }
}
