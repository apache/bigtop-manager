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
package org.apache.bigtop.manager.stack.core.tarball;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class ChecksumValidatorTest {

    @TempDir
    private Path tempDir;

    @Test
    void testValidateChecksum_HappyPath() throws Exception {
        // Create an empty temporary file
        Path tempFilePath = tempDir.resolve("testfile.txt");
        Files.write(tempFilePath, new byte[0]);
        File file = tempFilePath.toFile();

        String expectedChecksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        boolean result = ChecksumValidator.validateChecksum("SHA-256", expectedChecksum, file);

        assertTrue(result, "Checksum validation should succeed for valid case");
    }

    @Test
    void testValidateChecksum_EdgeCase_UnsupportedAlgorithm() throws Exception {
        // Create an empty temporary file
        Path tempFilePath = tempDir.resolve("testfile.txt");
        Files.write(tempFilePath, new byte[0]);
        File file = tempFilePath.toFile();

        boolean result = ChecksumValidator.validateChecksum("INVALID-ALGORITHM", "any-checksum", file);

        assertFalse(result, "Should return false for unsupported algorithm");
    }

    @Test
    void testValidateChecksum_EdgeCase_FileNotExist() {
        // Use a non-existent file path
        File file = tempDir.resolve("non_existent.txt").toFile();
        boolean result = ChecksumValidator.validateChecksum("SHA-256", "any-checksum", file);

        assertFalse(result, "Should return false for non-existent file");
    }

    @Test
    void testValidateChecksum_EdgeCase_EmptyFile() throws Exception {
        // Create an empty temporary file
        Path tempFilePath = tempDir.resolve("emptyfile.txt");
        Files.write(tempFilePath, new byte[0]);
        File file = tempFilePath.toFile();

        String expectedChecksum = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        boolean result = ChecksumValidator.validateChecksum("SHA-256", expectedChecksum, file);

        assertTrue(result, "Checksum validation should succeed for empty file");
    }

    @Test
    void testValidateChecksum_EdgeCase_ChecksumMismatch() throws Exception {
        // Create a temporary file with content
        Path tempFilePath = tempDir.resolve("datafile.txt");
        Files.write(tempFilePath, "test_data".getBytes());
        File file = tempFilePath.toFile();

        boolean result = ChecksumValidator.validateChecksum("SHA-256", "wrong_checksum", file);

        assertFalse(result, "Should return false for checksum mismatch");
    }

    @Test
    void testValidateChecksum_EdgeCase_CaseInsensitive() throws Exception {
        // Create an empty temporary file
        Path tempFilePath = tempDir.resolve("casefile.txt");
        Files.write(tempFilePath, new byte[0]);
        File file = tempFilePath.toFile();

        String expectedChecksum = "E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855";
        boolean result = ChecksumValidator.validateChecksum("sha-256", expectedChecksum, file);

        assertTrue(result, "Checksum validation should be case-insensitive");
    }
}
