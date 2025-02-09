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
package org.apache.bigtop.manager.server.config;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValidatorConfigTest {

    @Test
    public void testFailFast() {
        // Manually create a Validator instance
        ValidatorConfig validatorConfig = new ValidatorConfig();
        Validator validator = validatorConfig.validator();

        // Create an invalid object with multiple validation errors
        InvalidObject invalidObject = new InvalidObject("", "123");

        // Validate the object
        Set<ConstraintViolation<InvalidObject>> violations = validator.validate(invalidObject);

        // Since failFast is enabled, there should be only one error
        assertEquals(1, violations.size());
    }

    // Define a class with multiple validation constraints
    private static class InvalidObject {

        @NotBlank(message = "must not be blank")
        private String field1;

        @Size(min = 5, message = "length must be at least 5")
        private String field2;

        public InvalidObject(String field1, String field2) {
            this.field1 = field1;
            this.field2 = field2;
        }
    }
}
