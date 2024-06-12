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
package org.apache.bigtop.manager.server.exception;

import org.apache.bigtop.manager.server.enums.LocaleKeys;
import org.apache.bigtop.manager.server.enums.ResponseStatus;
import org.apache.bigtop.manager.server.utils.MessageSourceUtils;
import org.apache.bigtop.manager.server.utils.ResponseEntity;

import org.apache.commons.lang3.EnumUtils;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

import jakarta.validation.ConstraintViolationException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {ServerException.class, Exception.class})
    public ResponseEntity<Void> exceptionHandler(Exception e) {
        log.error("Internal Server Error: ", e);
        return ResponseEntity.error(ResponseStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = ApiException.class)
    public ResponseEntity<Void> exceptionHandler(ApiException e) {
        return ResponseEntity.error(e.getEx());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<Void> exceptionHandler(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        if (fieldError == null || fieldError.getCode() == null) {
            return ResponseEntity.error(ResponseStatus.PARAMETER_ERROR, e.getMessage());
        } else {
            String code = fieldError.getCode();
            String field = fieldError.getField();
            String message = fieldError.getDefaultMessage();
            if (EnumUtils.isValidEnum(LocaleKeys.class, code.toUpperCase())) {
                message = MessageSourceUtils.getMessage(LocaleKeys.valueOf(code.toUpperCase()), field);
            }

            return ResponseEntity.error(ResponseStatus.PARAMETER_ERROR, message);
        }
    }

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Void> exceptionHandler(ConstraintViolationException e) {
        String message = e.getMessage();
        log.error("Method parameter exception, message: {}", message, e);
        return ResponseEntity.error(ResponseStatus.PARAMETER_ERROR, message);
    }
}
