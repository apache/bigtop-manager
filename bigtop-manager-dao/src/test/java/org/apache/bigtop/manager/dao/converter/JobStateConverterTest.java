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

import org.apache.bigtop.manager.common.enums.JobState;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JobStateConverterTest {

    private final JobStateConverter converter = new JobStateConverter();

    @Test
    void testConvertToDatabaseColumn() {
        assertEquals("Pending", converter.convertToDatabaseColumn(JobState.PENDING));
        assertEquals("Processing", converter.convertToDatabaseColumn(JobState.PROCESSING));
        assertEquals("Successful", converter.convertToDatabaseColumn(JobState.SUCCESSFUL));
        assertEquals("Failed", converter.convertToDatabaseColumn(JobState.FAILED));
        assertEquals("Canceled", converter.convertToDatabaseColumn(JobState.CANCELED));

        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void testConvertToEntityAttribute() {
        assertEquals(JobState.PENDING, converter.convertToEntityAttribute("pending"));
        assertEquals(JobState.PROCESSING, converter.convertToEntityAttribute("processing"));
        assertEquals(JobState.SUCCESSFUL, converter.convertToEntityAttribute("successful"));
        assertEquals(JobState.FAILED, converter.convertToEntityAttribute("failed"));
        assertEquals(JobState.CANCELED, converter.convertToEntityAttribute("canceled"));

        assertNull(converter.convertToEntityAttribute(null));

        assertEquals(JobState.PENDING, converter.convertToEntityAttribute("PENDING"));
        assertEquals(JobState.FAILED, converter.convertToEntityAttribute("FAILED"));
    }

    @Test
    void testConvertToEntityAttributeInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("invalid"));
    }
}
