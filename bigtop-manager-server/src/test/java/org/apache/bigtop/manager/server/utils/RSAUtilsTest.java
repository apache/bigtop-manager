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

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RSAUtilsTest {

    @Test
    public void testEncryptAndDecrypt() throws Exception {
        // 1. Generate RSA key pairs
        KeyPair keyPair = RSAUtils.generateRsaKeyPair(2048);

        // 2. Retrieve byte arrays for public and private keys
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        // 3. Convert to PEM format string
        String publicKeyPEM = RSAUtils.toPemPublicKey(publicKey);
        String privateKeyPEM = RSAUtils.toPemPrivateKey(privateKey);
        String publicKeyStr = publicKeyPEM.replaceAll("\\s", "");
        String privateKeyStr = privateKeyPEM.replaceAll("\\s", "");

        String rawPassword = "password";
        String encrypt = RSAUtils.encrypt(rawPassword, publicKeyStr);
        String decrypt = RSAUtils.decrypt(encrypt, privateKeyStr);
        assertEquals(rawPassword, decrypt);
    }

    @Test
    public void testGenBcryptAndCheckBcrypt() {
        String rawPassword = "password";
        String hashedPassword = RSAUtils.getBcryptPassword(rawPassword);
        boolean check = RSAUtils.checkBcryptPassword(rawPassword, hashedPassword);
        assertTrue(check);
    }
}
