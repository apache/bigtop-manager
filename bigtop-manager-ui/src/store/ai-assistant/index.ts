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
  ChatMessageItem,
  ChatThread,
  ThreadId
} from '@/api/ai-assistant/types'
import * as ai from '@/api/ai-assistant/index'
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAiChatStore = defineStore(
  'ai-assistant',
  () => {
    const currThread = ref<ChatThread>({})
    const threads = ref<ChatThread[]>([])
    const chatRecords = ref<ChatMessageItem[]>([])

    const createChatThread = async () => {
      const data = await ai.createChatThread({
        id: null,
        name: `thread-${Date.now().toString().slice(-6)}`
      })
      currThread.value = data
      getThreadsFromAuthPlatform()
    }

    const getThread = async (threadId: ThreadId) => {
      const data = await ai.getThread(threadId)
      currThread.value = data
    }

    const getThreadsFromAuthPlatform = async () => {
      const data = await ai.getThreadsFromAuthPlatform()
      threads.value = data
    }

    return {
      currThread,
      threads,
      chatRecords,
      createChatThread,
      getThread,
      getThreadsFromAuthPlatform
    }
  },
  {
    persist: {
      storage: sessionStorage,
      paths: ['currThread']
    }
  }
)
