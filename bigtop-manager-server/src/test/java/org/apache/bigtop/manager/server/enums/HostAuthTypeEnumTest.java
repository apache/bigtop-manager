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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HostAuthTypeEnumTest {

    @Test
    public void testFromCodeNormalPath() {
        // Test normal path
        assertEquals(HostAuthTypeEnum.PASSWORD, HostAuthTypeEnum.fromCode(1));
        assertEquals(HostAuthTypeEnum.KEY, HostAuthTypeEnum.fromCode(2));
        assertEquals(HostAuthTypeEnum.NO_AUTH, HostAuthTypeEnum.fromCode(3));
    }

    @Test
    public void testFromCodeEdgeCases() {
        // Test below minimum value
        assertThrows(IllegalArgumentException.class, () -> HostAuthTypeEnum.fromCode(0));
        // Test above maximum value
        assertThrows(IllegalArgumentException.class, () -> HostAuthTypeEnum.fromCode(4));
        // Test null value
        assertThrows(IllegalArgumentException.class, () -> HostAuthTypeEnum.fromCode(null));
        // Test negative value
        assertThrows(IllegalArgumentException.class, () -> HostAuthTypeEnum.fromCode(-1));
    }
}
