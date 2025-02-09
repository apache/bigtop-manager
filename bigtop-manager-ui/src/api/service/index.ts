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
  ServiceParams,
  ServiceVO,
  ServiceConfig,
  ServiceConfigSnapshot,
  ServiceList,
  SnapshotData,
  SnapshotRecovery
} from './types'
import request from '@/api/request.ts'

export const getServiceList = (clusterId: number): Promise<ServiceList> => {
  return request({
    method: 'get',
    url: `/clusters/${clusterId}/services`
  })
}

export const getService = (params: ServiceParams): Promise<ServiceVO> => {
  return request({
    method: 'get',
    url: `/clusters/${params.clusterId}/services/${params.id}`
  })
}

export const getServiceConfigs = (params: ServiceParams): Promise<ServiceConfig[]> => {
  return request({
    method: 'get',
    url: `/clusters/${params.clusterId}/services/${params.id}/configs`
  })
}

export const updateServiceConfigs = (params: ServiceParams, data: ServiceConfig): Promise<ServiceConfig[]> => {
  return request({
    method: 'post',
    url: `/clusters/${params.clusterId}/services/${params.id}/configs`,
    data
  })
}

export const getServiceConfigSnapshotsList = (params: ServiceParams): Promise<ServiceConfigSnapshot[]> => {
  return request({
    method: 'get',
    url: `/clusters/${params.clusterId}/services/${params.id}/config-snapshots`
  })
}

export const takeServiceConfigSnapshot = (
  params: ServiceParams,
  data: SnapshotData
): Promise<ServiceConfigSnapshot> => {
  return request({
    method: 'post',
    url: `/clusters/${params.clusterId}/services/${params.id}/config-snapshots`,
    data
  })
}

export const recoveryServiceConfigSnapshot = (params: SnapshotRecovery): Promise<ServiceConfig[]> => {
  return request({
    method: 'post',
    url: `/clusters/${params.clusterId}/services/${params.id}/config-snapshots/${params.snapshotId}`
  })
}
export const deleteServiceConfigSnapshot = (params: SnapshotRecovery): Promise<boolean> => {
  return request({
    method: 'delete',
    url: `/clusters/${params.clusterId}/services/${params.id}/config-snapshots/${params.snapshotId}`
  })
}
