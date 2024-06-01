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
package org.apache.bigtop.manager.server.command.scheduler;

import org.apache.bigtop.manager.dao.entity.Job;

/**
 * JobScheduler interface for job management.
 * This interface provides methods to submit, start, and stop jobs.
 */
public interface JobScheduler {

    /**
     * Submits a job to the job scheduler.
     * @param job The job to be submitted.
     */
    void submit(Job job);

    /**
     * Starts the job scheduler.
     * This method should be called after all jobs have been submitted.
     */
    void start();

    /**
     * Stops the job scheduler.
     * This method should be called to gracefully stop the job scheduler.
     */
    void stop();
}
