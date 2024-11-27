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
  import { computed, ref, shallowReactive } from 'vue'
  import EmptyContent from './empty-content.vue'
  import ChatInput from './chat-input.vue'
  import ChatHistory from './chat-history.vue'
  import ChatItem from './chat-item.vue'

  enum Action {
    ADD,
    HSITORY,
    FULLSCREEN,
    EXITSCREEN,
    CLOSE
  }

  type ActionType = keyof typeof Action

  interface ActionItem {
    icon: string
    action: ActionType
    tip?: string
  }

  const open = ref(false)
  const isFullScreen = ref(false)
  const title = ref('AI 助理')
  const commonConfig = shallowReactive({
    headerStyle: {
      height: '56px'
    },
    contentWrapperStyle: {
      boxShadow: 'none',
      top: '64px'
    },
    footerStyle: {
      border: 'none'
    },
    placement: 'right',
    closable: false,
    mask: false
  })

  const historyStyleConfig = shallowReactive({
    width: '300px',
    backgroundColor: '#F7F9FC'
  })

  const historyVisible = computed(() => isFullScreen.value && open.value)
  const chatWindowWidth = computed(() =>
    isFullScreen.value ? `calc(100% - ${historyStyleConfig.width})` : 450
  )

  const actions = computed((): ActionItem[] => [
    {
      tip: '创建对话',
      icon: 'plus_gray',
      action: 'ADD'
    },
    {
      tip: '历史记录',
      icon: 'history',
      action: 'HSITORY'
    },
    {
      tip: isFullScreen.value ? '退出全屏' : '窗口全屏',
      icon: isFullScreen.value ? 'exit_screen' : 'full_screen',
      action: isFullScreen.value ? 'EXITSCREEN' : 'FULLSCREEN'
    },
    {
      icon: 'close',
      action: 'CLOSE'
    }
  ])

  const onActions = (item: ActionItem) => {
    if (['FULLSCREEN', 'EXITSCREEN'].includes(item.action)) {
      isFullScreen.value = !isFullScreen.value
    }
    if (item.action === 'CLOSE') {
      open.value = false
    }
  }

  const afterOpenChange = (bool: boolean) => {
    console.log('open', bool)
  }

  const openDrawer = () => {
    open.value = true
  }

  defineExpose({
    openDrawer
  })
</script>

<template>
  <div class="ai-assistant">
    <chat-history :visible="historyVisible" />
    <a-drawer
      v-model:open="open"
      :title="title"
      :width="chatWindowWidth"
      v-bind="commonConfig"
      @after-open-change="afterOpenChange"
    >
      <template #extra>
        <a-space>
          <a-button
            v-for="action in actions"
            :key="action.icon"
            shape="circle"
            type="text"
            @click="onActions(action)"
          >
            <svg-icon :name="action.icon" />
          </a-button>
        </a-space>
      </template>
      <div class="chat">
        <empty-content v-if="false" />
        <div class="chat-content">
          <chat-item />
        </div>
      </div>
      <template #footer>
        <chat-input />
        <a-typography-text type="secondary" class="ai-assistant-desc">
          DashScope qwen-1.8b-chat
        </a-typography-text>
      </template>
    </a-drawer>
  </div>
</template>

<style lang="scss" scoped>
  .chat {
    height: 100%;

    .chat-content {
      width: 100%;
      max-width: 800px;
      margin: 0 auto;
    }
  }

  .ai-assistant-desc {
    display: block;
    text-align: center;
    font-size: 12px;
    margin: $space-sm 0;
  }
</style>
