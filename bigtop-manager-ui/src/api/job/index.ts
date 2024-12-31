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
import { get, post } from '@/api/request-util'
import type { JobParams, Job, JobList, Stage, Task, TaskLogParams, TaskParams } from './types'

export const getJobList = (clusterId: number) => {
  return get<JobList>(`/clusters/${clusterId}/jobs`)
}

export const retryJob = (params: JobParams) => {
  return post<Job[]>(`/clusters/${params.clusterId}/jobs/${params.jobId}/retry`)
}

export const getStageList = (params: JobParams) => {
  return get<Stage[]>(`/clusters/${params.clusterId}/jobs/${params.jobId}/stages`)
}

export const getTaskList = (params: TaskParams) => {
  return get<Task[]>(`/clusters/${params.clusterId}/jobs/${params.jobId}/stages/${params.stageId}/tasks`)
}

export const getTaskLog = (params: TaskLogParams) => {
  return get<string[]>(
    `/clusters/${params.clusterId}/jobs/${params.jobId}/stages/${params.stageId}/tasks/${params.taskId}/log`
  )
}
