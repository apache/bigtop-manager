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
package org.apache.bigtop.manager.server.command.stage.runner;

import org.apache.bigtop.manager.dao.po.Stage;
import org.apache.bigtop.manager.dao.po.Task;
import org.apache.bigtop.manager.server.command.stage.factory.StageContext;
import org.apache.bigtop.manager.server.command.stage.factory.StageType;

/**
 * Interface for running stages.
 * A stage represents a phase in a job, and this interface provides methods for managing and running these stages.
 */
public interface StageRunner {

    /**
     * Get the type of the stage that this runner is responsible for.
     *
     * @return The type of the stage.
     */
    StageType getStageType();

    /**
     * Set the stage that this runner will manage.
     *
     * @param stage The stage to be managed by this runner.
     */
    void setStage(Stage stage);

    /**
     * Set the context for the stage. The context may contain additional information necessary for running the stage.
     *
     * @param stageContext The context for the stage.
     */
    void setStageContext(StageContext stageContext);

    /**
     * Method to be called before running the stage. Can be used for setup and preparation.
     */
    void beforeRun();

    /**
     * Run the stage. This is where the main logic of the stage should be implemented.
     */
    void run();

    /**
     * Method to be called after the stage has successfully run. Can be used for cleanup and finalization.
     */
    void onSuccess();

    /**
     * Method to be called if the stage fails to run. Can be used for error handling and recovery.
     */
    void onFailure();

    /**
     * Method to be called before running a task in the stage. Can be used for task-specific setup and preparation.
     *
     * @param task The task that is about to be run.
     */
    void beforeRunTask(Task task);

    /**
     * Method to be called after a task in the stage has successfully run. Can be used for task-specific cleanup and finalization.
     *
     * @param task The task that has been successfully run.
     */
    void onTaskSuccess(Task task);

    /**
     * Method to be called if a task in the stage fails to run. Can be used for task-specific error handling and recovery.
     *
     * @param task The task that has failed to run.
     */
    void onTaskFailure(Task task);
}
