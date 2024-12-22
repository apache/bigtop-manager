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
package org.apache.bigtop.manager.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class JsonUtils {

    public static final ObjectMapper OBJECTMAPPER;
    public static final ObjectMapper INDENT_MAPPER;

    static {
        OBJECTMAPPER = new ObjectMapper();
        OBJECTMAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECTMAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        INDENT_MAPPER = OBJECTMAPPER.copy();
        INDENT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static <T> void writeToFile(String fileName, T obj) {
        writeToFile(new File(fileName), obj);
    }

    public static <T> void writeToFile(File file, T obj) {
        try {
            OBJECTMAPPER.writeValue(file, obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readFromFile(String fileName) {
        return readFromFile(new File(fileName), new TypeReference<>() {});
    }

    public static <T> T readFromFile(String fileName, TypeReference<T> typeReference) {
        return readFromFile(new File(fileName), typeReference);
    }

    public static <T> T readFromFile(File file) {
        try {
            return OBJECTMAPPER.readValue(file, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readFromFile(File file, TypeReference<T> typeReference) {
        try {
            return OBJECTMAPPER.readValue(file, typeReference);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readFromString(String json) {
        if (json == null) {
            return null;
        }

        try {
            return OBJECTMAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readFromString(String json, TypeReference<T> typeReference) {
        if (json == null) {
            return null;
        }

        try {
            return OBJECTMAPPER.readValue(json, typeReference);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readFromString(String json, Class<T> clazz) {
        if (json == null) {
            return null;
        }

        try {
            return OBJECTMAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonNode readTree(String filename) {
        try {
            return OBJECTMAPPER.readTree(new File(filename));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> String writeAsString(T obj) {
        if (obj == null) {
            return null;
        }

        try {
            return OBJECTMAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> String indentWriteAsString(T obj) {
        if (obj == null) {
            return null;
        }
        try {
            return INDENT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
