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
import {
  AuthorizedPlatform,
  SupportedPlatForm,
  CredentialFormItem,
  AuthCredentialTestParams,
  AuthTestResult,
  ChatThread,
  ChatThreadCondition
} from '@/api/chatbot/types.ts'

export const getAuthorizedPlatforms = (): Promise<AuthorizedPlatform[]> => {
  return request({
    method: 'get',
    url: '/chatbot/platforms/authorized'
  })
}
export const getSupportedPlatforms = (): Promise<SupportedPlatForm[]> => {
  return request({
    method: 'get',
    url: '/chatbot/platforms'
  })
}
export const getCredentialFormModelofPlatform = (
  platformId: string | number
): Promise<CredentialFormItem[]> => {
  return request({
    method: 'get',
    url: `/chatbot/platforms/${platformId}/auth/credential`
  })
}

export const validateAuthCredentials = (
  data: AuthCredentialTestParams
): Promise<AuthTestResult> => {
  return request({
    method: 'put',
    url: '/chatbot/platforms',
    data
  })
}
export const getChatThreads = (
  params: ChatThreadCondition
): Promise<ChatThread[]> => {
  return request({
    method: 'get',
    url: `/chatbot/platforms/${params.platformId}/threads`,
    params
  })
}

export const createChatThread = (
  params: ChatThreadCondition
): Promise<ChatThread> => {
  return request({
    method: 'put',
    url: `/chatbot/platforms/${params.platformId}/threads`,
    params
  })
}
