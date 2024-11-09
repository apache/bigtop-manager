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

package org.apache.bigtop.manager.server.service;

import org.apache.bigtop.manager.dao.po.JobPO;
import org.apache.bigtop.manager.dao.po.StagePO;
import org.apache.bigtop.manager.dao.po.TaskPO;
import org.apache.bigtop.manager.dao.repository.JobDao;
import org.apache.bigtop.manager.dao.repository.StageDao;
import org.apache.bigtop.manager.dao.repository.TaskDao;
import org.apache.bigtop.manager.server.command.factory.JobFactories;
import org.apache.bigtop.manager.server.command.factory.JobFactory;
import org.apache.bigtop.manager.server.command.job.Job;
import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.command.scheduler.JobScheduler;
import org.apache.bigtop.manager.server.command.stage.Stage;
import org.apache.bigtop.manager.server.command.task.Task;
import org.apache.bigtop.manager.server.command.validator.ValidatorExecutionChain;
import org.apache.bigtop.manager.server.holder.SpringContextHolder;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;
import org.apache.bigtop.manager.server.service.impl.CommandServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommandServiceTest {
    @Mock
    JobFactory jobFactory;

    @Mock
    JobDao jobDao;

    @Mock
    private StageDao stageDao;

    @Mock
    private TaskDao taskDao;

    @Mock
    private JobScheduler jobScheduler;

    @InjectMocks
    private CommandService commandService = new CommandServiceImpl();

    @Test
    public void testCommand() {
        Job mockJob = mock(Job.class);
        JobContext mockJobContext = mock(JobContext.class);

        CommandDTO mockCommandDTO = mock(CommandDTO.class);

        when(mockJob.getJobContext()).thenReturn(mockJobContext);
        when(mockJobContext.getCommandDTO()).thenReturn(mockCommandDTO);
        when(mockJob.getJobPO()).thenReturn(new JobPO());

        Stage mockStage = mock(Stage.class);
        when(mockJob.getStages()).thenReturn(List.of(mockStage));
        when(mockStage.getStagePO()).thenReturn(new StagePO());

        Task mockTask = mock(Task.class);
        when(mockStage.getTasks()).thenReturn(List.of(mockTask));
        when(mockTask.getTaskPO()).thenReturn(new TaskPO());

        try (var mockSpringContextHolder = mockStatic(SpringContextHolder.class)) {
            mockSpringContextHolder
                    .when(SpringContextHolder::getCommandValidators)
                    .thenReturn(new HashMap<>());
            mockStatic(ValidatorExecutionChain.class);
            try (var mockJobFactories = mockStatic(JobFactories.class)) {
                mockJobFactories.when(() -> JobFactories.getJobFactory(any())).thenReturn(jobFactory);
                when(jobFactory.createJob(any())).thenReturn(mockJob);

                assert commandService.command(mockCommandDTO) != null;
            }
        }
    }
}
