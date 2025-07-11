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
package org.apache.bigtop.manager.server.mcp.prompt;

import org.apache.bigtop.manager.common.utils.FileUtils;
import org.apache.bigtop.manager.common.utils.ProjectPathUtils;
import org.apache.bigtop.manager.server.exception.ServerException;

import java.io.File;

/**
 * Static prompts for tools.
 */
public class Prompts {

    public static final String SAMPLE = getText("sample.prompt");

    private static String getText(String filename) {
        String promptPath = ProjectPathUtils.getPromptsPath();
        String filePath = promptPath + File.separator + filename;

        try {
            return FileUtils.readFile2Str(new File(filePath));
        } catch (Exception e) {
            throw new ServerException("Error reading prompt file: " + filePath, e);
        }
    }
}
