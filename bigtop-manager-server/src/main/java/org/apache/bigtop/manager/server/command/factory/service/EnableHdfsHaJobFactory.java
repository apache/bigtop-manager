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
package org.apache.bigtop.manager.server.command.factory.service;

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.server.command.CommandIdentifier;
import org.apache.bigtop.manager.server.command.job.Job;
import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.command.job.service.EnableHdfsHaJob;
import org.apache.bigtop.manager.server.enums.CommandLevel;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class EnableHdfsHaJobFactory extends AbstractServiceJobFactory {

    @Override
    public CommandIdentifier getCommandIdentifier() {
        return new CommandIdentifier(CommandLevel.SERVICE, Command.ENABLE_HDFS_HA);
    }

    @Override
    public Job createJob(JobContext jobContext) {
        log.info("EnableHdfsHaJobFactory creating job, jobFactoryClassSource={}, jobClassSource={}, stageClassSource={}, taskClassSource={}, commandDTO.command={}, commandDTO.customCommandLen={}",
                getCodeSource(EnableHdfsHaJobFactory.class),
                getCodeSource(EnableHdfsHaJob.class),
                getCodeSource(org.apache.bigtop.manager.server.command.stage.ComponentCustomStage.class),
                getCodeSource(org.apache.bigtop.manager.server.command.task.ComponentCustomTask.class),
                jobContext == null || jobContext.getCommandDTO() == null ? null : jobContext.getCommandDTO().getCommand(),
                jobContext == null || jobContext.getCommandDTO() == null || jobContext.getCommandDTO().getCustomCommand() == null
                        ? null
                        : jobContext.getCommandDTO().getCustomCommand().length());
        return new EnableHdfsHaJob(jobContext);
    }

    private static String getCodeSource(Class<?> clazz) {
        try {
            if (clazz == null || clazz.getProtectionDomain() == null || clazz.getProtectionDomain().getCodeSource() == null) {
                return "null";
            }
            return String.valueOf(clazz.getProtectionDomain().getCodeSource().getLocation());
        } catch (Exception e) {
            return "error:" + e.getMessage();
        }
    }
}

