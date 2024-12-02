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
  import { computed, ref, watch } from 'vue'
  import { DrawerProps } from 'ant-design-vue'
  import { useAiChatStore } from '@/store/ai-assistant'
  import { useLlmConfigStore } from '@/store/llm-config'
  import { storeToRefs } from 'pinia'
  import EmptyContent from './empty-content.vue'
  import ChatInput from './chat-input.vue'
  import ChatHistory from './chat-history.vue'
  import ChatMessage from './chat-message.vue'
  import type { ChatMessageItem } from '@/api/ai-assistant/types'
  import type { GroupItem } from '@/components/common/button-group/types'

  enum Action {
    ADD,
    RECORDS,
    FULLSCREEN,
    EXITSCREEN,
    CLOSE
  }

  type ActionType = keyof typeof Action

  const aiChatStore = useAiChatStore()
  const llmConfigStore = useLlmConfigStore()
  const { currThread, chatRecords, threads, messageReceiver, loadingChatRecords } = storeToRefs(aiChatStore)
  const { currAuthPlatform: currPlatform } = storeToRefs(llmConfigStore)

  const open = ref(false)
  const title = ref('aiAssistant.ai_assistant')
  const fullScreen = ref(false)
  const actionHandlers = ref<Map<ActionType, (...args: any[]) => void>>(new Map())
  const samllChatHistoryRef = ref<InstanceType<typeof ChatHistory> | null>(null)

  const width = computed(() => (fullScreen.value ? 'calc(100% - 300px)' : 450))
  const noChat = computed(() => Object.keys(currThread.value).length === 0 || chatRecords.value.length === 0)
  const historyVisible = computed(() => fullScreen.value && open.value)
  const historyType = computed(() => (historyVisible.value ? 'large' : 'small'))
  const authPlatformCached = computed(() => Object.keys(currPlatform.value || {}).length > 0)
  const recordReceiver = computed((): ChatMessageItem => ({ sender: 'AI', message: messageReceiver.value }))
  const checkActions = computed(() =>
    fullScreen.value ? filterActions(['EXITSCREEN', 'CLOSE']) : filterActions(['ADD', 'RECORDS', 'FULLSCREEN', 'CLOSE'])
  )
  const addIcon = computed(() => (chatRecords.value.length === 0 && threads.value.length < 10 ? 'plus' : 'plus_gray'))
  const actionGroup = computed((): GroupItem<ActionType>[] => [
    {
      tip: 'new_chat',
      icon: addIcon.value,
      action: 'ADD',
      clickEvent: aiChatStore.createChatThread,
      disabled: threads.value.length >= 10
    },
    { tip: 'history', icon: 'history', action: 'RECORDS', clickEvent: openRecord },
    { tip: 'full_screen', icon: 'full_screen', action: 'FULLSCREEN', clickEvent: toggleFullScreen },
    { tip: 'exit_screen', icon: 'exit_screen', action: 'EXITSCREEN', clickEvent: toggleFullScreen },
    { icon: 'close', action: 'CLOSE', clickEvent: () => controlVisible(false) }
  ])

  const styleConfig = computed((): DrawerProps => {
    return {
      headerStyle: { height: '56px' },
      bodyStyle: { padding: 0 },
      contentWrapperStyle: {
        boxShadow: 'none',
        top: '64px',
        transform: historyType.value === 'large' ? 'auto' : 'translateX(-180)'
      },
      footerStyle: { border: 'none' }
    }
  })

  watch(open, (val) => {
    val && initConfig()
  })

  const controlVisible = (visible: boolean = true) => {
    open.value = visible
  }

  const openRecord = () => {
    samllChatHistoryRef.value?.controlVisible(true)
  }

  const toggleFullScreen = () => {
    fullScreen.value = !fullScreen.value
  }

  const initConfig = async () => {
    actionGroup.value.forEach(({ action, clickEvent }) => {
      action && actionHandlers.value.set(action, clickEvent || (() => {}))
    })
    !authPlatformCached.value && (await llmConfigStore.getAuthorizedPlatforms())
    if (Object.keys(currThread.value).length == 0) {
      if (threads.value.length === 0) {
        aiChatStore.createChatThread()
      } else {
        currThread.value = threads.value[0]
        aiChatStore.getThreadRecords()
      }
    } else {
      aiChatStore.getThreadRecords()
    }
  }

  const filterActions = (showActions?: ActionType[]) => {
    if (!showActions) return actionGroup.value
    return actionGroup.value.filter((groupItem) => groupItem.action && showActions.includes(groupItem.action))
  }

  const onActions = (item: GroupItem<ActionType>) => {
    const handler = item.action && actionHandlers.value.get(item.action)
    handler ? handler() : console.warn(`Unknown action: ${item.action}`)
  }

  defineExpose({
    controlVisible
  })
</script>

<template>
  <div class="ai-assistant">
    <chat-history :visible="historyVisible" :history-type="historyType" />
    <a-drawer
      v-model:open="open"
      :title="$t(title)"
      :width="width"
      :mask="false"
      :closable="false"
      placement="right"
      v-bind="styleConfig"
    >
      <template #extra>
        <button-group i18n="aiAssistant" :groups="checkActions" @on-click="onActions">
          <template #icon="{ item }">
            <svg-icon :name="item.icon" />
          </template>
        </button-group>
      </template>
      <chat-history ref="samllChatHistoryRef" :history-type="historyType" />
      <div class="chat">
        <a-spin :spinning="loadingChatRecords">
          <empty-content v-if="noChat" />
          <template v-else>
            <div v-if="open" v-auto-scroll class="chat-content">
              <chat-message v-for="(record, idx) in chatRecords" :key="idx" :record="record" />
              <chat-message v-if="messageReceiver" :record="recordReceiver" />
            </div>
          </template>
        </a-spin>
      </div>
      <template #footer>
        <chat-input />
        <a-typography-text type="secondary" class="ai-assistant-desc">
          {{ currPlatform?.platformName }} {{ currPlatform?.model }}
        </a-typography-text>
      </template>
    </a-drawer>
  </div>
</template>

<style lang="scss" scoped>
  .ai-assistant-desc {
    display: block;
    text-align: center;
    font-size: 12px;
    margin: $space-sm 0;
  }

  .chat {
    height: 100%;
    padding-inline: $space-md;
    overflow: auto;

    :deep(.ant-spin-nested-loading) {
      height: 100% !important;
      .ant-spin-container {
        height: 100% !important;
      }
    }

    .chat-content {
      width: 100%;
      max-width: 800px;
      margin: 0 auto;
    }
  }
</style>
