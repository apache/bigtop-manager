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
package org.apache.bigtop.manager.server.service.impl;

import org.apache.bigtop.manager.server.command.CommandIdentifier;
import org.apache.bigtop.manager.server.command.factory.JobFactories;
import org.apache.bigtop.manager.server.command.factory.JobFactory;
import org.apache.bigtop.manager.server.command.job.Job;
import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.command.scheduler.JobScheduler;
import org.apache.bigtop.manager.server.command.validator.ValidatorContext;
import org.apache.bigtop.manager.server.command.validator.ValidatorExecutionChain;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.model.vo.CommandVO;
import org.apache.bigtop.manager.server.service.CommandService;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.Resource;

@Slf4j
@Service
public class CommandServiceImpl implements CommandService {

    @Resource
    private JobScheduler jobScheduler;

    @Override
    public CommandVO command(CommandDTO commandDTO) {
        CommandIdentifier commandIdentifier =
                new CommandIdentifier(commandDTO.getCommandLevel(), commandDTO.getCommand());

        // Validate command params
        ValidatorContext validatorContext = new ValidatorContext();
        validatorContext.setCommandDTO(commandDTO);
        ValidatorExecutionChain.execute(validatorContext, commandIdentifier);

        // Create job
        JobContext jobContext = new JobContext();
        jobContext.setCommandDTO(commandDTO);
        JobFactory jobFactory = JobFactories.getJobFactory(commandIdentifier);
        Job job = jobFactory.createJob(jobContext);

        // Submit job
        jobScheduler.submit(job);

        return new CommandVO(job.getJobContext().getJobId(), job.getState().getName());
    }
}
