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
import type {
  JobParams,
  JobList,
  TaskLogParams,
  JobVO,
  JobListParams,
  TaskListParams,
  StageListParams,
  StageList,
  TaskList,
  LogsRes
} from './types'
import type { ListParams } from '../types'
import { get, post } from '../request-util'
import axios, { type AxiosProgressEvent, type CancelTokenSource } from 'axios'
import request from '../request'

export const retryJob = (pathParams: JobParams) => {
  return post<JobVO>(`/clusters/${pathParams.clusterId}/jobs/${pathParams.jobId}/retry`)
}

export const getJobDetails = (pathParams: JobParams) => {
  return get<JobVO>(`/clusters/${pathParams.clusterId}/jobs/${pathParams.jobId}`)
}

export const getJobList = (pathParams: JobListParams, params: ListParams) => {
  return get<JobList>(`/clusters/${pathParams.clusterId}/jobs`, params)
}

export const getStageList = (pathParams: StageListParams, params: ListParams) => {
  return get<StageList>(`/clusters/${pathParams.clusterId}/jobs/${pathParams.jobId}/stages`, params)
}

export const getTaskList = (pathParams: TaskListParams, params: ListParams) => {
  return get<TaskList>(
    `/clusters/${pathParams.clusterId}/jobs/${pathParams.jobId}/stages/${pathParams.stageId}/tasks`,
    params
  )
}

// eslint-disable-next-line @typescript-eslint/no-unsafe-function-type
export const getTaskLog = (pathParams: TaskLogParams, func: Function): LogsRes => {
  const source: CancelTokenSource = axios.CancelToken.source()

  const promise = request({
    method: 'get',
    url: `/clusters/${pathParams.clusterId}/jobs/${pathParams.jobId}/stages/${pathParams.stageId}/tasks/${pathParams.taskId}/log`,
    responseType: 'stream',
    timeout: 0,
    cancelToken: source.token,
    onDownloadProgress: (progressEvent: AxiosProgressEvent) => func(progressEvent)
  })

  return { promise, cancel: source.cancel }
}
