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
package org.apache.bigtop.manager.server.utils;

import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.enums.ResponseStatus;

import lombok.Data;

@Data
public class ResponseEntity<T> {

    private Integer code;

    private String message;

    private T data;

    public ResponseEntity() {}

    public ResponseEntity(ResponseStatus status) {
        this.code = status.getCode();
        this.message = status.getMessage();
    }

    public ResponseEntity(ResponseStatus status, T data) {
        this.code = status.getCode();
        this.message = status.getMessage();
        this.data = data;
    }

    public ResponseEntity(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResponseEntity(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ResponseEntity<T> success(T data) {
        return new ResponseEntity<>(ResponseStatus.SUCCESS, data);
    }

    public static <T> ResponseEntity<T> success() {
        return new ResponseEntity<>(ResponseStatus.SUCCESS);
    }

    public static <T> ResponseEntity<T> error(ResponseStatus status) {
        return new ResponseEntity<>(status);
    }

    public static <T> ResponseEntity<T> error(ResponseStatus status, String appendMessage) {
        return new ResponseEntity<>(status.getCode(), status.getMessage() + ": " + appendMessage);
    }

    public static <T> ResponseEntity<T> error(ApiExceptionEnum ex) {
        return new ResponseEntity<>(ex.getCode(), ex.getMessage());
    }
}
