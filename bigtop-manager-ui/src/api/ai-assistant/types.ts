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

export enum Sender {
  USER,
  AI,
  SYSTEM
}

export type CreateChatThread = {
  id?: number | string | null
  name?: string | null
}

export type SenderType = keyof typeof Sender
export type UpdateChatThread = CreateChatThread
export type ThreadId = number | string

export interface ChatThread {
  threadId?: number | string
  authId?: number | string
  platformId?: number | string
  platformName?: string
  model?: string
  name?: string
  createTime?: string
  updateTime?: string
}

export interface ChatMessageItem {
  sender: SenderType
  message: string
  createTime?: string
}

export interface ReceivedMessageItem {
  content?: string
  finishReason?: 'completed' | null
}
