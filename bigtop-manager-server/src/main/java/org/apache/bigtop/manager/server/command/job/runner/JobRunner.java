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

import org.apache.bigtop.manager.dao.po.Job;
import org.apache.bigtop.manager.server.command.CommandIdentifier;
import org.apache.bigtop.manager.server.command.job.factory.JobContext;

/**
 * Interface for running jobs in the application.
 * A job represents a unit of work, and this interface provides methods for managing and running these jobs.
 */
public interface JobRunner {

    /**
     * Get the identifier of the command that this runner is responsible for.
     *
     * @return The identifier of the command.
     */
    CommandIdentifier getCommandIdentifier();

    /**
     * Set the job that this runner will manage.
     *
     * @param job The job to be managed by this runner.
     */
    void setJob(Job job);

    /**
     * Set the context for the job. The context may contain additional information necessary for running the job.
     *
     * @param jobContext The context for the job.
     */
    void setJobContext(JobContext jobContext);

    /**
     * Method to be called before running the job. Can be used for setup and preparation.
     */
    void beforeRun();

    /**
     * Run the job. This is where the main logic of the job should be implemented.
     */
    void run();

    /**
     * Method to be called after the job has successfully run. Can be used for cleanup and finalization.
     */
    void onSuccess();

    /**
     * Method to be called if the job fails to run. Can be used for error handling and recovery.
     */
    void onFailure();
}
