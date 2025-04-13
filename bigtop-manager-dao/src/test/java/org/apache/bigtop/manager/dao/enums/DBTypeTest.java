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
package org.apache.bigtop.manager.dao.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DBTypeTest {

    @Test
    public void testToTypeMysql() {
        String databaseId = "mysql";
        DBType expected = DBType.MYSQL;
        DBType result = DBType.toType(databaseId);
        assertEquals(expected, result);
    }

    @Test
    public void testToTypePostgresql() {
        String databaseId = "postgresql";
        DBType expected = DBType.POSTGRESQL;
        DBType result = DBType.toType(databaseId);
        assertEquals(expected, result);
    }

    @Test
    public void testToTypeDm() {
        String databaseId = "dm";
        DBType expected = DBType.DM;
        DBType result = DBType.toType(databaseId);
        assertEquals(expected, result);
    }

    @Test
    public void testToTypeUnsupportedDatabase() {
        String databaseId = "unsupported";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            DBType.toType(databaseId);
        });
        assertEquals("Unsupported database: unsupported", exception.getMessage());
    }
}
