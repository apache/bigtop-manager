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
    CLUSTER_IS_INSTALLED("cluster.is.installed"),

    HOST_NOT_FOUND("host.not.found"),
    HOST_ASSIGNED("host.assigned"),
    HOST_NOT_CONNECTED("host.not.connected"),

    STACK_NOT_FOUND("stack.not.found"),

    SERVICE_NOT_FOUND("service.not.found"),
    SERVICE_REQUIRED_NOT_FOUND("service.required.not.found"),

    COMPONENT_NOT_FOUND("component.not.found"),

    JOB_NOT_FOUND("job.not.found"),

    CONFIG_NOT_FOUND("config.not.found"),

    COMMAND_NOT_FOUND("command.not.found"),
    COMMAND_NOT_SUPPORTED("command.not.supported"),
    ;

    private final String key;

    LocaleKeys(String key) {
        this.key = key;
    }
}
