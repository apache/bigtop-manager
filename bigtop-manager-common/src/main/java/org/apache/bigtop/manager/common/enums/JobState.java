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
public enum JobState {
    PENDING("pending", "Pending"),

    PROCESSING("processing", "Processing"),

    SUCCESSFUL("successful", "Successful"),

    FAILED("failed", "Failed"),

    CANCELED("canceled", "Canceled"),
    ;

    private final String code;

    private final String name;

    @JsonCreator
    public static JobState fromString(String value) {
        return JobState.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toCamelCase() {
        return CaseUtils.toCamelCase(name());
    }
}
