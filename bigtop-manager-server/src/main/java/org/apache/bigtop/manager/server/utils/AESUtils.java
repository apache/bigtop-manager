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

import com.password4j.Password;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

/**
 * AES encryption and decryption utility class
 */
public class AESUtils {

    private static final Logger logger = LoggerFactory.getLogger(AESUtils.class);

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final int BASE = ALPHABET.length();

    /**
     * Base64 encoding and decoding
     */
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    /**
     * Algorithm / Encryption Mode / Padding Scheme
     */
    private static final String AES_PKCS5P = "AES/ECB/PKCS5Padding";

    public static String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = ThreadLocalRandom.current().nextInt(BASE);
            sb.append(ALPHABET.charAt(index));
        }
        return sb.toString();
    }

    /**
     * Standard Base64 encoding
     */
    public static String base64Encode(String data) {
        return BASE64_ENCODER.encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Standard Base64 decoding
     */
    public static String base64Decode(String encodedData) {
        byte[] decodedBytes = BASE64_DECODER.decode(encodedData);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    /**
     * Encrypt string
     *
     * @param str  string to be encrypted
     * @param key  encryption key
     * @return encrypted string
     */
    public static String encrypt(String str, String key) {
        validateParams(str, key);

        try {
            byte[] raw = key.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            // "Algorithm/Mode/Padding"
            Cipher cipher = Cipher.getInstance(AES_PKCS5P);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] encrypted = cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));
            // Use BASE64 encoding here, which can also serve as a second layer of encryption
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            logger.error("Encryption error", ex);
            return null;
        }
    }

    /**
     * Decrypt string
     *
     * @param str  string to be decrypted
     * @param key  decryption key
     * @return decrypted string
     */
    public static String decrypt(String str, String key) {
        validateParams(str, key);

        try {
            byte[] raw = key.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance(AES_PKCS5P);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            // First decode using base64
            byte[] decrypted = Base64.getDecoder().decode(str);
            try {
                byte[] original = cipher.doFinal(decrypted);
                return new String(original, StandardCharsets.UTF_8);
            } catch (Exception e) {
                logger.error("Decryption error", e);
                return null;
            }
        } catch (Exception ex) {
            logger.error("Decryption error", ex);
            return null;
        }
    }

    /**
     * Parameter validation
     */
    private static void validateParams(String str, String key) {
        if (StringUtils.isBlank(str)) {
            throw new IllegalArgumentException("str cannot be empty");
        }
        if (StringUtils.isBlank(key)) {
            throw new IllegalArgumentException("key cannot be empty");
        }
        if (key.length() != 16 && key.length() != 32) {
            throw new IllegalArgumentException(
                    "Key length must be 16 characters (128 bits) or 32 characters (256 bits)");
        }
    }

    /**
     * Get BCrypt password
     * @param rawPassword raw password
     * @return hashed password
     */
    public static String getBcryptPassword(String rawPassword) {
        return Password.hash(rawPassword).withBcrypt().getResult();
    }

    /**
     * Verify BCrypt password
     * @param rawPassword raw password
     * @param hashedPassword hashed password
     * @return verification result
     */
    public static boolean checkBcryptPassword(String rawPassword, String hashedPassword) {
        return Password.check(rawPassword, hashedPassword).withBcrypt();
    }
}
