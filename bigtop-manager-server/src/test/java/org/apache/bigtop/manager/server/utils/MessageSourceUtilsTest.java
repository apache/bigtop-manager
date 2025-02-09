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

import org.apache.bigtop.manager.server.enums.LocaleKeys;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MessageSourceUtilsTest {

    private MessageSource messageSourceMock;

    @BeforeEach
    void setUp() {
        messageSourceMock = mock(MessageSource.class);
        MessageSourceUtils utils = new MessageSourceUtils();
        utils.setMessageSource(messageSourceMock);

        LocaleContextHolder.setLocale(Locale.US);
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void testGetMessageWithArguments() {
        LocaleKeys testKey = LocaleKeys.REQUEST_SUCCESS;
        Object[] args = {"operation"};
        String expectedMessage = "Operation succeeded";

        when(messageSourceMock.getMessage(eq(testKey.getKey()), aryEq(args), eq(Locale.US)))
                .thenReturn(expectedMessage);

        String actualMessage = MessageSourceUtils.getMessage(testKey, "operation");
        assertEquals(expectedMessage, actualMessage);
    }
}
