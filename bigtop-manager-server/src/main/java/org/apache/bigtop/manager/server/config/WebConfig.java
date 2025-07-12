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

import org.apache.bigtop.manager.server.interceptor.AuthInterceptor;
import org.apache.bigtop.manager.server.interceptor.MCPInterceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.Resource;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private AuthInterceptor authInterceptor;

    @Resource
    private MCPInterceptor mcpInterceptor;

    private static final String API_PREFIX = "/api";

    private static final String PREFIXED_PACKAGE = "org.apache.bigtop.manager.server.controller";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                // Server APIs
                .excludePathPatterns("/api/salt", "/api/nonce", "/api/login")
                // MCP APIs
                .excludePathPatterns("/mcp/**")
                // Frontend pages
                .excludePathPatterns("/", "/ui/**", "/favicon.ico", "/error")
                // Swagger pages
                .excludePathPatterns("/swagger-ui/**", "/v3/**", "/swagger-ui.html");

        registry.addInterceptor(mcpInterceptor).addPathPatterns("/mcp/**");
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(API_PREFIX, c -> c.getPackageName().equals(PREFIXED_PACKAGE));
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/ui/**").addResourceLocations("file:ui/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:/ui/");
        registry.addViewController("/ui/").setViewName("forward:/ui/index.html");
    }
}
