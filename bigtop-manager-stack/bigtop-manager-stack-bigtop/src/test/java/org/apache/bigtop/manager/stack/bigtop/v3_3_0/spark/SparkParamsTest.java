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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.spark;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SparkParamsTest {

    @Test
    public void testHadoopConfDir() {
        SparkParams params = mock(SparkParams.class);
        when(params.stackHome()).thenReturn("/opt/stack");
        when(params.hadoopHome()).thenCallRealMethod();
        when(params.hadoopConfDir()).thenCallRealMethod();
        assertEquals("/opt/stack/hadoop/etc/hadoop", params.hadoopConfDir());
    }

    @Test
    public void testHiveConfDir() {
        SparkParams params = mock(SparkParams.class);
        when(params.stackHome()).thenReturn("/opt/stack");
        when(params.hiveHome()).thenCallRealMethod();
        when(params.hiveConfDir()).thenCallRealMethod();
        assertEquals("/opt/stack/hive/conf", params.hiveConfDir());
    }

    @Test
    public void testGetServiceName() {
        SparkParams params = new SparkParams();
        assertEquals("spark", params.getServiceName());
    }
}
