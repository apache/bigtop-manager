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
package org.apache.bigtop.manager.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MaintainStateTest {

    // Test normal path: verify that the code and name of enum values are correct
    @Test
    public void testMaintainStateNormalPath() {
        assertEquals(
                "uninstalled", MaintainState.UNINSTALLED.getCode(), "The code of UNINSTALLED should be 'uninstalled'");
        assertEquals(
                "Uninstalled", MaintainState.UNINSTALLED.getName(), "The name of UNINSTALLED should be 'Uninstalled'");

        assertEquals("installed", MaintainState.INSTALLED.getCode(), "The code of INSTALLED should be 'installed'");
        assertEquals("Installed", MaintainState.INSTALLED.getName(), "The name of INSTALLED should be 'Installed'");

        assertEquals("maintained", MaintainState.MAINTAINED.getCode(), "The code of MAINTAINED should be 'maintained'");
        assertEquals("Maintained", MaintainState.MAINTAINED.getName(), "The name of MAINTAINED should be 'Maintained'");

        assertEquals("started", MaintainState.STARTED.getCode(), "The code of STARTED should be 'started'");
        assertEquals("Started", MaintainState.STARTED.getName(), "The name of STARTED should be 'Started'");

        assertEquals("stopped", MaintainState.STOPPED.getCode(), "The code of STOPPED should be 'stopped'");
        assertEquals("Stopped", MaintainState.STOPPED.getName(), "The name of STOPPED should be 'Stopped'");
    }

    // Test boundary conditions: verify that the fromString method can correctly convert strings in upper case, lower
    // case, and mixed case
    @Test
    public void testFromStringWithVariousCases() {
        assertEquals(
                MaintainState.UNINSTALLED,
                MaintainState.fromString("UNINSTALLED"),
                "Upper case 'UNINSTALLED' should be converted to UNINSTALLED enum value");
        assertEquals(
                MaintainState.UNINSTALLED,
                MaintainState.fromString("uninstalled"),
                "Lower case 'uninstalled' should be converted to UNINSTALLED enum value");
        assertEquals(
                MaintainState.UNINSTALLED,
                MaintainState.fromString("UnInStAlLeD"),
                "Mixed case 'UnInStAlLeD' should be converted to UNINSTALLED enum value");

        assertEquals(
                MaintainState.INSTALLED,
                MaintainState.fromString("INSTALLED"),
                "Upper case 'INSTALLED' should be converted to INSTALLED enum value");
        assertEquals(
                MaintainState.INSTALLED,
                MaintainState.fromString("installed"),
                "Lower case 'installed' should be converted to INSTALLED enum value");
        assertEquals(
                MaintainState.INSTALLED,
                MaintainState.fromString("InStAlLeD"),
                "Mixed case 'InStAlLeD' should be converted to INSTALLED enum value");

        assertEquals(
                MaintainState.MAINTAINED,
                MaintainState.fromString("MAINTAINED"),
                "Upper case 'MAINTAINED' should be converted to MAINTAINED enum value");
        assertEquals(
                MaintainState.MAINTAINED,
                MaintainState.fromString("maintained"),
                "Lower case 'maintained' should be converted to MAINTAINED enum value");
        assertEquals(
                MaintainState.MAINTAINED,
                MaintainState.fromString("MaInTaInEd"),
                "Mixed case 'MaInTaInEd' should be converted to MAINTAINED enum value");

        assertEquals(
                MaintainState.STARTED,
                MaintainState.fromString("STARTED"),
                "Upper case 'STARTED' should be converted to STARTED enum value");
        assertEquals(
                MaintainState.STARTED,
                MaintainState.fromString("started"),
                "Lower case 'started' should be converted to STARTED enum value");
        assertEquals(
                MaintainState.STARTED,
                MaintainState.fromString("StArTeD"),
                "Mixed case 'StArTeD' should be converted to STARTED enum value");

        assertEquals(
                MaintainState.STOPPED,
                MaintainState.fromString("STOPPED"),
                "Upper case 'STOPPED' should be converted to STOPPED enum value");
        assertEquals(
                MaintainState.STOPPED,
                MaintainState.fromString("stopped"),
                "Lower case 'stopped' should be converted to STOPPED enum value");
        assertEquals(
                MaintainState.STOPPED,
                MaintainState.fromString("StOpPeD"),
                "Mixed case 'StOpPeD' should be converted to STOPPED enum value");
    }

    // Test boundary conditions: verify that the fromString method throws an exception when passed an invalid string
    @Test
    public void testFromStringWithInvalidString() {
        assertThrows(
                IllegalArgumentException.class,
                () -> MaintainState.fromString("INVALID"),
                "Passing invalid string 'INVALID' should throw an IllegalArgumentException");
        assertThrows(
                IllegalArgumentException.class,
                () -> MaintainState.fromString(""),
                "Passing empty string '' should throw an IllegalArgumentException");
        assertThrows(
                NullPointerException.class,
                () -> MaintainState.fromString(null),
                "Passing null should throw a NullPointerException");
    }

    // Test boundary conditions: verify that the toCamelCase method can correctly convert enum values to camel case
    // strings
    @Test
    public void testToCamelCase() {
        assertEquals(
                "Uninstalled",
                MaintainState.UNINSTALLED.toCamelCase(),
                "UNINSTALLED should be converted to 'Uninstalled'");
        assertEquals(
                "Installed", MaintainState.INSTALLED.toCamelCase(), "INSTALLED should be converted to 'Installed'");
        assertEquals(
                "Maintained", MaintainState.MAINTAINED.toCamelCase(), "MAINTAINED should be converted to 'Maintained'");
        assertEquals("Started", MaintainState.STARTED.toCamelCase(), "STARTED should be converted to 'Started'");
        assertEquals("Stopped", MaintainState.STOPPED.toCamelCase(), "STOPPED should be converted to 'Stopped'");
    }
}
