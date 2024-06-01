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

export enum State {
  Pending = '#1677ff',
  Processing = '#1677fe',
  Successful = '#52c41a',
  Failed = '#ff4d4f',
  Canceled = '#80868b'
}

export type StateType = keyof typeof State

interface BaseVO {
  id: number
  name: string
  state: StateType
  createTime?: string
  updateTime?: string
  progress?: number
}

export interface JobVO extends BaseVO {
  stages: StageVO[]
}

export interface StageVO extends BaseVO {
  order: number
  tasks: TaskVO[]
}

export interface TaskVO extends BaseVO {
  hostname: string
}

export interface Pagination {
  pageNum: number
  pageSize: number
  sort?: 'asc' | 'desc'
  orderBy?: string
}

export interface OuterData {
  meta: JobVO[]
  currItem: StageVO | TaskVO | undefined
}
