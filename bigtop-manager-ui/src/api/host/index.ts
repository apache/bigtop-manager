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

import type { HostListParams, HostParams, HostVO, HostVOList, InstalledStatusVO } from '@/api/host/types'
import type { ComponentVO } from '@/api/component/types.ts'
import { get, post, del, put } from '@/api/request-util'

export const getHosts = (params?: HostListParams) => {
  return get<HostVOList>('/hosts', params)
}

export const getHost = (pathParams: { id: number }) => {
  return get<HostVO>(`/hosts/${pathParams.id}`)
}

export const addHost = (data: HostParams): Promise<HostVO> => {
  return post<HostVO>('/hosts', data)
}

export const installDependencies = (data: HostParams) => {
  return post('/hosts/install-dependencies', data)
}

export const getInstalledStatus = () => {
  return get<InstalledStatusVO[]>('/hosts/installed-status')
}

export const updateHost = (data: HostParams) => {
  return put<HostVO[]>(`/hosts/${data.id}`, data)
}

export const removeHost = (data: { ids: number[] }) => {
  return del<boolean>('/hosts/batch', { data })
}

export const getComponentsByHost = (pathParams: { id: number }): Promise<ComponentVO[]> => {
  return get<ComponentVO[]>(`/hosts/${pathParams.id}/components`)
}

export const startAgent = (pathParams: { id: number }) => {
  return post<boolean>(`/hosts/${pathParams.id}/start-agent`)
}

export const restartAgent = (pathParams: { id: number }) => {
  return post<boolean>(`/hosts/${pathParams.id}/restart-agent`)
}

export const stopAgent = (pathParams: { id: number }) => {
  return post<boolean>(`/hosts/${pathParams.id}/stop-agent`)
}
