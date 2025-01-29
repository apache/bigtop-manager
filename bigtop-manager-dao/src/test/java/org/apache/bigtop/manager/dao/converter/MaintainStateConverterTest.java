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

import org.apache.bigtop.manager.common.enums.MaintainState;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MaintainStateConverterTest {

    private final MaintainStateConverter converter = new MaintainStateConverter();

    @Test
    void testConvertToDatabaseColumn() {
        assertEquals("Uninstalled", converter.convertToDatabaseColumn(MaintainState.UNINSTALLED));
        assertEquals("Installed", converter.convertToDatabaseColumn(MaintainState.INSTALLED));
        assertEquals("Maintained", converter.convertToDatabaseColumn(MaintainState.MAINTAINED));
        assertEquals("Started", converter.convertToDatabaseColumn(MaintainState.STARTED));
        assertEquals("Stopped", converter.convertToDatabaseColumn(MaintainState.STOPPED));

        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void testConvertToEntityAttribute() {
        assertEquals(MaintainState.UNINSTALLED, converter.convertToEntityAttribute("uninstalled"));
        assertEquals(MaintainState.INSTALLED, converter.convertToEntityAttribute("installed"));
        assertEquals(MaintainState.MAINTAINED, converter.convertToEntityAttribute("maintained"));
        assertEquals(MaintainState.STARTED, converter.convertToEntityAttribute("started"));
        assertEquals(MaintainState.STOPPED, converter.convertToEntityAttribute("stopped"));

        assertNull(converter.convertToEntityAttribute(null));

        assertEquals(MaintainState.UNINSTALLED, converter.convertToEntityAttribute("UNINSTALLED"));
        assertEquals(MaintainState.STARTED, converter.convertToEntityAttribute("STARTED"));
    }

    @Test
    void testConvertToEntityAttributeInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("invalid"));
    }
}
