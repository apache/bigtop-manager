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
package org.apache.bigtop.manager.server.enums;

import org.apache.bigtop.manager.server.utils.MessageSourceUtils;

import lombok.Getter;

@Getter
public enum ResponseStatus {
    SUCCESS(0, LocaleKeys.REQUEST_SUCCESS),

    INTERNAL_SERVER_ERROR(-1, LocaleKeys.REQUEST_FAILED),

    PARAMETER_ERROR(1, LocaleKeys.PARAMETER_ERROR),
    ;

    private final Integer code;

    private final LocaleKeys key;

    ResponseStatus(Integer code, LocaleKeys key) {
        this.code = code;
        this.key = key;
    }

    public String getMessage() {
        return MessageSourceUtils.getMessage(key);
    }
}
