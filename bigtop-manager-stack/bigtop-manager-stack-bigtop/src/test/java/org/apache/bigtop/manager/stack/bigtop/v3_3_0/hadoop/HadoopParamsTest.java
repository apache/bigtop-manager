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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.hadoop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HadoopParamsTest {

    private HadoopParams hadoopParams;

    @BeforeEach
    public void setUp() {
        hadoopParams = mock(HadoopParams.class);
        when(hadoopParams.stackHome()).thenReturn("/stack");
        when(hadoopParams.getServiceName()).thenCallRealMethod();
        when(hadoopParams.serviceHome()).thenCallRealMethod();
        when(hadoopParams.confDir()).thenCallRealMethod();
        when(hadoopParams.binDir()).thenCallRealMethod();
    }

    @Test
    public void testServiceHome() {
        assertEquals("/stack/hadoop", hadoopParams.serviceHome());
    }

    @Test
    public void testConfDir() {
        assertEquals("/stack/hadoop/etc/hadoop", hadoopParams.confDir());
    }

    @Test
    public void testBinDir() {
        assertEquals("/stack/hadoop/bin", hadoopParams.binDir());
    }

    @Test
    public void testGetServiceName() {
        assertEquals("hadoop", hadoopParams.getServiceName());
    }
}
