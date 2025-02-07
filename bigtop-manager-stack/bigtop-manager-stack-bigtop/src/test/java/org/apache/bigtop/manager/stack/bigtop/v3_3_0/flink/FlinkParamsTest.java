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
package org.apache.bigtop.manager.stack.bigtop.v3_3_0.flink;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FlinkParamsTest {

    @Test
    public void testHadoopConfDir() {
        FlinkParams flinkParams = mock(FlinkParams.class);
        when(flinkParams.stackHome()).thenReturn("/usr/local");
        when(flinkParams.hadoopHome()).thenCallRealMethod();
        when(flinkParams.hadoopConfDir()).thenCallRealMethod();

        String expected = "/usr/local/hadoop/etc/hadoop";
        assertEquals(expected, flinkParams.hadoopConfDir());
    }

    @Test
    public void testHadoopHome() {
        FlinkParams flinkParams = mock(FlinkParams.class);
        when(flinkParams.stackHome()).thenReturn("/usr/local");
        when(flinkParams.hadoopHome()).thenCallRealMethod();

        String expected = "/usr/local/hadoop";
        assertEquals(expected, flinkParams.hadoopHome());
    }

    @Test
    public void testGetServiceName() {
        FlinkParams flinkParams = new FlinkParams();
        String expected = "flink";
        assertEquals(expected, flinkParams.getServiceName());
    }
}
