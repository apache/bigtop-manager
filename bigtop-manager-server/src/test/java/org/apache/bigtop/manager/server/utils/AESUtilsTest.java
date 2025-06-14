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
package org.apache.bigtop.manager.server.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AESUtilsTest {

    @Test
    public void testRandomString() {
        int length = 32;
        String randomString = AESUtils.randomString(length);
        assertEquals(length, randomString.length());
    }

    @Test
    public void testEncryptAndDecrypt() {
        String data = "username";
        String key = AESUtils.randomString(32);
        String encryptedData = AESUtils.encrypt(data, key);
        String decryptedData = AESUtils.decrypt(encryptedData, key);
        assertEquals(data, decryptedData);
    }

    @Test
    public void testGenBcryptAndCheckBcrypt() {
        String rawPassword = "password";
        String hashedPassword = AESUtils.getBcryptPassword(rawPassword);
        boolean check = AESUtils.checkBcryptPassword(rawPassword, hashedPassword);
        assertTrue(check);
    }
}
