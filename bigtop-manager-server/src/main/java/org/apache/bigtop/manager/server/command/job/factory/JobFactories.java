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
package org.apache.bigtop.manager.server.command.job.factory;

import org.apache.bigtop.manager.server.command.CommandIdentifier;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class JobFactories {

    private static final AtomicBoolean LOADED = new AtomicBoolean(false);

    private static final Map<CommandIdentifier, String> JOB_FACTORIES = new HashMap<>();

    public static JobFactory getJobFactory(CommandIdentifier identifier) {
        if (!LOADED.get()) {
            load();
        }

        if (!JOB_FACTORIES.containsKey(identifier)) {
            throw new ApiException(
                    ApiExceptionEnum.COMMAND_NOT_SUPPORTED,
                    identifier.getCommand().name().toLowerCase(),
                    identifier.getCommandLevel().toLowerCase());
        }

        String beanName = JOB_FACTORIES.get(identifier);
        return SpringContextHolder.getApplicationContext().getBean(beanName, JobFactory.class);
    }

    private static synchronized void load() {
        if (LOADED.get()) {
            return;
        }

        for (Map.Entry<String, JobFactory> entry :
                SpringContextHolder.getJobFactories().entrySet()) {
            String beanName = entry.getKey();
            JobFactory jobFactory = entry.getValue();
            if (JOB_FACTORIES.containsKey(jobFactory.getCommandIdentifier())) {
                log.error("Duplicate JobFactory with identifier: {}", jobFactory.getCommandIdentifier());
                continue;
            }

            JOB_FACTORIES.put(jobFactory.getCommandIdentifier(), beanName);
            log.info(
                    "Load JobFactory: {} with identifier: {}",
                    jobFactory.getClass().getName(),
                    jobFactory.getCommandIdentifier());
        }

        LOADED.set(true);
    }
}
