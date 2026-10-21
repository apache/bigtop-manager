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

import dayjs from 'dayjs'
import * as ai from '@/api/ai-assistant/index'
import { SenderType } from '@/api/ai-assistant/types'
import { getRandomFromTimestamp } from '@/utils/tools'
import { useLlmConfigStore } from '../llm-config'

import type { AxiosProgressEvent, Canceler } from 'axios'
import type { ChatMessageItem, ChatThread, ReceivedMessageItem, ThreadId } from '@/api/ai-assistant/types'

export const useAiChatStore = defineStore(
  'ai-assistant',
  () => {
    const llmConfigStore = useLlmConfigStore()
    const { currAuthPlatform } = storeToRefs(llmConfigStore)

    const currThread = ref<ChatThread>({})
    const threads = ref<ChatThread[]>([])
    const chatRecords = ref<ChatMessageItem[]>([])
    const canceler = ref<Canceler>()
    const messageReceiver = ref('')
    const isSending = ref(false)
    const loadingChatRecords = ref(false)
    const toolExecutions = ref<ChatMessageItem[]>([])
    const pendingAiRecord = ref<ChatMessageItem | null>(null)
    const streamRecords = computed(() => {
      const records = [...toolExecutions.value]
      if (pendingAiRecord.value) {
        records.push(pendingAiRecord.value)
      }
      return records
    })

    const hasActivePlatform = computed(() => Object.keys(currAuthPlatform.value || {}).length > 0)
    const threadLimit = computed(() => threads.value.length >= 10)

    watch(currAuthPlatform, (val, oldVal) => {
      if (val == undefined || !hasActivePlatform.value) {
        currThread.value = {}
      } else if (val.llmConfigId !== oldVal?.llmConfigId) {
        currThread.value = {}
      }
    })

    const resetState = () => {
      currThread.value = {}
      threads.value = []
      messageReceiver.value = ''
      isSending.value = false
      loadingChatRecords.value = false
      toolExecutions.value = []
      pendingAiRecord.value = null
    }

    const initCurrThread = () => {
      if (!hasActivePlatform.value) {
        return
      }
      if (Object.keys(currThread.value).length == 0) {
        if (threads.value.length === 0) {
          createChatThread()
        } else {
          currThread.value = threads.value[0]
          getThreadRecords()
        }
      } else {
        getThreadsFromAuthPlatform()
        getThreadRecords()
      }
    }

    const createChatThread = async (quickCreate = false) => {
      try {
        const tempName = `thread-${getRandomFromTimestamp()}`
        const data = await ai.createChatThread({ id: null, name: tempName })
        if (data) {
          await getThread(data.threadId as ThreadId, quickCreate)
          getThreadsFromAuthPlatform()
        }
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    const updateChatThread = async (thread: ChatThread, newName: string) => {
      try {
        const threadId = thread.threadId as ThreadId
        await ai.updateThread(threadId, { name: newName })
        return true
      } catch (error) {
        console.log('error :>> ', error)
        return false
      }
    }

    const deleteChatThread = async (thread: ChatThread) => {
      try {
        const threadId = thread.threadId as ThreadId
        await ai.deleteThread(threadId)
        return true
      } catch (error) {
        console.log('error :>> ', error)
        return false
      }
    }

    const getThread = async (threadId: ThreadId, quickCreate = false) => {
      try {
        const data = await ai.getThread(threadId)
        currThread.value = data
        if (!quickCreate) {
          await getThreadRecords()
        }
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    const getThreadsFromAuthPlatform = async () => {
      try {
        const data = await ai.getThreadsFromAuthPlatform()
        threads.value = data
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    const getThreadRecords = async () => {
      try {
        loadingChatRecords.value = true
        const { threadId } = currThread.value
        const data = await ai.getThreadRecords(threadId as ThreadId)
        chatRecords.value = data
      } catch (error) {
        console.log('error :>> ', error)
      } finally {
        loadingChatRecords.value = false
      }
    }

    const talkWithChatbot = async (message: string) => {
      try {
        const { threadId } = currThread.value
        isSending.value = true
        if (threadId) {
          const { cancel, promise } = ai.talkWithChatbot(threadId, { message }, onMessageReceive)
          canceler.value = cancel
          return promise.then(onMessageComplete)
        }
      } catch (error) {
        isSending.value = false
        console.log('error :>> ', error)
      }
    }

    const collectReceiveMessage = async (message: string) => {
      try {
        toolExecutions.value = []
        pendingAiRecord.value = null
        if (threads.value.length === 0) {
          await createChatThread(true)
        }
        await talkWithChatbot(message)
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    const checkReceiveMessageError = (item: ReceivedMessageItem) => {
      if (item.content != undefined) {
        return item.content
      } else if (item.finishReason && item.finishReason != 'completed') {
        return item.finishReason
      }
    }

    const updateToolExecution = (item: ReceivedMessageItem) => {
      const { executionId, toolName, toolStatus, toolPayload } = item
      if (!executionId || !toolName || !toolStatus) {
        return
      }

      const prevRecord = toolExecutions.value.find((execution) => execution.executionId === executionId)
      const record = {
        sender: 'AI' as const,
        message: '',
        messageType: 'tool' as const,
        executionId,
        toolName,
        toolStatus,
        toolPayload,
        toolInput: toolStatus === 'started' ? toolPayload || '' : prevRecord?.toolInput || '',
        toolOutput: toolStatus === 'completed' ? toolPayload || '' : prevRecord?.toolOutput || '',
        toolError: toolStatus === 'failed' ? toolPayload || '' : prevRecord?.toolError || '',
        createTime: dayjs().format()
      }

      const index = toolExecutions.value.findIndex((execution) => execution.executionId === executionId)

      if (index === -1) {
        toolExecutions.value.push(record)
      } else {
        toolExecutions.value.splice(index, 1, record)
      }
    }

    const onMessageReceive = ({ event }: AxiosProgressEvent) => {
      if (!pendingAiRecord.value) {
        pendingAiRecord.value = {
          sender: 'AI',
          message: '',
          createTime: dayjs().format()
        }
      }

      messageReceiver.value = event.target.responseText
        .split('data:')
        .map((stream: string) => {
          if (stream.length > 0) {
            const parsedMsgInfo = JSON.parse(stream.trimEnd())
            if (parsedMsgInfo.eventType === 'tool_execution') {
              updateToolExecution(parsedMsgInfo)
              return ''
            }
            return checkReceiveMessageError(parsedMsgInfo)
          }
        })
        .join('')

      if (pendingAiRecord.value) {
        pendingAiRecord.value.message = messageReceiver.value
      }
    }

    const onMessageComplete = () => {
      const currentToolExecutions = toolExecutions.value.map((record) => ({ ...record }))
      const currentAiRecord = pendingAiRecord.value ? { ...pendingAiRecord.value } : null
      const formatResultMsg = currentAiRecord?.message || messageReceiver.value

      if (currentToolExecutions.length > 0) {
        chatRecords.value.push(...currentToolExecutions)
      }
      if (currentAiRecord && currentAiRecord.message) {
        chatRecords.value.push(currentAiRecord)
      }

      messageReceiver.value = ''
      toolExecutions.value = []
      pendingAiRecord.value = null
      cancelSseConnect()
      isSending.value = false
      return Promise.resolve({ message: formatResultMsg, state: true })
    }

    const cancelSseConnect = () => {
      if (!canceler.value) {
        return
      }
      canceler.value()
    }

    const setChatRecordForSender = async (sender: SenderType, message: string) => {
      chatRecords.value.push({
        sender,
        message,
        createTime: dayjs().format()
      })
    }

    return {
      currThread,
      threads,
      chatRecords,
      messageReceiver,
      isSending,
      loadingChatRecords,
      toolExecutions,
      pendingAiRecord,
      streamRecords,
      threadLimit,
      hasActivePlatform,
      initCurrThread,
      talkWithChatbot,
      createChatThread,
      updateChatThread,
      deleteChatThread,
      getThread,
      getThreadsFromAuthPlatform,
      cancelSseConnect,
      getThreadRecords,
      setChatRecordForSender,
      collectReceiveMessage,
      resetState
    }
  },
  {
    persist: {
      storage: sessionStorage,
      paths: ['currThread']
    }
  }
)
