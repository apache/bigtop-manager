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
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocaleConfigTest {

    private final AnnotationConfigApplicationContext context =
            new AnnotationConfigApplicationContext(LocaleConfig.class);

    @Test
    void localeResolver_ReturnsAcceptHeaderLocaleResolver() {
        LocaleConfig localeConfig = context.getBean(LocaleConfig.class);
        LocaleResolver localeResolver = localeConfig.localeResolver();
        assertInstanceOf(AcceptHeaderLocaleResolver.class, localeResolver);
    }

    @Test
    void resolveLocale_ValidLocaleHeader() {
        LocaleConfig localeConfig = context.getBean(LocaleConfig.class);
        AcceptHeaderLocaleResolver localeResolver = (AcceptHeaderLocaleResolver) localeConfig.localeResolver();
        localeResolver.setSupportedLocales(List.of(Locale.CHINESE, Locale.ENGLISH));

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getLocales()).thenReturn(Collections.enumeration(List.of(Locale.CHINESE, Locale.ENGLISH)));
        Locale resolvedLocale;

        when(request.getHeader("Accept-Language")).thenReturn("zh");
        resolvedLocale = localeResolver.resolveLocale(request);
        assertEquals(Locale.CHINESE, resolvedLocale);

        when(request.getHeader("Accept-Language")).thenReturn("en");
        resolvedLocale = localeResolver.resolveLocale(request);
        assertEquals(Locale.ENGLISH, resolvedLocale);
    }

    @Test
    void resolveLocale_NoLocaleHeader() {
        LocaleConfig localeConfig = context.getBean(LocaleConfig.class);
        AcceptHeaderLocaleResolver localeResolver = (AcceptHeaderLocaleResolver) localeConfig.localeResolver();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Accept-Language")).thenReturn(null);

        Locale resolvedLocale = localeResolver.resolveLocale(request);
        assertEquals(Locale.US, resolvedLocale); // Assuming default is US
    }
}
