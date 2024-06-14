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
package org.apache.bigtop.manager.common.config;

import org.apache.bigtop.manager.common.message.serializer.MessageDeserializer;
import org.apache.bigtop.manager.common.message.serializer.MessageSerializer;

import org.apache.commons.lang3.StringUtils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.Resource;

@Configuration
public class MessageConfig {

    @Resource
    private ApplicationConfig applicationConfig;

    @Bean
    public MessageSerializer messageSerializer() throws Exception {
        String serializerType = applicationConfig.getSerializer().getType();
        String packageName = "org.apache.bigtop.manager.common.message.serializer";
        String className = packageName + "." + StringUtils.capitalize(serializerType) + "MessageSerializer";
        return (MessageSerializer)
                Class.forName(className).getDeclaredConstructor().newInstance();
    }

    @Bean
    public MessageDeserializer messageDeserializer() throws Exception {
        String deserializerType = applicationConfig.getSerializer().getType();
        String packageName = "org.apache.bigtop.manager.common.message.serializer";
        String className = packageName + "." + StringUtils.capitalize(deserializerType) + "MessageDeserializer";
        return (MessageDeserializer)
                Class.forName(className).getDeclaredConstructor().newInstance();
    }
}
