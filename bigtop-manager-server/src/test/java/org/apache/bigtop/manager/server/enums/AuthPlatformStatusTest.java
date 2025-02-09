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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthPlatformStatusTest {

    @Test
    // Normal path test: Check if the code for each status is correct
    public void testStatusCodes() {
        assertEquals(1, AuthPlatformStatus.ACTIVE.getCode());
        assertEquals(2, AuthPlatformStatus.AVAILABLE.getCode());
        assertEquals(3, AuthPlatformStatus.UNAVAILABLE.getCode());
    }

    @Test
    // Boundary case test: Check if invalid codes return UNAVAILABLE
    public void testInvalidCode() {
        assertEquals(AuthPlatformStatus.UNAVAILABLE, AuthPlatformStatus.fromCode(0));
        assertEquals(AuthPlatformStatus.UNAVAILABLE, AuthPlatformStatus.fromCode(4));
        assertEquals(AuthPlatformStatus.UNAVAILABLE, AuthPlatformStatus.fromCode(null));
    }

    @Test
    // Normal path test: Check if the isAvailable method works correctly with valid codes
    public void testIsAvailable() {
        assertTrue(AuthPlatformStatus.isAvailable(2));
        assertFalse(AuthPlatformStatus.isAvailable(1));
        assertFalse(AuthPlatformStatus.isAvailable(3));
    }

    @Test
    // Normal path test: Check if the isActive method works correctly with valid codes
    public void testIsActive() {
        assertTrue(AuthPlatformStatus.isActive(1));
        assertFalse(AuthPlatformStatus.isActive(2));
        assertFalse(AuthPlatformStatus.isActive(3));
    }

    @Test
    // Normal path test: Check if the available method works correctly with valid codes
    public void testAvailable() {
        assertTrue(AuthPlatformStatus.available(1));
        assertTrue(AuthPlatformStatus.available(2));
        assertFalse(AuthPlatformStatus.available(3));
    }

    @Test
    // Boundary case test: Check if the available method returns false with invalid codes
    public void testAvailableWithInvalidCode() {
        assertFalse(AuthPlatformStatus.available(0));
        assertFalse(AuthPlatformStatus.available(4));
    }
}
