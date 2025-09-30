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
    PASSWORD_NOT_EMPTY(10004, LocaleKeys.PASSWORD_NOT_EMPTY),
    ORIGINAL_PASSWORD_SAME_AS_NEW_PASSWORD(10005, LocaleKeys.ORIGINAL_PASSWORD_SAME_AS_NEW_PASSWORD),
    TWO_PASSWORDS_NOT_MATCH(10006, LocaleKeys.TWO_PASSWORDS_NOT_MATCH),
    ORIGINAL_PASSWORD_INCORRECT(10007, LocaleKeys.ORIGINAL_PASSWORD_INCORRECT),
    OPERATION_SUCCESS(10008, LocaleKeys.OPERATION_SUCCESS),
    OPERATION_FAILED(10009, LocaleKeys.OPERATION_FAILED),

    // Cluster Exceptions -- 11000 ~ 11999
    CLUSTER_NOT_FOUND(11000, LocaleKeys.CLUSTER_NOT_FOUND),
    CLUSTER_EXISTS(11001, LocaleKeys.CLUSTER_EXISTS),
    CLUSTER_HAS_HOSTS(11002, LocaleKeys.CLUSTER_HAS_HOSTS),
    CLUSTER_HAS_SERVICES(11003, LocaleKeys.CLUSTER_HAS_SERVICES),
    CLUSTER_HAS_NO_SERVICES(11004, LocaleKeys.CLUSTER_HAS_NO_SERVICES),
    CLUSTER_HAS_COMPONENTS(11005, LocaleKeys.CLUSTER_HAS_COMPONENTS),
    CLUSTER_HAS_NO_COMPONENTS(11005, LocaleKeys.CLUSTER_HAS_NO_COMPONENTS),

    // Host Exceptions -- 12000 ~ 12999
    HOST_NOT_FOUND(12000, LocaleKeys.HOST_NOT_FOUND),
    HOST_ASSIGNED(12001, LocaleKeys.HOST_ASSIGNED),
    HOST_NOT_CONNECTED(12002, LocaleKeys.HOST_NOT_CONNECTED),
    HOST_UNABLE_TO_RESOLVE(12003, LocaleKeys.HOST_UNABLE_TO_RESOLVE),
    HOST_UNABLE_TO_CONNECT(12004, LocaleKeys.HOST_UNABLE_TO_CONNECT),
    HOST_UNABLE_TO_EXEC_COMMAND(12005, LocaleKeys.HOST_UNABLE_TO_EXEC_COMMAND),
    HOST_HAS_COMPONENTS(12006, LocaleKeys.HOST_HAS_COMPONENTS),
    AGENT_STILL_RUNNING(12007, LocaleKeys.HOST_AGENT_STILL_RUNNING),

    // Stack Exceptions -- 13000 ~ 13999
    STACK_NOT_FOUND(13000, LocaleKeys.STACK_NOT_FOUND),

    // Service Exceptions -- 14000 ~ 14999
    SERVICE_NOT_FOUND(14000, LocaleKeys.SERVICE_NOT_FOUND),
    SERVICE_REQUIRED_NOT_FOUND(14001, LocaleKeys.SERVICE_REQUIRED_NOT_FOUND),
    SERVICE_HAS_COMPONENTS(14002, LocaleKeys.SERVICE_HAS_COMPONENTS),
    SERVICE_IS_RUNNING(14003, LocaleKeys.SERVICE_IS_RUNNING),
    SERVICE_REQUIRED_BY(14004, LocaleKeys.SERVICE_REQUIRED_BY),

    // Component Exceptions -- 15000 ~ 15999
    COMPONENT_NOT_FOUND(15000, LocaleKeys.COMPONENT_NOT_FOUND),
    COMPONENT_IS_RUNNING(15001, LocaleKeys.COMPONENT_IS_RUNNING),
    COMPONENT_HAS_NO_SUCH_OP(15002, LocaleKeys.COMPONENT_HAS_NO_SUCH_OP),

    // Job Exceptions -- 16000 ~ 16999
    JOB_NOT_FOUND(16000, LocaleKeys.JOB_NOT_FOUND),
    JOB_NOT_RETRYABLE(16001, LocaleKeys.JOB_NOT_RETRYABLE),
    JOB_HAS_NO_STAGES(16002, LocaleKeys.JOB_HAS_NO_STAGES),
    STAGE_HAS_NO_TASKS(16003, LocaleKeys.STAGE_HAS_NO_TASKS),

    // Configuration Exceptions -- 17000 ~ 17999
    CONFIG_NOT_FOUND(17000, LocaleKeys.CONFIG_NOT_FOUND),

    // Command Exceptions -- 18000 ~ 18999
    COMMAND_NOT_FOUND(18000, LocaleKeys.COMMAND_NOT_FOUND),
    COMMAND_NOT_SUPPORTED(18001, LocaleKeys.COMMAND_NOT_SUPPORTED),

    // LLM Exceptions -- 19000 ~ 19999
    PLATFORM_NOT_FOUND(19000, LocaleKeys.PLATFORM_NOT_FOUND),
    PLATFORM_NOT_AUTHORIZED(19001, LocaleKeys.PLATFORM_NOT_AUTHORIZED),
    PERMISSION_DENIED(19002, LocaleKeys.PERMISSION_DENIED),
    CREDIT_INCORRECT(19003, LocaleKeys.CREDIT_INCORRECT),
    MODEL_NOT_SUPPORTED(19004, LocaleKeys.MODEL_NOT_SUPPORTED),
    CHAT_THREAD_NOT_FOUND(19005, LocaleKeys.CHAT_THREAD_NOT_FOUND),
    NO_PLATFORM_IN_USE(19006, LocaleKeys.NO_PLATFORM_IN_USE),
    PLATFORM_NOT_IN_USE(19007, LocaleKeys.PLATFORM_NOT_IN_USE),
    PLATFORM_IS_ACTIVE(19008, LocaleKeys.PLATFORM_IS_ACTIVE),

    // File Exceptions -- 30000 ~ 30999
    FILE_UPLOAD_FAILED(30000, LocaleKeys.FILE_UPLOAD_FAILED),
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
