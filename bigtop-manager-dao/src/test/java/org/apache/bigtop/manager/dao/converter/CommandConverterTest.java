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
package org.apache.bigtop.manager.dao.converter;

import org.apache.bigtop.manager.common.enums.Command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandConverterTest {

    private final CommandConverter converter = new CommandConverter();

    @Test
    void testConvertToDatabaseColumn() {
        assertEquals("Add", converter.convertToDatabaseColumn(Command.ADD));
        assertEquals("Start", converter.convertToDatabaseColumn(Command.START));
        assertEquals("Stop", converter.convertToDatabaseColumn(Command.STOP));
        assertEquals("Restart", converter.convertToDatabaseColumn(Command.RESTART));
        assertEquals("Check", converter.convertToDatabaseColumn(Command.CHECK));
        assertEquals("Configure", converter.convertToDatabaseColumn(Command.CONFIGURE));
        assertEquals("Custom", converter.convertToDatabaseColumn(Command.CUSTOM));
        assertEquals("Init", converter.convertToDatabaseColumn(Command.INIT));
        assertEquals("Prepare", converter.convertToDatabaseColumn(Command.PREPARE));
        assertEquals("Status", converter.convertToDatabaseColumn(Command.STATUS));

        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void testConvertToEntityAttribute() {
        assertEquals(Command.ADD, converter.convertToEntityAttribute("add"));
        assertEquals(Command.START, converter.convertToEntityAttribute("start"));
        assertEquals(Command.STOP, converter.convertToEntityAttribute("stop"));
        assertEquals(Command.RESTART, converter.convertToEntityAttribute("restart"));
        assertEquals(Command.CHECK, converter.convertToEntityAttribute("check"));
        assertEquals(Command.CONFIGURE, converter.convertToEntityAttribute("configure"));
        assertEquals(Command.CUSTOM, converter.convertToEntityAttribute("custom"));
        assertEquals(Command.INIT, converter.convertToEntityAttribute("init"));
        assertEquals(Command.PREPARE, converter.convertToEntityAttribute("prepare"));
        assertEquals(Command.STATUS, converter.convertToEntityAttribute("status"));

        assertNull(converter.convertToEntityAttribute(null));

        assertEquals(Command.ADD, converter.convertToEntityAttribute("ADD"));
        assertEquals(Command.START, converter.convertToEntityAttribute("START"));
        assertEquals(Command.STOP, converter.convertToEntityAttribute("STOP"));
    }

    @Test
    void testConvertToEntityAttributeInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("invalid"));
    }
}
