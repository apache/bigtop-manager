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
  import { DrawerProps } from 'ant-design-vue'
  import { useAiChatStore } from '@/store/ai-assistant'
  import { useLlmConfigStore } from '@/store/llm-config'
  import { useMenuStore } from '@/store/menu'

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

  const { t } = useI18n()
  const aiChatStore = useAiChatStore()
  const llmConfigStore = useLlmConfigStore()
  const menuStore = useMenuStore()

  const { currThread, chatRecords, threads, messageReceiver, hasActivePlatform, loadingChatRecords } =
    storeToRefs(aiChatStore)
  const { currAuthPlatform } = storeToRefs(llmConfigStore)

  const open = ref(false)
  const title = ref('aiAssistant.ai_assistant')
  const fullScreen = ref(false)
  const actionHandlers = ref<Map<ActionType, (...args: any[]) => void>>(new Map())
  const smallChatHistoryRef = ref<InstanceType<typeof ChatHistory> | null>(null)

  const width = computed(() => (fullScreen.value ? 'calc(100% - 300px)' : 450))
  const noChat = computed(() => Object.keys(currThread.value || {}).length === 0 || chatRecords.value.length === 0)
  const historyVisible = computed(() => fullScreen.value && open.value)
  const historyType = computed(() => (historyVisible.value ? 'large' : 'small'))
  const recordReceiver = computed((): ChatMessageItem => ({ sender: 'AI', message: messageReceiver.value }))
  const checkActions = computed(() => {
    if (!hasActivePlatform.value) {
      return filterActions(['CLOSE'])
    }
    return fullScreen.value
      ? filterActions(['EXITSCREEN', 'CLOSE'])
      : filterActions(['ADD', 'RECORDS', 'FULLSCREEN', 'CLOSE'])
  })
  const addLimit = computed(() => threads.value.length >= 10)
  const addState = computed(() => threads.value.length >= 10 || loadingChatRecords.value || !hasActivePlatform.value)
  const actionGroup = computed((): GroupItem<ActionType>[] => [
    {
      tip: 'new_chat',
      icon: 'plus',
      action: 'ADD',
      clickEvent: () => aiChatStore.createChatThread(),
      disabled: addState.value
    },
    {
      tip: 'history',
      icon: 'history',
      action: 'RECORDS',
      clickEvent: openRecord
    },
    {
      tip: 'full_screen',
      icon: 'full-screen',
      action: 'FULLSCREEN',
      clickEvent: toggleFullScreen
    },
    {
      tip: 'exit_screen',
      icon: 'exit-screen',
      action: 'EXITSCREEN',
      clickEvent: toggleFullScreen
    },
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
    fullScreen.value = false
  }

  const openRecord = () => {
    smallChatHistoryRef.value?.controlVisible(true)
  }

  const toggleFullScreen = () => {
    fullScreen.value = !fullScreen.value
  }

  const initConfig = async () => {
    actionGroup.value.forEach(({ action, clickEvent }) => {
      action && actionHandlers.value.set(action, clickEvent || (() => {}))
    })
    !hasActivePlatform.value && (await llmConfigStore.getAuthorizedPlatforms(false))
    aiChatStore.initCurrThread()
  }

  const filterActions = (showActions?: ActionType[]) => {
    if (!showActions) return actionGroup.value
    return actionGroup.value.filter((groupItem) => groupItem.action && showActions.includes(groupItem.action))
  }

  const onActions = (item: GroupItem<ActionType>) => {
    const handler = item.action && actionHandlers.value.get(item.action)
    handler ? handler() : console.warn(`Unknown action: ${item.action}`)
  }

  const goSetUpLlmConfig = () => {
    controlVisible(false)
    menuStore.onHeaderClick('/system-manage')
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
      :title="t(title)"
      :width="width"
      :mask="false"
      :closable="false"
      placement="right"
      v-bind="styleConfig"
    >
      <template #extra>
        <button-group i18n="aiAssistant" :groups="checkActions" @on-click="onActions">
          <template #icon="{ item }">
            <template v-if="item.icon">
              <svg-icon v-if="item.action === 'ADD'" :name="item.icon" color="#33333340" :highlight="!addLimit" />
              <svg-icon v-else :name="item.icon" />
            </template>
          </template>
        </button-group>
      </template>
      <chat-history ref="smallChatHistoryRef" :history-type="historyType" />
      <div class="chat">
        <a-spin :spinning="loadingChatRecords">
          <empty-content v-if="noChat" @go-set-up="goSetUpLlmConfig" />
          <template v-else>
            <div v-if="open" v-auto-scroll class="chat-content">
              <chat-message v-for="(record, idx) in chatRecords" :key="idx" :record="record" />
              <chat-message v-if="messageReceiver" :record="recordReceiver" />
            </div>
          </template>
        </a-spin>
      </div>
      <template v-if="hasActivePlatform" #footer>
        <chat-input />
        <a-typography-text type="secondary" class="ai-assistant-desc">
          {{ currAuthPlatform?.platformName }} {{ currAuthPlatform?.model }}
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
