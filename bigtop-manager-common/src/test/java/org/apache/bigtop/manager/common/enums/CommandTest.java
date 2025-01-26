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

public class CommandTest {

    // Test normal path: verify that the code and name of enum values are correct
    @Test
    public void testCommandNormalPath() {
        assertEquals("add", Command.ADD.getCode(), "The code of ADD should be 'add'");
        assertEquals("Add", Command.ADD.getName(), "The name of ADD should be 'Add'");

        assertEquals("start", Command.START.getCode(), "The code of START should be 'start'");
        assertEquals("Start", Command.START.getName(), "The name of START should be 'Start'");

        assertEquals("stop", Command.STOP.getCode(), "The code of STOP should be 'stop'");
        assertEquals("Stop", Command.STOP.getName(), "The name of STOP should be 'Stop'");

        assertEquals("restart", Command.RESTART.getCode(), "The code of RESTART should be 'restart'");
        assertEquals("Restart", Command.RESTART.getName(), "The name of RESTART should be 'Restart'");

        assertEquals("check", Command.CHECK.getCode(), "The code of CHECK should be 'check'");
        assertEquals("Check", Command.CHECK.getName(), "The name of CHECK should be 'Check'");

        assertEquals("configure", Command.CONFIGURE.getCode(), "The code of CONFIGURE should be 'configure'");
        assertEquals("Configure", Command.CONFIGURE.getName(), "The name of CONFIGURE should be 'Configure'");

        assertEquals("custom", Command.CUSTOM.getCode(), "The code of CUSTOM should be 'custom'");
        assertEquals("Custom", Command.CUSTOM.getName(), "The name of CUSTOM should be 'Custom'");

        assertEquals("init", Command.INIT.getCode(), "The code of INIT should be 'init'");
        assertEquals("Init", Command.INIT.getName(), "The name of INIT should be 'Init'");

        assertEquals("prepare", Command.PREPARE.getCode(), "The code of PREPARE should be 'prepare'");
        assertEquals("Prepare", Command.PREPARE.getName(), "The name of PREPARE should be 'Prepare'");

        assertEquals("status", Command.STATUS.getCode(), "The code of STATUS should be 'status'");
        assertEquals("Status", Command.STATUS.getName(), "The name of STATUS should be 'Status'");
    }

    // Test boundary conditions: verify that the fromString method can correctly convert strings in upper case, lower
    // case, and mixed case
    @Test
    public void testFromStringWithVariousCases() {
        assertEquals(Command.ADD, Command.fromString("ADD"), "Upper case 'ADD' should be converted to ADD enum value");
        assertEquals(Command.ADD, Command.fromString("add"), "Lower case 'add' should be converted to ADD enum value");
        assertEquals(Command.ADD, Command.fromString("AdD"), "Mixed case 'AdD' should be converted to ADD enum value");

        assertEquals(
                Command.START,
                Command.fromString("START"),
                "Upper case 'START' should be converted to START enum value");
        assertEquals(
                Command.START,
                Command.fromString("start"),
                "Lower case 'start' should be converted to START enum value");
        assertEquals(
                Command.START,
                Command.fromString("StArT"),
                "Mixed case 'StArT' should be converted to START enum value");

        assertEquals(
                Command.STOP, Command.fromString("STOP"), "Upper case 'STOP' should be converted to STOP enum value");
        assertEquals(
                Command.STOP, Command.fromString("stop"), "Lower case 'stop' should be converted to STOP enum value");
        assertEquals(
                Command.STOP, Command.fromString("StOp"), "Mixed case 'StOp' should be converted to STOP enum value");

        assertEquals(
                Command.RESTART,
                Command.fromString("RESTART"),
                "Upper case 'RESTART' should be converted to RESTART enum value");
        assertEquals(
                Command.RESTART,
                Command.fromString("restart"),
                "Lower case 'restart' should be converted to RESTART enum value");
        assertEquals(
                Command.RESTART,
                Command.fromString("ReStArT"),
                "Mixed case 'ReStArT' should be converted to RESTART enum value");

        assertEquals(
                Command.CHECK,
                Command.fromString("CHECK"),
                "Upper case 'CHECK' should be converted to CHECK enum value");
        assertEquals(
                Command.CHECK,
                Command.fromString("check"),
                "Lower case 'check' should be converted to CHECK enum value");
        assertEquals(
                Command.CHECK,
                Command.fromString("ChEcK"),
                "Mixed case 'ChEcK' should be converted to CHECK enum value");

        assertEquals(
                Command.CONFIGURE,
                Command.fromString("CONFIGURE"),
                "Upper case 'CONFIGURE' should be converted to CONFIGURE enum value");
        assertEquals(
                Command.CONFIGURE,
                Command.fromString("configure"),
                "Lower case 'configure' should be converted to CONFIGURE enum value");
        assertEquals(
                Command.CONFIGURE,
                Command.fromString("CoNfIgUrE"),
                "Mixed case 'CoNfIgUrE' should be converted to CONFIGURE enum value");

        assertEquals(
                Command.CUSTOM,
                Command.fromString("CUSTOM"),
                "Upper case 'CUSTOM' should be converted to CUSTOM enum value");
        assertEquals(
                Command.CUSTOM,
                Command.fromString("custom"),
                "Lower case 'custom' should be converted to CUSTOM enum value");
        assertEquals(
                Command.CUSTOM,
                Command.fromString("CuStOm"),
                "Mixed case 'CuStOm' should be converted to CUSTOM enum value");

        assertEquals(
                Command.INIT, Command.fromString("INIT"), "Upper case 'INIT' should be converted to INIT enum value");
        assertEquals(
                Command.INIT, Command.fromString("init"), "Lower case 'init' should be converted to INIT enum value");
        assertEquals(
                Command.INIT, Command.fromString("InIt"), "Mixed case 'InIt' should be converted to INIT enum value");

        assertEquals(
                Command.PREPARE,
                Command.fromString("PREPARE"),
                "Upper case 'PREPARE' should be converted to PREPARE enum value");
        assertEquals(
                Command.PREPARE,
                Command.fromString("prepare"),
                "Lower case 'prepare' should be converted to PREPARE enum value");
        assertEquals(
                Command.PREPARE,
                Command.fromString("PrEpArE"),
                "Mixed case 'PrEpArE' should be converted to PREPARE enum value");

        assertEquals(
                Command.STATUS,
                Command.fromString("STATUS"),
                "Upper case 'STATUS' should be converted to STATUS enum value");
        assertEquals(
                Command.STATUS,
                Command.fromString("status"),
                "Lower case 'status' should be converted to STATUS enum value");
        assertEquals(
                Command.STATUS,
                Command.fromString("StAtUs"),
                "Mixed case 'StAtUs' should be converted to STATUS enum value");
    }

    // Test boundary conditions: verify that the fromString method throws an exception when passed an invalid string
    @Test
    public void testFromStringWithInvalidString() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Command.fromString("INVALID"),
                "Passing invalid string 'INVALID' should throw an IllegalArgumentException");
        assertThrows(
                IllegalArgumentException.class,
                () -> Command.fromString(""),
                "Passing empty string '' should throw an IllegalArgumentException");
        assertThrows(
                NullPointerException.class,
                () -> Command.fromString(null),
                "Passing null should throw a NullPointerException");
    }

    // Test boundary conditions: verify that the toCamelCase method can correctly convert enum values to camel case
    // strings
    @Test
    public void testToCamelCase() {
        assertEquals("Add", Command.ADD.toCamelCase(), "ADD should be converted to 'Add'");
        assertEquals("Start", Command.START.toCamelCase(), "START should be converted to 'Start'");
        assertEquals("Stop", Command.STOP.toCamelCase(), "STOP should be converted to 'Stop'");
        assertEquals("Restart", Command.RESTART.toCamelCase(), "RESTART should be converted to 'Restart'");
        assertEquals("Check", Command.CHECK.toCamelCase(), "CHECK should be converted to 'Check'");
        assertEquals("Configure", Command.CONFIGURE.toCamelCase(), "CONFIGURE should be converted to 'Configure'");
        assertEquals("Custom", Command.CUSTOM.toCamelCase(), "CUSTOM should be converted to 'Custom'");
        assertEquals("Init", Command.INIT.toCamelCase(), "INIT should be converted to 'Init'");
        assertEquals("Prepare", Command.PREPARE.toCamelCase(), "PREPARE should be converted to 'Prepare'");
        assertEquals("Status", Command.STATUS.toCamelCase(), "STATUS should be converted to 'Status'");
    }
}
