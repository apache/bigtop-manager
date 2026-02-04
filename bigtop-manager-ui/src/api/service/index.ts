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

import type { ListParams } from '../types'
import type {
  ServiceParams,
  ServiceVO,
  ServiceConfig,
  ServiceConfigSnapshot,
  ServiceList,
  SnapshotData,
  SnapshotRecovery,
  ServiceListParams
} from './types'
import { get, post, del } from '@/api/request-util'

export const getServiceList = (clusterId: number, params?: ListParams & ServiceListParams) => {
  return get<ServiceList>(`/clusters/${clusterId}/services`, params)
}

export const getService = (pathParams: ServiceParams) => {
  return get<ServiceVO>(`/clusters/${pathParams.clusterId}/services/${pathParams.id}`)
}

export const removeService = (pathParams: ServiceParams) => {
  return del<boolean>(`/clusters/${pathParams.clusterId}/services/${pathParams.id}`)
}

export const getServiceConfigs = (pathParams: ServiceParams) => {
  return get<ServiceConfig[]>(`/clusters/${pathParams.clusterId}/services/${pathParams.id}/configs`)
}

export const updateServiceConfigs = (pathParams: ServiceParams, data: ServiceConfig[]) => {
  return post<ServiceConfig[]>(`/clusters/${pathParams.clusterId}/services/${pathParams.id}/configs`, data)
}

export const getServiceConfigSnapshotsList = (pathParams: ServiceParams) => {
  return get<ServiceConfigSnapshot[]>(`/clusters/${pathParams.clusterId}/services/${pathParams.id}/config-snapshots`)
}

export const takeServiceConfigSnapshot = (pathParams: ServiceParams, data: SnapshotData) => {
  return post<ServiceConfigSnapshot>(
    `/clusters/${pathParams.clusterId}/services/${pathParams.id}/config-snapshots`,
    data
  )
}

export interface EnableHdfsHaReq {
  activeNameNodeHost: string
  standbyNameNodeHost: string
  journalNodeHosts: string[]
  zookeeperServiceId?: number
  zookeeperHosts?: string[]
  zkfcHosts: string[]
  nameservice: string
}

export const enableHdfsHa = (clusterId: number, serviceId: number, data: EnableHdfsHaReq) => {
  return post(`/clusters/${clusterId}/services/${serviceId}/actions/enable-hdfs-ha`, data)
}

export interface EnableYarnRmHaReq {
  /** rm1 host */
  activeResourceManagerHost: string
  /** rm2 host */
  standbyResourceManagerHost: string
  /** yarn.resourcemanager.ha.rm-ids，rm1,rm2 */
  rmIds: string[]
  /** yarn.resourcemanager.cluster-id */
  yarnClusterId: string
  /** zookeeper service id */
  zookeeperServiceId?: number
  /** zookeeper hosts */
  zookeeperHosts?: string[]
}

export const enableYarnRmHa = (clusterId: number, serviceId: number, data: EnableYarnRmHaReq) => {
  return post(`/clusters/${clusterId}/services/${serviceId}/actions/enable-yarn-rm-ha`, data)
}

export const recoveryServiceConfigSnapshot = (pathParams: SnapshotRecovery) => {
  return post<ServiceConfig[]>(
    `/clusters/${pathParams.clusterId}/services/${pathParams.id}/config-snapshots/${pathParams.snapshotId}`
  )
}
export const deleteServiceConfigSnapshot = (pathParams: SnapshotRecovery) => {
  return del<boolean>(
    `/clusters/${pathParams.clusterId}/services/${pathParams.id}/config-snapshots/${pathParams.snapshotId}`
  )
}
