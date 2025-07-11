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
package org.apache.bigtop.manager.server.mcp.converter;

import org.apache.bigtop.manager.common.utils.JsonUtils;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.execution.ToolCallResultConverter;
import org.springframework.lang.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

/**
 * Custom converter only replace JsonParser to JsonUtils.
 * See original source code from {@link org.springframework.ai.tool.execution.DefaultToolCallResultConverter}
 */
public class JsonToolCallResultConverter implements ToolCallResultConverter {

    private static final Logger logger = LoggerFactory.getLogger(JsonToolCallResultConverter.class);

    @NotNull @Override
    public String convert(@Nullable Object result, @Nullable Type returnType) {
        if (returnType == Void.TYPE) {
            logger.debug("The tool has no return type. Converting to conventional response.");
            return JsonUtils.writeAsString("Done");
        }
        if (result instanceof RenderedImage) {
            final var buf = new ByteArrayOutputStream(1024 * 4);
            try {
                ImageIO.write((RenderedImage) result, "PNG", buf);
            } catch (IOException e) {
                return "Failed to convert tool result to a base64 image: " + e.getMessage();
            }
            final var imgB64 = Base64.getEncoder().encodeToString(buf.toByteArray());
            return JsonUtils.writeAsString(Map.of("mimeType", "image/png", "data", imgB64));
        } else {
            logger.debug("Converting tool result to JSON.");
            return Objects.requireNonNull(JsonUtils.writeAsString(result));
        }
    }
}
