<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements.  See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership.  The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License.  You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
-->
<script setup lang="ts">
  import { getSvgUrl, scrollToBottom } from '@/utils/tools'
  import { message } from 'ant-design-vue/es/components'
  import { ref, computed, toRefs, watchEffect } from 'vue'
  import ChatMsgItem from './chat-msg-item.vue'
  import useChatBot from './use-chat-bot'
  import { useI18n } from 'vue-i18n'
  import type { Option } from './select-menu.vue'
  import {
    ChatbotConfig,
    ChatThreadHistoryItem,
    SendChatMessageCondition,
    Sender
  } from '@/api/chatbot/types'

  interface PlatfromChatPorps {
    visible: boolean
    chatPayload: ChatbotConfig
    currPage?: Option
  }

  const { t } = useI18n()
  const {
    loading,
    messageReciver,
    fetchSendChatMessage,
    fetchThreadChatHistory
  } = useChatBot()
  const inputText = ref('')
  const isMessageFullyReceived = ref(true)
  const tempHistory = ref<ChatThreadHistoryItem[]>([])
  const msgInputRef = ref<HTMLInputElement | null>(null)
  const props = defineProps<PlatfromChatPorps>()
  const { visible, currPage, chatPayload } = toRefs(props)

  const sendable = computed(
    () => inputText.value != '' && isMessageFullyReceived.value
  )

  const tempMsg = computed<ChatThreadHistoryItem>(() => ({
    sender: 'AI',
    message: messageReciver.value
  }))

  watchEffect(async () => {
    if (currPage.value?.nextPage === 'chat-window' && visible.value) {
      const { platformId, threadId } = chatPayload.value
      const data = await fetchThreadChatHistory(
        platformId as string | number,
        threadId as string | number
      )
      tempHistory.value = data as ChatThreadHistoryItem[]
      loading.value = false
    }
  })

  const onInput = (e: Event) => {
    inputText.value = (e.target as Element)?.textContent || ''
  }

  const reciveMessage = async () => {
    try {
      const sendChatMessageCondition = {
        platformId: chatPayload.value.platformId,
        threadId: chatPayload.value.threadId,
        message: inputText.value
      } as SendChatMessageCondition
      const res = await fetchSendChatMessage(sendChatMessageCondition)
      isMessageFullyReceived.value = res as boolean
      inputText.value = ''
    } catch (error) {
      console.log('recived message:>> ', error)
    } finally {
      isMessageFullyReceived.value = true
      handleScrollToBottom()
    }
  }

  const updateThreadChatHistory = (sender: Sender, message: string) => {
    tempHistory.value.push({
      sender,
      message,
      createTime: new Date().toISOString()
    })
  }

  const clearUpInputContent = () => {
    msgInputRef.value!.innerHTML = ''
  }

  const sendMessage = () => {
    isMessageFullyReceived.value = false
    if (inputText.value === '') {
      message.warning(t('ai.empty_message'))
      return
    }
    updateThreadChatHistory('USER', inputText.value as string)
    handleScrollToBottom()
    clearUpInputContent()
    reciveMessage()
  }

  const handleScrollToBottom = () => {
    scrollToBottom(document.querySelector('.chat-container') as HTMLElement)
  }
</script>

<template>
  <div class="platfrom-chat">
    <section class="chat-container">
      <chat-msg-item
        v-for="(chatItem, index) of tempHistory"
        :key="index"
        :chat-item="chatItem"
      />
      <chat-msg-item
        v-if="loading"
        :chat-item="tempMsg"
        @updated-msg="handleScrollToBottom"
      />
    </section>
    <footer>
      <div class="msg-wrp">
        <div
          ref="msgInputRef"
          class="msg-input"
          data-placeholder="message chat"
          :contenteditable="true"
          @input="onInput"
        ></div>
        <div class="msg-input-suffix">
          <a-button
            :disabled="!sendable"
            type="primary"
            class="msg-input-send"
            @click="sendMessage"
          >
            <img
              :src="getSvgUrl(sendable ? 'send' : 'send-disabled', 'chatbot')"
              alt="send"
            />
          </a-button>
        </div>
      </div>
      <section>
        {{ `${chatPayload?.platformName} ${chatPayload?.model}` }}
      </section>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
  .platfrom-chat {
    @include flexbox($direction: column, $justify: space-between);
    position: relative;
    height: 100%;
    padding: 4px !important;

    section {
      flex: 1 1 0%;
      overflow: auto;
      padding: 0 14px;
    }

    .msg-wrp {
      @include flexbox($align: center);
      border: 1px solid #e5e7eb;
      border-radius: 16px;
      padding: 6px;
      overflow: hidden;
      .ant-input {
        border-color: transparent;
        &:focus {
          border-color: transparent;
          box-shadow: none;
        }
        &:hover {
          border-color: transparent;
        }
      }

      .msg-input {
        width: 100%;
        max-height: 100px;
        padding: 2px 8px;
        white-space: normal;
        transition: all 0.2s;
        overflow: auto;
        font-weight: 500;
        &::before {
          content: attr(data-placeholder);
          color: #a9a9a9;
          display: block;
          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
        }
        &:not(:empty)::before {
          content: '';
        }
        &:focus-visible {
          outline: 0;
          border-color: none;
        }
        &-suffix {
          align-self: end;
          cursor: pointer;
        }
        &-send {
          width: 50px;
          height: 34px;
          @include flexbox($justify: flex-end, $align: center);
          border-radius: 12px;
        }
      }
    }

    footer {
      width: 100%;
      section {
        text-align: center;
        font-size: 12px;
        padding: 2px;
        color: #a9a9a9;
      }
    }
  }
</style>
