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

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Pbkdf2 utility class
 */
public class Pbkdf2Utils {

    private static final Logger logger = LoggerFactory.getLogger(Pbkdf2Utils.class);

    private static final String DEFAULT_SALT = "k9@2Uw3*Y@Ccno9c";

    private Pbkdf2Utils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Derive a key from a password and salt using PBKDF2WithHmacSHA256
     * @param password the user's password
     * @param salt the salt value
     * @return the derived key
     */
    public static byte[] deriveKey(String password, String salt) {
        if (StringUtils.isBlank(password)) {
            return null;
        }

        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 600000, 256);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error("Error generating derive key: ", e);
            return null;
        }
    }

    /**
     * Generate a salt value for a given username
     * @param username the user's username
     * @return the generated salt
     */
    public static String generateSalt(String username) {
        if (StringUtils.isBlank(username)) {
            return null;
        }

        byte[] saltBytes = deriveKey(username, DEFAULT_SALT);
        if (saltBytes == null) {
            return null;
        }
        return HexUtils.bytesToHex(saltBytes);
    }

    /**
     * Get the PBKDF2 password for a given username and password
     * @param username the user's username
     * @param password the user's password
     * @return the PBKDF2 password
     */
    public static String getPbkdf2Password(String username, String password) {
        String salt = generateSalt(username);
        if (StringUtils.isBlank(salt)) {
            return null;
        }
        byte[] key = deriveKey(password, salt);
        return HexUtils.bytesToHex(key);
    }

    /**
     * Get the BCrypt password for a given username and password
     * @param username the user's username
     * @param password the user's password
     * @return the BCrypt password
     */
    public static String getBcryptPassword(String username, String password) {
        String pbkdf2Password = getPbkdf2Password(username, password);
        if (StringUtils.isBlank(pbkdf2Password)) {
            return null;
        }
        return PasswordUtils.getBcryptPassword(pbkdf2Password);
    }
}
