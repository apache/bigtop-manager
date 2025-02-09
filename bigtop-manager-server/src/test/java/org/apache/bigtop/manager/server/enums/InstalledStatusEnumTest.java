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

public class InstalledStatusEnumTest {

    @Test
    // Test normal case: INSTALLING
    public void testFromCodeNormalCase_INSTALLING() {
        assertEquals(InstalledStatusEnum.INSTALLING, InstalledStatusEnum.fromCode(1));
    }

    @Test
    // Test normal case: SUCCESS
    public void testFromCodeNormalCase_SUCCESS() {
        assertEquals(InstalledStatusEnum.SUCCESS, InstalledStatusEnum.fromCode(2));
    }

    @Test
    // Test normal case: FAILED
    public void testFromCodeNormalCase_FAILED() {
        assertEquals(InstalledStatusEnum.FAILED, InstalledStatusEnum.fromCode(3));
    }

    @Test
    // Test boundary case: less than minimum value
    public void testFromCodeBoundaryCaseLessThanMinValue() {
        assertEquals(InstalledStatusEnum.FAILED, InstalledStatusEnum.fromCode(0));
    }

    @Test
    // Test boundary case: greater than maximum value
    public void testFromCodeBoundaryCaseGreaterThanMaxValue() {
        assertEquals(InstalledStatusEnum.FAILED, InstalledStatusEnum.fromCode(4));
    }

    @Test
    // Test boundary case: null value
    public void testFromCodeBoundaryCaseNullValue() {
        assertEquals(InstalledStatusEnum.FAILED, InstalledStatusEnum.fromCode(null));
    }

    @Test
    // Test boundary case: negative number
    public void testFromCodeBoundaryCaseNegativeNumber() {
        assertEquals(InstalledStatusEnum.FAILED, InstalledStatusEnum.fromCode(-1));
    }
}
