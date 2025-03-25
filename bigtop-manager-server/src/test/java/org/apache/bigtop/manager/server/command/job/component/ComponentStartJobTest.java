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
package org.apache.bigtop.manager.server.command.job.component;

import org.apache.bigtop.manager.common.enums.Command;
import org.apache.bigtop.manager.server.command.helper.ComponentStageHelper;
import org.apache.bigtop.manager.server.command.job.JobContext;
import org.apache.bigtop.manager.server.command.stage.ComponentStartStage;
import org.apache.bigtop.manager.server.command.stage.Stage;
import org.apache.bigtop.manager.server.command.stage.StageContext;
import org.apache.bigtop.manager.server.model.dto.CommandDTO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ComponentStartJobTest {

    @Mock
    private ComponentStartJob componentStartJob;

    @Test
    public void testCreateStages() {
        CommandDTO commandDTO = new CommandDTO();
        commandDTO.setCommand(Command.START);
        JobContext jobContext = new JobContext();
        jobContext.setCommandDTO(commandDTO);
        List<Stage> stages = new ArrayList<>();
        ReflectionTestUtils.setField(componentStartJob, "jobContext", jobContext);
        ReflectionTestUtils.setField(componentStartJob, "stages", stages);

        MockedStatic<?> mocked1 = mockStatic(StageContext.class);
        MockedStatic<?> mocked2 = mockStatic(ComponentStageHelper.class, InvocationOnMock::callRealMethod);

        when(StageContext.fromCommandDTO(any())).thenReturn(new StageContext());

        List<Stage> stageList = List.of(mock(ComponentStartStage.class));
        mocked2.when(() -> ComponentStageHelper.createComponentStages(anyMap(), eq(Command.START), any()))
                .thenReturn(stageList);

        doCallRealMethod().when(componentStartJob).createStages();
        componentStartJob.createStages();

        assertEquals(1, stages.size());
        assertTrue(stages.containsAll(stageList));

        mocked1.close();
        mocked2.close();
    }

    @Test
    public void testGetName() {
        doCallRealMethod().when(componentStartJob).getName();
        assertEquals("Start components", componentStartJob.getName());
    }
}
