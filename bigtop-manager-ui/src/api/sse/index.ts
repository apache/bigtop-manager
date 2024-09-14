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

import axios, { type AxiosProgressEvent, type CancelTokenSource } from 'axios'
import request from '@/api/request.ts'
import type { chatMessagesRes, LogsRes } from './types'
import type { sendChatMessageCondition } from '@/api/chatbot/types'

export const getLogs = (
  clusterId: number,
  id: number,
  func: Function
): LogsRes => {
  const source: CancelTokenSource = axios.CancelToken.source()

  const promise = request({
    method: 'get',
    url: `/sse/clusters/${clusterId}/tasks/${id}/log`,
    responseType: 'stream',
    timeout: 0,
    cancelToken: source.token,
    onDownloadProgress: (progressEvent: AxiosProgressEvent) =>
      func(progressEvent)
  })

  return { promise, cancel: source.cancel }
}
export const sendChatMessage = (
  data: sendChatMessageCondition,
  func: Function
): chatMessagesRes => {
  const source: CancelTokenSource = axios.CancelToken.source()

  const promise = request({
    method: 'post',
    url: `/chatbot/platforms/${data.platformId}/threads/${data.threadId}/talk`,
    responseType: 'stream',
    data: {
      message: data.message
    },
    timeout: 0,
    cancelToken: source.token,
    onDownloadProgress: (progressEvent: AxiosProgressEvent) =>
      func(progressEvent)
  })

  return { promise, cancel: source.cancel }
}
