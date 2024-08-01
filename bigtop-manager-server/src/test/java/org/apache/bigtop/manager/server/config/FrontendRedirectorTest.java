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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FrontendRedirectorTest {

    @Test
    void handleError_ReturnsForwardToIndexHtmlForNotFound() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(HttpStatus.NOT_FOUND.value());

        ModelAndView modelAndView = new FrontendRedirector().handleError(request);

        assertEquals(HttpStatus.OK, modelAndView.getStatus());
        assertEquals("forward:/ui/index.html", modelAndView.getViewName());
    }

    @Test
    void handleError_ReturnsModelAndViewWithStatusCode() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE))
                .thenReturn(HttpStatus.INTERNAL_SERVER_ERROR.value());

        ModelAndView modelAndView = new FrontendRedirector().handleError(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, modelAndView.getStatus());
        assertNull(modelAndView.getViewName());
    }

    @Test
    void handleError_ReturnsModelAndViewWithNullStatus() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)).thenReturn(null);

        ModelAndView modelAndView = new FrontendRedirector().handleError(request);

        assertNull(modelAndView.getStatus());
        assertNull(modelAndView.getViewName());
    }
}
