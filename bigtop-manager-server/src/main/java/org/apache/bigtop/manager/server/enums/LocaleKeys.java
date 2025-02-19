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
public enum LocaleKeys {
    REQUEST_SUCCESS("request.success"),
    REQUEST_FAILED("request.failed"),
    PARAMETER_ERROR("request.parameter.incorrect"),

    NOTEMPTY("validation.not.empty"),
    NOTNULL("validation.not.null"),

    LOGIN_REQUIRED("login.required"),
    LOGIN_ACCOUNT_REQUIRED("login.account.required"),
    LOGIN_ACCOUNT_INCORRECT("login.account.incorrect"),
    LOGIN_ACCOUNT_DISABLED("login.account.disabled"),

    CLUSTER_NOT_FOUND("cluster.not.found"),
    CLUSTER_EXISTS("cluster.exists"),
    CLUSTER_HAS_HOSTS("cluster.has.hosts"),
    CLUSTER_HAS_SERVICES("cluster.has.services"),
    CLUSTER_HAS_NO_SERVICES("cluster.has.no.services"),

    HOST_NOT_FOUND("host.not.found"),
    HOST_ASSIGNED("host.assigned"),
    HOST_NOT_CONNECTED("host.not.connected"),
    HOST_UNABLE_TO_CONNECT("host.unable.to.connect"),
    HOST_UNABLE_TO_EXEC_COMMAND("host.unable.to.exec.command"),
    HOST_HAS_COMPONENTS("host.has.components"),

    STACK_NOT_FOUND("stack.not.found"),

    SERVICE_NOT_FOUND("service.not.found"),
    SERVICE_REQUIRED_NOT_FOUND("service.required.not.found"),
    SERVICE_HAS_COMPONENTS("service.has.components"),

    COMPONENT_NOT_FOUND("component.not.found"),
    COMPONENT_IS_RUNNING("component.is.running"),

    JOB_NOT_FOUND("job.not.found"),
    JOB_NOT_RETRYABLE("job.not.retryable"),

    CONFIG_NOT_FOUND("config.not.found"),

    COMMAND_NOT_FOUND("command.not.found"),
    COMMAND_NOT_SUPPORTED("command.not.supported"),

    PLATFORM_NOT_FOUND("platform.not.found"),
    PLATFORM_NOT_AUTHORIZED("platform.not.authorized"),
    PERMISSION_DENIED("permission.denied"),
    CREDIT_INCORRECT("credit.incorrect"),
    MODEL_NOT_SUPPORTED("model.not.supported"),
    CHAT_THREAD_NOT_FOUND("chat.thread.not.found"),
    NO_PLATFORM_IN_USE("no.platform.in.use"),
    PLATFORM_NOT_IN_USE("platform.not.in.use"),
    PLATFORM_IS_ACTIVE("platform.is.active"),

    FILE_UPLOAD_FAILED("file.upload.failed"),
    ;

    private final String key;

    LocaleKeys(String key) {
        this.key = key;
    }
}
