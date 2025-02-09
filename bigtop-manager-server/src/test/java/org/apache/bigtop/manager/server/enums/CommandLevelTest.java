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

public class CommandLevelTest {

    @Test
    public void testFromStringNormalPath() {
        assertEquals(CommandLevel.CLUSTER, CommandLevel.fromString("CLUSTER"));
        assertEquals(CommandLevel.SERVICE, CommandLevel.fromString("SERVICE"));
        assertEquals(CommandLevel.COMPONENT, CommandLevel.fromString("COMPONENT"));
        assertEquals(CommandLevel.HOST, CommandLevel.fromString("HOST"));
    }

    @Test
    public void testFromStringLowercaseInput() {
        assertEquals(CommandLevel.CLUSTER, CommandLevel.fromString("cluster"));
        assertEquals(CommandLevel.SERVICE, CommandLevel.fromString("service"));
        assertEquals(CommandLevel.COMPONENT, CommandLevel.fromString("component"));
        assertEquals(CommandLevel.HOST, CommandLevel.fromString("host"));
    }

    @Test
    public void testFromStringMixedCaseInput() {
        assertEquals(CommandLevel.CLUSTER, CommandLevel.fromString("ClUsTeR"));
        assertEquals(CommandLevel.SERVICE, CommandLevel.fromString("SeRvIcE"));
        assertEquals(CommandLevel.COMPONENT, CommandLevel.fromString("CoMpOnEnT"));
        assertEquals(CommandLevel.HOST, CommandLevel.fromString("HoSt"));
    }

    @Test
    public void testFromStringEmptyStringInput() {
        assertThrows(IllegalArgumentException.class, () -> CommandLevel.fromString(""));
    }

    @Test
    public void testFromStringInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> CommandLevel.fromString("INVALID"));
    }

    @Test
    public void testToLowercaseNormalPath() {
        assertEquals("cluster", CommandLevel.CLUSTER.toLowerCase());
        assertEquals("service", CommandLevel.SERVICE.toLowerCase());
        assertEquals("component", CommandLevel.COMPONENT.toLowerCase());
        assertEquals("host", CommandLevel.HOST.toLowerCase());
    }

    @Test
    public void testToLowercaseRepeatedCall() {
        assertEquals("cluster", CommandLevel.CLUSTER.toLowerCase());
        assertEquals("cluster", CommandLevel.CLUSTER.toLowerCase());
    }
}
