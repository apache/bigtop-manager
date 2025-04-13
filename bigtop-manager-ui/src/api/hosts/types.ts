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

export type HostVOList = PageVO<HostVO>

export type HostStatusType = 1 | 2 | 3

/**
 * HostVO
 */
export interface HostVO {
  agentDir?: string
  arch?: string
  authType?: number
  availableProcessors?: number
  clusterDisplayName?: string
  clusterName?: string
  componentNum?: number
  desc?: string
  errInfo?: string
  freeDisk?: number
  freeMemorySize?: number
  grpcPort?: number
  hostname?: string
  id?: number
  ipv4?: string
  ipv6?: string
  os?: string
  sshKeyFilename?: string
  sshKeyPassword?: string
  sshKeyString?: string
  sshPassword?: string
  sshPort?: number
  sshUser?: string
  status?: number
  totalDisk?: number
  totalMemorySize?: number
  [property: string]: any
}

export interface HostListParams {
  clusterId?: number
  hostname?: string
  ipv4?: string
  orderBy?: string
  pageNum?: number
  pageSize?: number
  sort?: string
  status?: number
}

export interface HostParams {
  agentDir?: string
  authType?: number | string // '1-password，2-key，3-no_auth',
  clusterId?: number
  desc?: string
  grpcPort?: number
  hostnames?: string[]
  sshKeyFilename?: string
  sshKeyPassword?: string
  sshKeyString?: string
  sshPassword?: string
  sshPort?: number
  sshUser?: string
  [property: string]: any
}

export interface InstalledStatusVO {
  hostname?: string
  message?: string
  status?: Status
}

export enum Status {
  Installing = 'INSTALLING',
  Success = 'SUCCESS',
  Failed = 'FAILED',
  Unknown = 'UNKNOWN'
}

export enum BaseStatus {
  'INSTALLING',
  'SUCCESS',
  'FAILED',
  'UNKNOWN'
}
