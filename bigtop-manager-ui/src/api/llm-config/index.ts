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
import { ResponseEntity } from '@/api/types'
import {
  Key,
  Platform,
  PlatformCredential,
  AuthorizedPlatform,
  UpdateAuthorizedPlatformConfig
} from './types'

export const getPlatforms = (): Promise<ResponseEntity<Platform[]>> => {
  return request({
    method: 'get',
    url: '/llm/config/platforms'
  })
}

export const getAuthorizedPlatforms = (): Promise<
  ResponseEntity<AuthorizedPlatform[]>
> => {
  return request({
    method: 'get',
    url: '/llm/config/auth-platforms'
  })
}

export const getPlatformCredentials = (
  platformId: Key
): Promise<ResponseEntity<PlatformCredential[]>> => {
  return request({
    method: 'get',
    url: `/llm/config/platforms/${platformId}/auth-credentials`
  })
}

export const addAuthorizedPlatform = (
  data: UpdateAuthorizedPlatformConfig
): Promise<ResponseEntity<AuthorizedPlatform>> => {
  return request({
    method: 'post',
    url: '/llm/config/auth-platforms',
    data
  })
}

export const updateAuthPlatform = (
  authId: Key
): Promise<ResponseEntity<UpdateAuthorizedPlatformConfig>> => {
  return request({
    method: 'put',
    url: `/llm/config/auth-platforms/${authId}`
  })
}

export const deleteAuthPlatform = (
  authId: Key
): Promise<ResponseEntity<boolean>> => {
  return request({
    method: 'delete',
    url: `/llm/config/auth-platforms/${authId}`
  })
}

export const deactivateAuthorizedPlatform = (
  authId: Key
): Promise<ResponseEntity<boolean>> => {
  return request({
    method: 'post',
    url: `/llm/config/auth-platforms/${authId}/deactivate`
  })
}

export const activateAuthorizedPlatform = (
  authId: Key
): Promise<ResponseEntity<boolean>> => {
  return request({
    method: 'post',
    url: `/llm/config/auth-platforms/${authId}/activate`
  })
}

export const testAuthorizedPlatform = (
  data: UpdateAuthorizedPlatformConfig
): Promise<ResponseEntity<boolean>> => {
  return request({
    method: 'post',
    url: '/llm/config/auth-platforms/test',
    data
  })
}
