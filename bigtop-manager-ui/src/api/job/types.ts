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

import type { PageVO } from '@/api/types'

export type JobList = PageVO<Job>
export type StageList = PageVO<Stage>
export type JobParams = { clusterId: number; jobId: number }
export type TaskParams = JobParams & { stageId: number }
export type TaskLogParams = TaskParams & { taskId: number }

export interface Job {
  createTime?: string
  id?: number
  name?: string
  state?: string
  updateTime?: string
}

export interface Stage {
  createTime?: string
  id?: number
  name?: string
  order?: number
  state?: string
  tasks?: Task[]
  updateTime?: string
}
export interface Task {
  createTime?: string
  hostname?: string
  id?: number
  name?: string
  state?: string
  updateTime?: string
}
