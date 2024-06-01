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
package org.apache.bigtop.manager.server.command.job.runner;

import org.apache.bigtop.manager.common.utils.JsonUtils;
import org.apache.bigtop.manager.dao.entity.Job;
import org.apache.bigtop.manager.server.command.CommandIdentifier;
import org.apache.bigtop.manager.server.command.job.factory.JobContext;
import org.apache.bigtop.manager.server.enums.ApiExceptionEnum;
import org.apache.bigtop.manager.server.exception.ApiException;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JobRunners {

    private static final AtomicBoolean LOADED = new AtomicBoolean(false);

    private static final Map<CommandIdentifier, String> JOB_RUNNERS = new HashMap<>();

    public static JobRunner getJobRunner(Job job) {
        if (!LOADED.get()) {
            load();
        }

        JobContext jobContext = JsonUtils.readFromString(job.getContext(), JobContext.class);
        CommandDTO commandDTO = jobContext.getCommandDTO();
        CommandIdentifier identifier = new CommandIdentifier(commandDTO.getCommandLevel(), commandDTO.getCommand());
        if (!JOB_RUNNERS.containsKey(identifier)) {
            throw new ApiException(ApiExceptionEnum.COMMAND_NOT_SUPPORTED, identifier.getCommand().name().toLowerCase(),
                    identifier.getCommandLevel().toLowerCase());
        }

        String beanName = JOB_RUNNERS.get(identifier);
        JobRunner runner = SpringContextHolder.getApplicationContext().getBean(beanName, JobRunner.class);
        runner.setJob(job);
        runner.setJobContext(jobContext);

        return runner;
    }

    private static synchronized void load() {
        if (LOADED.get()) {
            return;
        }

        for (Map.Entry<String, JobRunner> entry : SpringContextHolder.getJobRunners().entrySet()) {
            String beanName = entry.getKey();
            JobRunner jobRunner = entry.getValue();
            if (JOB_RUNNERS.containsKey(jobRunner.getCommandIdentifier())) {
                log.error("Duplicate JobRunner with identifier: {}", jobRunner.getCommandIdentifier());
                continue;
            }

            JOB_RUNNERS.put(jobRunner.getCommandIdentifier(), beanName);
            log.info("Load JobRunner: {} with identifier: {}", jobRunner.getClass().getName(),
                    jobRunner.getCommandIdentifier());
        }

        LOADED.set(true);
    }
}
