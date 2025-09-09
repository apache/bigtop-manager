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

import request from '@/api/request.ts'
import type { HostListParams, HostParams, HostVO, HostVOList, InstalledStatusVO } from '@/api/host/types'
import type { ComponentVO } from '@/api/component/types.ts'

export const getHosts = (params?: HostListParams): Promise<HostVOList> => {
  return request({
    method: 'get',
    url: '/hosts',
    params
  })
}
export const getHost = (pathParams: { id: number }): Promise<HostVO> => {
  return request({
    method: 'get',
    url: `/hosts/${pathParams.id}`
  })
}

export const addHost = (data: HostParams): Promise<HostVO> => {
  return request({
    method: 'post',
    url: '/hosts',
    data
  })
}

export const installDependencies = (data: HostParams) => {
  return request({
    method: 'post',
    url: '/hosts/install-dependencies',
    data
  })
}

export const getInstalledStatus = (): Promise<InstalledStatusVO[]> => {
  return request({
    method: 'get',
    url: '/hosts/installed-status'
  })
}

export const updateHost = (data: HostParams): Promise<HostVO[]> => {
  return request({
    method: 'put',
    url: `/hosts/${data.id}`,
    data
  })
}

export const removeHost = (data: { ids: number[] }): Promise<boolean> => {
  return request({
    method: 'delete',
    url: '/hosts/batch',
    data
  })
}

export const getComponentsByHost = (pathParams: { id: number }): Promise<ComponentVO[]> => {
  return request({
    method: 'get',
    url: `/hosts/${pathParams.id}/components`
  })
}
