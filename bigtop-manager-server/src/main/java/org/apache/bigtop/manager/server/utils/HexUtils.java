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

import java.util.Objects;

/**
 * Hexadecimal utility class
 */
public class HexUtils {

    private HexUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts a byte array to a hexadecimal string (lowercase)
     *
     * @param bytes the input byte array
     * @return the hexadecimal string representation
     */
    public static String bytesToHex(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes cannot be null");

        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b & 0xff));
        }
        return hex.toString();
    }

    /**
     * Converts a hexadecimal string to a byte array
     *
     * @param hex the input hexadecimal string
     * @return the corresponding byte array
     * @throws IllegalArgumentException if the length of the hex string is not even
     */
    public static byte[] hexToBytes(String hex) {
        Objects.requireNonNull(hex, "hex cannot be null");

        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string length must be even");
        }

        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
