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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.hive;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HiveParamsTest {

    private HiveParams hiveParams;

    @BeforeEach
    public void setUp() {
        hiveParams = mock(HiveParams.class);
        when(hiveParams.stackHome()).thenReturn("/stack");
        when(hiveParams.getServiceName()).thenCallRealMethod();
        when(hiveParams.serviceHome()).thenCallRealMethod();
        when(hiveParams.hadoopHome()).thenCallRealMethod();
        when(hiveParams.confDir()).thenCallRealMethod();
    }

    @Test
    public void testServiceHome() {
        assertEquals("/stack/hive", hiveParams.serviceHome());
    }

    @Test
    public void testConfDir() {
        assertEquals("/stack/hive/conf", hiveParams.confDir());
    }

    @Test
    public void testHadoopHome() {
        assertEquals("/stack/hadoop", hiveParams.hadoopHome());
    }

    @Test
    public void testGetServiceName() {
        assertEquals("hive", hiveParams.getServiceName());
    }
}
