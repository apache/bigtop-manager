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

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAUtils {

    // Default algorithm: RSA/ECB/PKCS1Padding
    private static final String DEFAULT_ALGORITHM = "RSA/ECB/PKCS1Padding";

    /**
     * Encrypt data using public key (OAEP padding is used by default)
     *
     * @param data         The plaintext string to be encrypted
     * @param publicKeyPem The public key in X.509 PEM format
     * @return Base64-encoded encryption result
     * @throws Exception Throws an exception when encryption fails
     */
    public static String encrypt(String data, String publicKeyPem) throws Exception {
        return encrypt(data, publicKeyPem, DEFAULT_ALGORITHM);
    }

    /**
     * Encrypt data using public key (supports custom algorithms)
     *
     * @param data         Plaintext string to be encrypted
     * @param publicKeyPem Public key in X.509 PEM format
     * @param algorithm    Specifies the encryption algorithm, such as "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
     * @return Base64-encoded encryption result
     * @throws Exception Throws an exception when encryption fails
     */
    public static String encrypt(String data, String publicKeyPem, String algorithm) throws Exception {
        validateParameters(data, publicKeyPem);

        byte[] publicKeyBytes = extractKeyBytes(publicKeyPem);
        PublicKey publicKey = generatePublicKey(publicKeyBytes);

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

        byte[] encryptedBytes = cipher.doFinal(dataBytes);
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    /**
     * Decrypt data using private key (OAEP padding is used by default)
     *
     * @param encryptedData Base64-encoded encrypted data
     * @param privateKeyPem PKCS#8 PEM formatted private key
     * @return decrypted plaintext string
     * @throws Exception Throws an exception when decryption fails
     */
    public static String decrypt(String encryptedData, String privateKeyPem) throws Exception {
        return decrypt(encryptedData, privateKeyPem, DEFAULT_ALGORITHM);
    }

    /**
     * Decrypt data using private key (supports custom algorithms)
     *
     * @param encryptedData Base64-encoded encrypted data
     * @param privateKeyPem PKCS#8 PEM formatted private key
     * @param algorithm     specifies the decryption algorithm, such as "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
     * @return decrypted plaintext string
     * @throws Exception Throws an exception when decryption fails
     */
    public static String decrypt(String encryptedData, String privateKeyPem, String algorithm) throws Exception {
        validateParameters(encryptedData, privateKeyPem);

        byte[] privateKeyBytes = extractKeyBytes(privateKeyPem);
        PrivateKey privateKey = generatePrivateKey(privateKeyBytes);

        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    /**
     * Extract the Base64 encoded content in PEM
     */
    private static byte[] extractKeyBytes(String pem) {
        String content = pem.replaceAll("-----[\\s\\w]+KEY-----", "").replaceAll("\\s", "");
        if (content.isEmpty()) {
            throw new IllegalArgumentException("PEM content is empty after cleaning.");
        }
        return Base64.getDecoder().decode(content);
    }

    /**
     * Build a public key object
     */
    private static PublicKey generatePublicKey(byte[] encodedKey) throws Exception {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * Build a private key object
     */
    private static PrivateKey generatePrivateKey(byte[] encodedKey) throws Exception {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * Parameter verification
     */
    private static void validateParameters(String data, String keyPem) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data cannot be null or empty.");
        }
        if (keyPem == null || keyPem.isEmpty()) {
            throw new IllegalArgumentException("PEM key content cannot be null or empty.");
        }
    }

    private static final Base64.Encoder ENCODER = Base64.getEncoder();

    /**
     * Generate RSA Key Pair
     */
    public static KeyPair generateRsaKeyPair(int keySize) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(keySize);
        return kpg.generateKeyPair();
    }

    /**
     * Convert Public Key to PEM format
     */
    public static String toPemPublicKey(PublicKey publicKey) {
        byte[] publicKeyBytes = publicKey.getEncoded();
        String encodedKey = ENCODER.encodeToString(publicKeyBytes);
        return "-----BEGIN PUBLIC KEY-----\n" + formatKey(encodedKey) + "\n-----END PUBLIC KEY-----";
    }

    /**
     * Convert Private Key to PEM format (PKCS#8)
     */
    public static String toPemPrivateKey(PrivateKey privateKey) {
        byte[] privateKeyBytes = privateKey.getEncoded();
        String encodedKey = ENCODER.encodeToString(privateKeyBytes);
        return "-----BEGIN PRIVATE KEY-----\n" + formatKey(encodedKey) + "\n-----END PRIVATE KEY-----";
    }

    /**
     * Format the base64 string into 64-character lines for PEM readability
     */
    private static String formatKey(String key) {
        StringBuilder formattedKey = new StringBuilder();
        for (int i = 0; i < key.length(); i += 64) {
            int end = Math.min(i + 64, key.length());
            String line = key.substring(i, end);
            formattedKey.append(line).append("\n");
        }
        return formattedKey.toString().trim();
    }

    public static String getBcryptPassword(String rawPassword) {
        return Password.hash(rawPassword).withBcrypt().getResult();
    }

    public static boolean checkBcryptPassword(String rawPassword, String hashedPassword) {
        return Password.check(rawPassword, hashedPassword).withBcrypt();
    }
}
