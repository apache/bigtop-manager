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
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

@Slf4j
public class TarballExtractor {

    public static void extractTarball(String source, String dest) {
        extractTarball(source, dest, 0);
    }

    public static void extractTarball(String source, String dest, int skipLevels) {
        Path sourcePath = Paths.get(source);
        Path destPath = Paths.get(dest);

        if (!Files.exists(sourcePath) || !Files.isRegularFile(sourcePath)) {
            log.error("Source file does not exist or is not a file: {}", source);
            throw new IllegalArgumentException("Source file does not exist or is not a file: " + source);
        }

        if (!Files.exists(destPath)) {
            try {
                Files.createDirectories(destPath);
            } catch (IOException e) {
                log.error("Failed to create destination directory: {}", dest, e);
                throw new StackException(e);
            }
        }

        Function<TarArchiveInputStream, Boolean> func = ais -> extract(ais, destPath, skipLevels);

        String sourceFileName = sourcePath.getFileName().toString();
        if (isTar(sourceFileName)) {
            extractTar(sourcePath, func);
        } else if (isTarGz(source)) {
            extractTarGz(sourcePath, func);
        } else if (isTarXz(source)) {
            extractTarXz(sourcePath, func);
        } else {
            log.error("Unsupported file type: {}", source);
        }
    }

    private static boolean extract(TarArchiveInputStream ais, Path destDir, int skipLevels) {
        try {
            TarArchiveEntry entry;
            while ((entry = ais.getNextEntry()) != null) {
                String entryName = entry.getName();
                Path entryPath = Paths.get(entryName);

                // Check if it is necessary to skip directories at the specified level
                if (entryPath.getNameCount() <= skipLevels) {
                    // Skip this entry
                    continue;
                }

                Path relativePath = entryPath.subpath(skipLevels, entryPath.getNameCount());
                Path outputPath = destDir.resolve(relativePath);

                if (entry.isDirectory()) {
                    createDirectories(outputPath);
                } else if (entry.isSymbolicLink()) {
                    createSymbolicLink(outputPath, entry.getLinkName());
                } else {
                    createFile(outputPath, ais);
                }
            }
        } catch (Exception e) {
            log.error("Error extracting archive", e);
            throw new StackException(e);
        }
        return true;
    }

    private static void createDirectories(Path path) {
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                log.error("Failed to create directory: {}", path, e);
                throw new StackException(e);
            }
        }
    }

    private static void createSymbolicLink(Path linkPath, String targetName) {
        Path targetPath = linkPath.getParent().resolve(targetName).normalize();
        if (!targetPath.isAbsolute()) {
            targetPath = linkPath.getParent().resolve(targetPath).normalize();
        }

        createDirectories(linkPath.getParent());

        try {
            Files.createSymbolicLink(linkPath, targetPath);
        } catch (IOException e) {
            log.error("Failed to create symbolic link from {} to {}", linkPath, targetPath, e);
            throw new StackException(e);
        }
    }

    private static void createFile(Path filePath, InputStream inputStream) {
        createDirectories(filePath.getParent());

        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            inputStream.transferTo(fos);
        } catch (IOException e) {
            log.error("Failed to create file: {}", filePath, e);
            throw new StackException(e);
        }
    }

    private static void extractTar(Path tarball, Function<TarArchiveInputStream, Boolean> func) {
        try (InputStream fis = Files.newInputStream(tarball);
                TarArchiveInputStream tis = new TarArchiveInputStream(fis)) {
            func.apply(tis);
        } catch (Exception e) {
            log.error("Error processing tar file", e);
            throw new StackException(e);
        }
    }

    private static void extractTarGz(Path tarball, Function<TarArchiveInputStream, Boolean> func) {
        try (InputStream fis = Files.newInputStream(tarball);
                GzipCompressorInputStream gis = new GzipCompressorInputStream(fis);
                TarArchiveInputStream tis = new TarArchiveInputStream(gis)) {
            func.apply(tis);
        } catch (Exception e) {
            log.error("Error processing tar.gz file", e);
            throw new StackException(e);
        }
    }

    private static void extractTarXz(Path tarball, Function<TarArchiveInputStream, Boolean> func) {
        try (InputStream fis = Files.newInputStream(tarball);
                XZCompressorInputStream xzis = new XZCompressorInputStream(fis);
                TarArchiveInputStream tis = new TarArchiveInputStream(xzis)) {
            func.apply(tis);
        } catch (Exception e) {
            log.error("Error processing tar.xz file", e);
            throw new StackException(e);
        }
    }

    private static boolean isTar(String filePath) {
        return filePath.endsWith(".tar");
    }

    private static boolean isTarGz(String filePath) {
        return filePath.endsWith(".tar.gz") || filePath.endsWith(".tgz");
    }

    private static boolean isTarXz(String filePath) {
        return filePath.endsWith(".tar.xz");
    }
}
