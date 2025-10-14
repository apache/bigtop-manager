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

import type { ClusterStatusType } from '@/api/cluster/types'
import type { HostStatusType } from '@/api/host/types'
import type { StateType } from '@/api/job/types'
import type { TimeRangeType } from '@/api/metrics/types'
import type { ServiceStatusType } from '@/api/service/types'
import { CommonStatusTexts } from '@/enums/state'

export type Status = ServiceStatusType | HostStatusType | ClusterStatusType

export type StatusColorType = Record<Status, keyof typeof CommonStatusTexts>

export const API_RETRY_TIME = 3
export const API_EXPIRE_TIME = 30 * 1000
export const JOB_SCHEDULE_INTERVAL = 1000
export const MONITOR_SCHEDULE_INTERVAL = 10 * 1000
export const DEFAULT_PAGE_SIZE = 10
export const POLLING_INTERVAL = 30000

export const TIME_RANGES: TimeRangeType[] = ['1m', '5m', '15m', '30m', '1h', '2h']
export const HOST_STATUS = ['INSTALLING', 'SUCCESS', 'FAILED', 'UNKNOWN']
export const COMPONENT_STATUS = ['INSTALLING', 'SUCCESS', 'FAILED', 'UNKNOWN']

export const STATUS_COLOR: StatusColorType = {
  1: 'healthy',
  2: 'unhealthy',
  3: 'unknown'
}

export const JOB_STATUS: Record<StateType, string> = {
  Pending: 'installing',
  Processing: 'processing',
  Failed: 'failed',
  Canceled: 'canceled',
  Successful: 'success'
}
