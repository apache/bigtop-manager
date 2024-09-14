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
import lombok.Setter;

@Getter
public enum ApiExceptionEnum {
    NEED_LOGIN(10000, LocaleKeys.LOGIN_REQUIRED),
    USERNAME_OR_PASSWORD_REQUIRED(10001, LocaleKeys.LOGIN_ACCOUNT_REQUIRED),
    INCORRECT_USERNAME_OR_PASSWORD(10002, LocaleKeys.LOGIN_ACCOUNT_INCORRECT),
    USER_IS_DISABLED(10003, LocaleKeys.LOGIN_ACCOUNT_DISABLED),

    // Cluster Exceptions -- 11000 ~ 11999
    CLUSTER_NOT_FOUND(11000, LocaleKeys.CLUSTER_NOT_FOUND),
    CLUSTER_IS_INSTALLED(11001, LocaleKeys.CLUSTER_IS_INSTALLED),

    // Host Exceptions -- 12000 ~ 12999
    HOST_NOT_FOUND(12000, LocaleKeys.HOST_NOT_FOUND),
    HOST_ASSIGNED(12001, LocaleKeys.HOST_ASSIGNED),
    HOST_NOT_CONNECTED(12002, LocaleKeys.HOST_NOT_CONNECTED),
    HOST_UNABLE_TO_CONNECT(12003, LocaleKeys.HOST_UNABLE_TO_CONNECT),

    // Stack Exceptions -- 13000 ~ 13999
    STACK_NOT_FOUND(13000, LocaleKeys.STACK_NOT_FOUND),

    // Service Exceptions -- 14000 ~ 14999
    SERVICE_NOT_FOUND(14000, LocaleKeys.SERVICE_NOT_FOUND),
    SERVICE_REQUIRED_NOT_FOUND(14001, LocaleKeys.SERVICE_REQUIRED_NOT_FOUND),

    // Component Exceptions -- 15000 ~ 15999
    COMPONENT_NOT_FOUND(15000, LocaleKeys.COMPONENT_NOT_FOUND),

    // Job Exceptions -- 16000 ~ 16999
    JOB_NOT_FOUND(16000, LocaleKeys.JOB_NOT_FOUND),
    JOB_NOT_RETRYABLE(16001, LocaleKeys.JOB_NOT_RETRYABLE),

    // Configuration Exceptions -- 17000 ~ 17999
    CONFIG_NOT_FOUND(17000, LocaleKeys.CONFIG_NOT_FOUND),

    // Command Exceptions -- 18000 ~ 18999
    COMMAND_NOT_FOUND(18000, LocaleKeys.COMMAND_NOT_FOUND),
    COMMAND_NOT_SUPPORTED(18000, LocaleKeys.COMMAND_NOT_SUPPORTED),

    // Chatbot Exceptions -- 19000 ~ 19999
    PLATFORM_NOT_FOUND(19000, LocaleKeys.PLATFORM_NOT_FOUND),
    PLATFORM_NOT_AUTHORIZED(19001, LocaleKeys.PLATFORM_NOT_AUTHORIZED),
    PERMISSION_DENIED(19002, LocaleKeys.PERMISSION_DENIED),
    CREDIT_INCORRECT(19003, LocaleKeys.CREDIT_INCORRECT),
    MODEL_NOT_SUPPORTED(19004, LocaleKeys.MODEL_NOT_SUPPORTED),
    CHAT_THREAD_NOT_FOUND(19005, LocaleKeys.CHAT_THREAD_NOT_FOUND),
    ;

    private final Integer code;

    private final LocaleKeys key;

    @Setter
    private String[] args;

    ApiExceptionEnum(Integer code, LocaleKeys key) {
        this.code = code;
        this.key = key;
    }

    public String getMessage() {
        if (args == null || args.length == 0) {
            return MessageSourceUtils.getMessage(key);
        } else {
            return MessageSourceUtils.getMessage(key, args);
        }
    }
}
