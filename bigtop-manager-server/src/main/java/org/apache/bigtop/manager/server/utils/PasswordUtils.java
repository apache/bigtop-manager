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

import com.password4j.Password;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Password utility class
 */
public class PasswordUtils {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    private static final int BASE = ALPHABET.length();

    private PasswordUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Generate random string
     *
     * @param length the length of the random string to generate
     * @return a random string
     */
    public static String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = ThreadLocalRandom.current().nextInt(BASE);
            sb.append(ALPHABET.charAt(index));
        }
        return sb.toString();
    }

    /**
     * Get BCrypt password
     *
     * @param rawPassword raw password
     * @return hashed password
     */
    public static String getBcryptPassword(String rawPassword) {
        return Password.hash(rawPassword).withBcrypt().getResult();
    }

    /**
     * Verify BCrypt password
     *
     * @param rawPassword    raw password
     * @param hashedPassword hashed password
     * @return verification result
     */
    public static boolean checkBcryptPassword(String rawPassword, String hashedPassword) {
        return Password.check(rawPassword, hashedPassword).withBcrypt();
    }
}
