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
package org.apache.bigtop.manager.common.enums;

import org.apache.bigtop.manager.common.utils.CaseUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Command {
    // Available for: Cluster, Host, Service, Component
    // Remove is not a command because it won't create job, please refer to the related controller for remove action.
    ADD("add", "Add"),
    START("start", "Start"),
    STOP("stop", "Stop"),
    RESTART("restart", "Restart"),

    // Available for: Service, Component
    CHECK("check", "Check"),

    // Available for: Service
    CONFIGURE("configure", "Configure"),
    CUSTOM("custom", "Custom"),

    // Internal use only, not available for job creation
    STATUS("status", "Status"),
    ;

    private final String code;

    private final String name;

    @JsonCreator
    public static Command fromString(String value) {
        return Command.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toCamelCase() {
        return CaseUtils.toCamelCase(name());
    }
}
