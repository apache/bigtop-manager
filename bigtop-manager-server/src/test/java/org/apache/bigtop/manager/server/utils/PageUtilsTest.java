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

import org.apache.bigtop.manager.server.model.query.PageQuery;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class PageUtilsTest {

    private MockedStatic<ServletUtils> servletUtilsMock;

    @BeforeEach
    void setUp() {
        servletUtilsMock = mockStatic(ServletUtils.class);
    }

    @AfterEach
    void tearDown() {
        servletUtilsMock.close();
    }

    @Test
    void testDefaultValues() {
        // Simulate all parameters not existing
        setupMockParameters(null, null, null, null);

        PageQuery result = PageUtils.getPageQuery();

        assertAll(
                "Default values check",
                () -> assertEquals(1, result.getPageNum()),
                () -> assertEquals(10, result.getPageSize()),
                () -> assertEquals("id ASC ", result.getOrderBy()));
    }

    @Test
    void testValidParameters() {
        // Simulate valid parameters
        setupMockParameters("3", "25", "createTime", "desc");

        PageQuery result = PageUtils.getPageQuery();

        assertAll(
                "Valid parameters check",
                () -> assertEquals(3, result.getPageNum()),
                () -> assertEquals(25, result.getPageSize()),
                () -> assertEquals("createTime DESC ", result.getOrderBy()));
    }

    @Test
    void testInvalidNumberParameters() {
        // Simulate invalid number parameters
        setupMockParameters("abc", "xyz", "name", "asc");

        PageQuery result = PageUtils.getPageQuery();

        assertAll(
                "Invalid number parameters check",
                () -> assertEquals(1, result.getPageNum()),
                () -> assertEquals(10, result.getPageSize()),
                () -> assertEquals("name ASC ", result.getOrderBy()));
    }

    @Test
    void testEmptyStringParameters() {
        // Simulate empty string parameters
        setupMockParameters("", "", "", "");

        PageQuery result = PageUtils.getPageQuery();

        assertAll(
                "Empty string parameters check",
                () -> assertEquals(1, result.getPageNum()),
                () -> assertEquals(10, result.getPageSize()),
                () -> assertEquals("id ASC ", result.getOrderBy()));
    }

    @Test
    void testEdgeCaseNumbers() {
        // Test edge case numbers
        setupMockParameters("0", "0", "updateTime", "invalid");

        PageQuery result = PageUtils.getPageQuery();

        assertAll(
                "Edge case numbers check",
                () -> assertEquals(0, result.getPageNum()),
                () -> assertEquals(0, result.getPageSize()),
                () -> assertEquals("updateTime ASC ", result.getOrderBy()));
    }

    @Test
    void testSpecialCharactersInOrderBy() {
        // Test special characters handling
        setupMockParameters("2", "15", "user_name", "desc");

        PageQuery result = PageUtils.getPageQuery();

        assertAll("Special characters check", () -> assertEquals("user_name DESC ", result.getOrderBy()));
    }

    @Test
    void testMixedCaseSortParameter() {
        // Test mixed case sort parameter
        setupMockParameters("1", "10", "email", "desc");

        PageQuery result = PageUtils.getPageQuery();

        assertEquals("email DESC ", result.getOrderBy());
    }

    private void setupMockParameters(String pageNum, String pageSize, String orderBy, String sort) {
        // Parameter mock configuration method
        servletUtilsMock.when(() -> ServletUtils.getParameter(eq("pageNum"))).thenReturn(pageNum);
        servletUtilsMock.when(() -> ServletUtils.getParameter(eq("pageSize"))).thenReturn(pageSize);
        servletUtilsMock.when(() -> ServletUtils.getParameter(eq("orderBy"))).thenReturn(orderBy);
        servletUtilsMock.when(() -> ServletUtils.getParameter(eq("sort"))).thenReturn(sort);
    }
}
