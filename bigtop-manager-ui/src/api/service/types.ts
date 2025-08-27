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

import type { ComponentVO } from '@/api/component/types'
import type { PageVO } from '@/api/types'

export type ServiceList = PageVO<ServiceVO>
export type ServiceParams = { clusterId: number; id: number }
export type SnapshotData = { desc?: string; name?: string }
export type SnapshotRecovery = ServiceParams & { snapshotId: number }
export type ServiceStatusType = 1 | 2 | 3
export type ServiceListParams = {
  name?: string
  restartFlag?: boolean
  status?: ServiceStatusType
}

export interface ServiceVO {
  components?: ComponentVO[]
  configs?: ServiceConfig[]
  desc?: string
  displayName?: string
  id?: number
  name?: string
  requiredServices?: string[]
  restartFlag?: boolean
  stack?: string
  status: ServiceStatusType
  user?: string
  version?: string
  license?: string
  isInstalled?: boolean
  metrics?: boolean
  kerberos?: boolean
}

export interface ServiceConfig {
  id?: number
  name?: string
  properties?: Property[]
}

type PropertyAction = 'add' | 'update' | 'delete'

export interface Property {
  attrs?: Attr
  desc?: string
  displayName?: string
  name: string
  value?: string
  action?: PropertyAction
  [property: string]: any
}

export interface Attr {
  type?: string
  required?: boolean
}

export interface ServiceConfigSnapshot {
  configJson?: string
  desc?: string
  id?: number
  name?: string
  [property: string]: any
}
