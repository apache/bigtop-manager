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

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
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

    public static void extractTarball(String source, String dest, Integer skipLevels) {
        File tarball = new File(source);
        File destDir = new File(dest);

        if (isTarGz(source)) {
            extractTarGz(tarball, tis -> extract(tis, destDir, skipLevels));
        } else if (isTar(source)) {
            extractTar(tarball, tis -> extract(tis, destDir, skipLevels));
        } else {
            log.info("Unsupported file type: {}", source);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static Boolean extract(TarArchiveInputStream tis, File destDir, Integer skipLevels) {
        try {
            TarArchiveEntry entry;
            while ((entry = tis.getNextEntry()) != null) {
                String entryName = entry.getName();
                String[] parts = entryName.split("/");
                if (parts.length <= skipLevels) {
                    continue; // Skip this entry
                }
                StringBuilder newName = new StringBuilder();
                for (int i = skipLevels; i < parts.length; i++) {
                    newName.append(parts[i]);
                    if (i < parts.length - 1) {
                        newName.append(File.separator);
                    }
                }
                File outputFile = new File(destDir, newName.toString());

                if (entry.isDirectory()) {
                    if (!outputFile.exists()) {
                        outputFile.mkdirs();
                    }
                } else if (entry.isSymbolicLink()) {
                    Path link = Paths.get(outputFile.getAbsolutePath());
                    Path target = Paths.get(entry.getLinkName());
                    Files.createSymbolicLink(link, target);
                } else {
                    File parent = outputFile.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }

                    try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = tis.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.info("Error extracting tarball", e);
            throw new StackException(e);
        }

        return true;
    }

    private static void extractTar(File tarball, Function<TarArchiveInputStream, Boolean> func) {
        try (InputStream fis = Files.newInputStream(tarball.toPath());
                TarArchiveInputStream tis = new TarArchiveInputStream(fis)) {
            func.apply(tis);
        } catch (Exception e) {
            log.error("Error extracting tarball", e);
            throw new StackException(e);
        }
    }

    private static void extractTarGz(File tarball, Function<TarArchiveInputStream, Boolean> func) {
        try (InputStream fis = Files.newInputStream(tarball.toPath());
                GzipCompressorInputStream gis = new GzipCompressorInputStream(fis);
                TarArchiveInputStream tis = new TarArchiveInputStream(gis)) {
            func.apply(tis);
        } catch (Exception e) {
            log.error("Error extracting tarball", e);
            throw new StackException(e);
        }
    }

    private static boolean isTarGz(String filePath) {
        return filePath.endsWith(".tar.gz");
    }

    private static boolean isTar(String filePath) {
        return filePath.endsWith(".tar");
    }
}
