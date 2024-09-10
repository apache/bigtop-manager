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
package org.apache.bigtop.manager.ai.core.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public enum PlatformType {
    OPENAI("openai"),
    DASH_SCOPE("dashscope");

    private final String value;

    PlatformType(String value) {
        this.value = value;
    }

    public static List<String> getPlatforms() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    public static PlatformType getPlatformType(String value) {
        if (Objects.isNull(value) || value.isEmpty()) {
            return null;
        }
        for (PlatformType platformType : PlatformType.values()) {
            if (platformType.value.equals(value)) {
                return platformType;
            }
        }
        return null;
    }
}
