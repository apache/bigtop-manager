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

public class JobStateTest {
    // Test normal path: verify that the code and name of enum values are correct
    @Test
    public void testJobStateNormalPath() {
        assertEquals("pending", JobState.PENDING.getCode(), "The code of PENDING should be 'pending'");
        assertEquals("Pending", JobState.PENDING.getName(), "The name of PENDING should be 'Pending'");

        assertEquals("processing", JobState.PROCESSING.getCode(), "The code of PROCESSING should be 'processing'");
        assertEquals("Processing", JobState.PROCESSING.getName(), "The name of PROCESSING should be 'Processing'");

        assertEquals("successful", JobState.SUCCESSFUL.getCode(), "The code of SUCCESSFUL should be 'successful'");
        assertEquals("Successful", JobState.SUCCESSFUL.getName(), "The name of SUCCESSFUL should be 'Successful'");

        assertEquals("failed", JobState.FAILED.getCode(), "The code of FAILED should be 'failed'");
        assertEquals("Failed", JobState.FAILED.getName(), "The name of FAILED should be 'Failed'");

        assertEquals("canceled", JobState.CANCELED.getCode(), "The code of CANCELED should be 'canceled'");
        assertEquals("Canceled", JobState.CANCELED.getName(), "The name of CANCELED should be 'Canceled'");
    }

    // Test boundary conditions: verify that the fromString method can correctly convert strings in upper case, lower
    // case, and mixed case
    @Test
    public void testFromStringWithVariousCases() {
        assertEquals(
                JobState.PENDING,
                JobState.fromString("PENDING"),
                "Upper case 'PENDING' should be converted to PENDING enum value");
        assertEquals(
                JobState.PENDING,
                JobState.fromString("pending"),
                "Lower case 'pending' should be converted to PENDING enum value");
        assertEquals(
                JobState.PENDING,
                JobState.fromString("PeNdiNg"),
                "Mixed case 'PeNdiNg' should be converted to PENDING enum value");

        assertEquals(
                JobState.PROCESSING,
                JobState.fromString("PROCESSING"),
                "Upper case 'PROCESSING' should be converted to PROCESSING enum value");
        assertEquals(
                JobState.PROCESSING,
                JobState.fromString("processing"),
                "Lower case 'processing' should be converted to PROCESSING enum value");
        assertEquals(
                JobState.PROCESSING,
                JobState.fromString("PrOcEsSiNg"),
                "Mixed case 'PrOcEsSiNg' should be converted to PROCESSING enum value");

        assertEquals(
                JobState.SUCCESSFUL,
                JobState.fromString("SUCCESSFUL"),
                "Upper case 'SUCCESSFUL' should be converted to SUCCESSFUL enum value");
        assertEquals(
                JobState.SUCCESSFUL,
                JobState.fromString("successful"),
                "Lower case 'successful' should be converted to SUCCESSFUL enum value");
        assertEquals(
                JobState.SUCCESSFUL,
                JobState.fromString("SuCcEsSfUl"),
                "Mixed case 'SuCcEsSfUl' should be converted to SUCCESSFUL enum value");

        assertEquals(
                JobState.FAILED,
                JobState.fromString("FAILED"),
                "Upper case 'FAILED' should be converted to FAILED enum value");
        assertEquals(
                JobState.FAILED,
                JobState.fromString("failed"),
                "Lower case 'failed' should be converted to FAILED enum value");
        assertEquals(
                JobState.FAILED,
                JobState.fromString("FaIlEd"),
                "Mixed case 'FaIlEd' should be converted to FAILED enum value");

        assertEquals(
                JobState.CANCELED,
                JobState.fromString("CANCELED"),
                "Upper case 'CANCELED' should be converted to CANCELED enum value");
        assertEquals(
                JobState.CANCELED,
                JobState.fromString("canceled"),
                "Lower case 'canceled' should be converted to CANCELED enum value");
        assertEquals(
                JobState.CANCELED,
                JobState.fromString("CaNcElEd"),
                "Mixed case 'CaNcElEd' should be converted to CANCELED enum value");
    }

    // Test boundary conditions: verify that the fromString method throws an exception when passed an invalid string
    @Test
    public void testFromStringWithInvalidString() {
        assertThrows(
                IllegalArgumentException.class,
                () -> JobState.fromString("INVALID"),
                "Passing invalid string 'INVALID' should throw an IllegalArgumentException");
        assertThrows(
                IllegalArgumentException.class,
                () -> JobState.fromString(""),
                "Passing empty string '' should throw an IllegalArgumentException");
        assertThrows(
                NullPointerException.class,
                () -> JobState.fromString(null),
                "Passing null should throw a NullPointerException");
    }

    // Test boundary conditions: verify that the toCamelCase method can correctly convert enum values to camel case
    // strings
    @Test
    public void testToCamelCase() {
        assertEquals("Pending", JobState.PENDING.toCamelCase(), "PENDING should be converted to 'Pending'");
        assertEquals("Processing", JobState.PROCESSING.toCamelCase(), "PROCESSING should be converted to 'Processing'");
        assertEquals("Successful", JobState.SUCCESSFUL.toCamelCase(), "SUCCESSFUL should be converted to 'Successful'");
        assertEquals("Failed", JobState.FAILED.toCamelCase(), "FAILED should be converted to 'Failed'");
        assertEquals("Canceled", JobState.CANCELED.toCamelCase(), "CANCELED should be converted to 'Canceled'");
    }
}
