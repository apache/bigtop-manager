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
import request from '@/api/request.ts'
import type { JobParams, JobList, TaskLogParams, TaskParams, JobVO, StageVO, TaskVO } from './types'

export const retryJob = (pathParams: JobParams): Promise<JobVO> => {
  return request({
    method: 'post',
    url: `/clusters/${pathParams.clusterId}/jobs/${pathParams.jobId}/retry`
  })
}

export const getJobList = (clusterId: number): Promise<JobList> => {
  return request({
    method: 'get',
    url: `/clusters/${clusterId}/jobs`
  })
}

export const getJobDetails = (pathParams: JobParams): Promise<JobVO> => {
  return request({
    method: 'get',
    url: `/clusters/${pathParams.clusterId}/jobs/${pathParams.jobId}`
  })
}

export const getStageList = (pathParams: JobParams): Promise<StageVO[]> => {
  return request({
    method: 'get',
    url: `/clusters/${pathParams.clusterId}/jobs/${pathParams.jobId}/stages`
  })
}

export const getTaskList = (pathParams: TaskParams): Promise<TaskVO[]> => {
  return request({
    method: 'get',
    url: `/clusters/${pathParams.clusterId}/jobs/${pathParams.jobId}/stages/${pathParams.stageId}/tasks`
  })
}

export const getTaskLog = (pathParams: TaskLogParams): Promise<string[]> => {
  return request({
    method: 'get',
    url: `/clusters/${pathParams.clusterId}/jobs/${pathParams.jobId}/stages/${pathParams.stageId}/tasks/${pathParams.taskId}/log`
  })
}
