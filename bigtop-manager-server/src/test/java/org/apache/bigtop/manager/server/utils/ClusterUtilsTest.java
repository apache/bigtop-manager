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
package org.apache.bigtop.manager.server.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClusterUtilsTest {

    @Test
    public void testIsNoneClusterClusterIdIsNull() {
        assertTrue(ClusterUtils.isNoneCluster(null));
    }

    @Test
    public void testIsNoneClusterClusterIdIsZero() {
        assertTrue(ClusterUtils.isNoneCluster(0L));
    }

    @Test
    public void testIsNoneClusterClusterIdIsPositive() {
        assertFalse(ClusterUtils.isNoneCluster(1L));
    }

    @Test
    public void testIsNoneClusterClusterIdIsNegative() {
        assertFalse(ClusterUtils.isNoneCluster(-1L));
    }

    @Test
    public void testIsNoneClusterClusterIdIsMaxValue() {
        assertFalse(ClusterUtils.isNoneCluster(Long.MAX_VALUE));
    }

    @Test
    public void testIsNoneClusterClusterIdIsMinValue() {
        assertFalse(ClusterUtils.isNoneCluster(Long.MIN_VALUE));
    }
}
