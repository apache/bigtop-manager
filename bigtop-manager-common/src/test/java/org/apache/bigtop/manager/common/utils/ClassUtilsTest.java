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
package org.apache.bigtop.manager.common.utils;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClassUtilsTest {

    // Test case for a normal class
    @Test
    public void testGetFieldsNormalClass() {
        List<Field> fields = ClassUtils.getFields(TestClass.class);
        assertEquals(3, fields.size());
    }

    // Test case for a class with no fields
    @Test
    public void testGetFieldsNoFieldsClass() {
        List<Field> fields = ClassUtils.getFields(NoFieldsClass.class);
        assertEquals(0, fields.size());
    }

    // Test case for an inherited class
    @Test
    public void testGetFieldsInheritedClass() {
        List<Field> fields = ClassUtils.getFields(InheritedClass.class);
        assertEquals(5, fields.size());
    }

    // Test case for an interface
    @Test
    public void testGetFieldsInterface() {
        List<Field> fields = ClassUtils.getFields(TestInterface.class);
        assertEquals(0, fields.size());
    }

    // Test case for null input
    @Test
    public void testGetFieldsNullInput() {
        List<Field> fields = ClassUtils.getFields(null);
        assertEquals(0, fields.size());
    }

    // Test case for a primitive type
    @Test
    public void testGetFieldsPrimitiveType() {
        List<Field> fields = ClassUtils.getFields(int.class);
        assertEquals(0, fields.size()); // int is a primitive type, which has no fields
    }

    // Test case for a wrapper type
    @Test
    public void testGetFieldsWrapperType() {
        List<Field> fields = ClassUtils.getFields(Integer.class);
        assertTrue(fields.size() > 0);
    }

    // Helper test class
    private static class TestClass {
        private int field1;
        private String field2;
        private double field3;
    }

    // Helper test class with no fields
    private static class NoFieldsClass {}

    // Helper test class that inherits from TestClass
    private static class InheritedClass extends TestClass {
        private boolean field4;
        private char field5;
    }

    // Helper test interface
    private static interface TestInterface {}
}
