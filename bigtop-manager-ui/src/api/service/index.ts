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

import { del, get, post } from '@/api/request-util'
import type {
  ServiceParams,
  Service,
  ServiceConfig,
  ServiceConfigSnapshot,
  ServiceList,
  SnapshotData,
  SnapshotRecovery
} from './type'

export const getServiceList = (clusterId: number) => {
  return get<ServiceList>(`/clusters/${clusterId}/services`)
}

export const getService = (params: ServiceParams) => {
  return get<Service>(`/clusters/${params.clusterId}/services/${params.id}`)
}

export const getServiceConfigs = (params: ServiceParams) => {
  return get<ServiceConfig[]>(`/clusters/${params.clusterId}/services/${params.id}/configs`)
}

export const updateServiceConfigs = (params: ServiceParams, data: ServiceConfig) => {
  return post<ServiceConfig[]>(`/clusters/${params.clusterId}/services/${params.id}/configs,`, data)
}

export const getServiceConfigSnapshotsList = (params: ServiceParams) => {
  return get<ServiceConfigSnapshot[]>(`/clusters/${params.clusterId}/services/${params.id}/config-snapshots`)
}

export const takeServiceConfigSnapshot = (params: ServiceParams, data: SnapshotData) => {
  return post<ServiceConfigSnapshot>(`/clusters/${params.clusterId}/services/${params.id}/config-snapshots,`, data)
}

export const recoveryServiceConfigSnapshot = (params: SnapshotRecovery) => {
  return post<ServiceConfig[]>(
    `/clusters/${params.clusterId}/services/${params.id}/config-snapshots/${params.snapshotId}`
  )
}
export const deleteServiceConfigSnapshot = (params: SnapshotRecovery) => {
  return del<boolean>(`/clusters/${params.clusterId}/services/${params.id}/config-snapshots/${params.snapshotId}`)
}
