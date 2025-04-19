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

import org.apache.bigtop.manager.stack.core.exception.StackException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TarballExtractorTest {

    private Path tempDir;
    private Path destDir;

    @BeforeEach
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("tarball-extractor-test");
        destDir = Files.createTempDirectory("tarball-extractor-dest");
    }

    @AfterEach
    public void tearDown() throws IOException {
        deleteDirectory(tempDir);
        deleteDirectory(destDir);
    }

    private void deleteDirectory(Path directory) throws IOException {
        try (var stream = Files.walk(directory)) {
            stream.sorted((p1, p2) -> -p1.compareTo(p2)).map(Path::toFile).forEach(File::delete);
        }
    }

    @Test
    public void testExtractTar() throws IOException, StackException {
        Path tarFile = tempDir.resolve("test.tar");
        createTarFile(tarFile, "test.txt", "Hello, World!".getBytes());

        TarballExtractor.extractTarball(tarFile.toString(), destDir.toString());

        Path extractedFile = destDir.resolve("test.txt");
        assertTrue(Files.exists(extractedFile));
        assertEquals("Hello, World!", new String(Files.readAllBytes(extractedFile)));
    }

    @Test
    public void testExtractTarGz() throws IOException, StackException {
        Path tarGzFile = tempDir.resolve("test.tar.gz");
        createTarGzFile(tarGzFile, "test.txt", "Hello, World!".getBytes());

        TarballExtractor.extractTarball(tarGzFile.toString(), destDir.toString());

        Path extractedFile = destDir.resolve("test.txt");
        assertTrue(Files.exists(extractedFile));
        assertEquals("Hello, World!", new String(Files.readAllBytes(extractedFile)));
    }

    @Test
    public void testExtractTarXz() throws IOException, StackException {
        Path tarXzFile = tempDir.resolve("test.tar.xz");
        createTarXzFile(tarXzFile, "test.txt", "Hello, World!".getBytes());

        TarballExtractor.extractTarball(tarXzFile.toString(), destDir.toString());

        Path extractedFile = destDir.resolve("test.txt");
        assertTrue(Files.exists(extractedFile));
        assertEquals("Hello, World!", new String(Files.readAllBytes(extractedFile)));
    }

    @Test
    public void testExtractWithSkipLevels() throws IOException, StackException {
        Path tarFile = tempDir.resolve("test.tar");
        createTarFileWithSubdirectory(tarFile, "dir/test.txt", "Hello, World!".getBytes());

        TarballExtractor.extractTarball(tarFile.toString(), destDir.toString(), 1);

        Path extractedFile = destDir.resolve("test.txt");
        assertTrue(Files.exists(extractedFile));
        assertEquals("Hello, World!", new String(Files.readAllBytes(extractedFile)));
    }

    @Test
    public void testExtractSymbolicLink() throws IOException, StackException {
        Path tarFile = tempDir.resolve("test.tar");
        createTarFileWithSymbolicLink(tarFile, "test.txt", "link_to_test.txt");

        TarballExtractor.extractTarball(tarFile.toString(), destDir.toString());

        Path linkFile = destDir.resolve("link_to_test.txt");
        assertTrue(Files.exists(linkFile));
        assertTrue(Files.isSymbolicLink(linkFile));

        Path targetPath = Files.readSymbolicLink(linkFile).getFileName();
        assertEquals("test.txt", targetPath.toString());
    }

    private void createTarFile(Path tarFile, String entryName, byte[] content) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(tarFile.toFile());
                TarArchiveOutputStream tos = new TarArchiveOutputStream(fos)) {

            TarArchiveEntry entry = new TarArchiveEntry(entryName);
            entry.setSize(content.length);
            tos.putArchiveEntry(entry);
            tos.write(content);
            tos.closeArchiveEntry();
        }
    }

    private void createTarGzFile(Path tarGzFile, String entryName, byte[] content) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(tarGzFile.toFile());
                GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(fos);
                TarArchiveOutputStream tos = new TarArchiveOutputStream(gzos)) {

            TarArchiveEntry entry = new TarArchiveEntry(entryName);
            entry.setSize(content.length);
            tos.putArchiveEntry(entry);
            tos.write(content);
            tos.closeArchiveEntry();
        }
    }

    private void createTarXzFile(Path tarXzFile, String entryName, byte[] content) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(tarXzFile.toFile());
                XZCompressorOutputStream xzos = new XZCompressorOutputStream(fos);
                TarArchiveOutputStream tos = new TarArchiveOutputStream(xzos)) {

            TarArchiveEntry entry = new TarArchiveEntry(entryName);
            entry.setSize(content.length);
            tos.putArchiveEntry(entry);
            tos.write(content);
            tos.closeArchiveEntry();
        }
    }

    private void createTarFileWithSubdirectory(Path tarFile, String entryName, byte[] content) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(tarFile.toFile());
                TarArchiveOutputStream tos = new TarArchiveOutputStream(fos)) {

            // Create a directory entry
            TarArchiveEntry dirEntry = new TarArchiveEntry("dir/");
            tos.putArchiveEntry(dirEntry);
            tos.closeArchiveEntry();

            // Create a file entry inside the directory
            TarArchiveEntry entry = new TarArchiveEntry(entryName);
            entry.setSize(content.length);
            tos.putArchiveEntry(entry);
            tos.write(content);
            tos.closeArchiveEntry();
        }
    }

    private void createTarFileWithSymbolicLink(Path tarFile, String entryName, String linkName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(tarFile.toFile());
                TarArchiveOutputStream tos = new TarArchiveOutputStream(fos)) {

            // Create a test file entry
            TarArchiveEntry entry = new TarArchiveEntry(entryName);
            entry.setSize("Hello, World!".getBytes().length);
            tos.putArchiveEntry(entry);
            tos.write("Hello, World!".getBytes());
            tos.closeArchiveEntry();

            // Create a symbolic link entry
            TarArchiveEntry linkEntry = new TarArchiveEntry(linkName, TarArchiveEntry.LF_SYMLINK);
            linkEntry.setLinkName(entryName);
            tos.putArchiveEntry(linkEntry);
            tos.closeArchiveEntry();
        }
    }
}
