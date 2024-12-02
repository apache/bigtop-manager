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
import { Platform, PlatformCredential, AuthorizedPlatform, UpdateAuthorizedPlatformConfig } from './types'

export const getPlatforms = (): Promise<Platform[]> => {
  return request({
    method: 'get',
    url: '/llm/config/platforms'
  })
}

export const getAuthorizedPlatforms = (): Promise<AuthorizedPlatform[]> => {
  return request({
    method: 'get',
    url: '/llm/config/auth-platforms'
  })
}

export const getPlatformCredentials = (platformId: number): Promise<PlatformCredential[]> => {
  return request({
    method: 'get',
    url: `/llm/config/platforms/${platformId}/auth-credentials`
  })
}

export const addAuthorizedPlatform = (data: UpdateAuthorizedPlatformConfig): Promise<AuthorizedPlatform> => {
  return request({
    method: 'post',
    url: '/llm/config/auth-platforms',
    data
  })
}

export const getAuthPlatformDetail = (authId: number): Promise<UpdateAuthorizedPlatformConfig> => {
  return request({
    method: 'get',
    url: `/llm/config/auth-platforms/${authId}`
  })
}

export const updateAuthPlatform = (data: UpdateAuthorizedPlatformConfig): Promise<UpdateAuthorizedPlatformConfig> => {
  return request({
    method: 'put',
    url: `/llm/config/auth-platforms/${data.id}`,
    data
  })
}

export const deleteAuthPlatform = (authId: number): Promise<boolean> => {
  return request({
    method: 'delete',
    url: `/llm/config/auth-platforms/${authId}`
  })
}

export const deactivateAuthorizedPlatform = (authId: number): Promise<boolean> => {
  return request({
    method: 'post',
    url: `/llm/config/auth-platforms/${authId}/deactivate`
  })
}

export const activateAuthorizedPlatform = (authId: number): Promise<boolean> => {
  return request({
    method: 'post',
    url: `/llm/config/auth-platforms/${authId}/activate`
  })
}

export const testAuthorizedPlatform = (data: UpdateAuthorizedPlatformConfig): Promise<boolean> => {
  return request({
    method: 'post',
    url: '/llm/config/auth-platforms/test',
    data
  })
}
