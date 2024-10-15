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

import lombok.Getter;

@Getter
public enum AuthPlatformStatus {
    DELETED(-1),
    NORMAL(0),
    ACTIVE(1),
    INACTIVE(2),
    ;

    private final Integer code;

    AuthPlatformStatus(Integer code) {
        this.code = code;
    }

    public static AuthPlatformStatus fromCode(Integer code) {
        for (AuthPlatformStatus status : AuthPlatformStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return NORMAL;
    }

    public static boolean isNormal(int code) {
        return NORMAL.code.equals(code);
    }

    public static boolean isActive(int code) {
        return ACTIVE.code.equals(code);
    }

    public static boolean isAvailable(int code) {
        return isNormal(code) || isActive(code);
    }

    public static boolean isDeleted(int code) {
        return DELETED.code.equals(code);
    }

    public static boolean needSwitch(int oldStatus, int newStatus) {
        return isNormal(oldStatus) && isActive(newStatus);
    }
}
