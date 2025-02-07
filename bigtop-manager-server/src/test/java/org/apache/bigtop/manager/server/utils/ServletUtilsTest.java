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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServletUtilsTest {

    // Use mockito to mock HttpServletRequest object
    private HttpServletRequest mockRequest;

    @BeforeEach
    public void setUp() {
        ServletRequestAttributes mockAttributes = mock(ServletRequestAttributes.class);
        mockRequest = mock(HttpServletRequest.class);
        when(mockAttributes.getRequest()).thenReturn(mockRequest);
        RequestContextHolder.setRequestAttributes(mockAttributes);
    }

    @Test
    public void testGetParameterNormalPath() {
        // Test case: normal path, get existing parameter
        when(mockRequest.getParameter("testParam")).thenReturn("testValue");
        assertEquals("testValue", ServletUtils.getParameter("testParam"));
    }

    @Test
    public void testGetParameterParameterDoesNotExist() {
        // Test case: parameter does not exist, expect to return null
        when(mockRequest.getParameter("nonexistentParam")).thenReturn(null);
        assertNull(ServletUtils.getParameter("nonexistentParam"));
    }

    @Test
    public void testGetParameterEmptyStringParameterName() {
        // Test case: parameter name is an empty string, expect to return null
        when(mockRequest.getParameter("")).thenReturn(null);
        assertNull(ServletUtils.getParameter(""));
    }

    @Test
    public void testGetParameterSpaceParameterName() {
        // Test case: parameter name is a space, expect to return null
        when(mockRequest.getParameter(" ")).thenReturn(null);
        assertNull(ServletUtils.getParameter(" "));
    }

    @Test
    public void testGetParameterSpecialCharacterParameterName() {
        // Test case: parameter name contains special characters, expect to return null or correct value (adjust based
        // on actual situation)
        when(mockRequest.getParameter("!@#$%^&*()")).thenReturn(null);
        assertNull(ServletUtils.getParameter("!@#$%^&*()"));
    }

    @Test
    public void testGetRequestCorrectRequest() {
        // Test case: correct request, expect to return the mock HttpServletRequest object
        assertEquals(mockRequest, ServletUtils.getRequest());
    }

    @Test
    public void testGetRequestAttributesCorrectAttributes() {
        // Test case: correct attributes, expect to return the mock ServletRequestAttributes object
        ServletRequestAttributes attributes = ServletUtils.getRequestAttributes();
        assertNotNull(attributes);
        assertEquals(mockRequest, attributes.getRequest());
    }

    @Test
    public void testGetRequestAttributesAttributesNotSet() {
        // Test case: attributes not set, expect to return null
        RequestContextHolder.setRequestAttributes(null);
        assertNull(ServletUtils.getRequestAttributes());
    }

    @Test
    public void testGetParameterAttributesNotSet() {
        // Test case: attributes not set, expect to throw an exception or return null
        RequestContextHolder.setRequestAttributes(null);
        assertThrows(NullPointerException.class, () -> ServletUtils.getParameter("testParam"));
    }

    @Test
    public void testGetParameterRequestNotSet() {
        // Test case: request not set, expect to throw an exception or return null
        when(mockRequest.getParameter("testParam")).thenReturn(null);
        ServletRequestAttributes mockAttributes = mock(ServletRequestAttributes.class);
        when(mockAttributes.getRequest()).thenReturn(null);
        RequestContextHolder.setRequestAttributes(mockAttributes);
        assertThrows(NullPointerException.class, () -> ServletUtils.getParameter("testParam"));
    }
}
