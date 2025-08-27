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
import type { ChatMessageItem, ChatThread, CreateChatThread, ThreadId, UpdateChatThread } from './types'
import axios, { AxiosProgressEvent, AxiosResponse, CancelTokenSource } from 'axios'

export const getThreadsFromAuthPlatform = (): Promise<ChatThread[]> => {
  return request({
    method: 'get',
    url: '/llm/chatbot/threads'
  })
}

export const getThread = (threadId: ThreadId): Promise<ChatThread> => {
  return request({
    method: 'get',
    url: `/llm/chatbot/threads/${threadId}`
  })
}

export const getThreadRecords = (threadId: ThreadId): Promise<ChatMessageItem[]> => {
  return request({
    method: 'get',
    url: `/llm/chatbot/threads/${threadId}/history`
  })
}

export const updateThread = (threadId: ThreadId, data: UpdateChatThread): Promise<ChatThread> => {
  return request({
    method: 'put',
    url: `/llm/chatbot/threads/${threadId}`,
    data
  })
}

export const deleteThread = (threadId: ThreadId): Promise<boolean> => {
  return request({
    method: 'delete',
    url: `/llm/chatbot/threads/${threadId}`
  })
}

export const createChatThread = (data: CreateChatThread): Promise<ChatThread> => {
  return request({
    method: 'post',
    url: '/llm/chatbot/threads',
    data
  })
}

export interface ChatMessagesRes<T> {
  promise: Promise<T>
  cancel: () => void
}

export const talkWithChatbot = (
  threadId: ThreadId,
  data: { message: string },
  func: (progressEvent: AxiosProgressEvent) => void
): ChatMessagesRes<AxiosResponse<ChatMessageItem[], any>> => {
  const source: CancelTokenSource = axios.CancelToken.source()

  const promise = request({
    method: 'post',
    url: `/llm/chatbot/threads/${threadId}/talk`,
    responseType: 'stream',
    data: {
      message: data.message
    },
    timeout: 0,
    cancelToken: source.token,
    onDownloadProgress: (progressEvent: AxiosProgressEvent) => func(progressEvent)
  })

  return { promise, cancel: source.cancel }
}
