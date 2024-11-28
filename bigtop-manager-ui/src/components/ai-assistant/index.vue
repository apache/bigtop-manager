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
  import { DrawerProps } from 'ant-design-vue'
  import type { GroupItem } from '../common/button-group/types'

  // import ChatItem from './chat-item.vue'

  enum Action {
    ADD,
    RECORDS,
    FULLSCREEN,
    EXITSCREEN,
    CLOSE
  }

  type ActionType = keyof typeof Action

  const open = ref(false)
  const show = ref(false)
  const title = ref('AI 助理')
  const fullScreen = ref(false)
  const actionHandlers = ref<Map<ActionType, (...args: any[]) => void>>(
    new Map()
  )
  const styleConfig = shallowReactive<DrawerProps>({
    headerStyle: {
      height: '56px'
    },
    bodyStyle: {
      padding: '16px'
    },
    contentWrapperStyle: {
      boxShadow: 'none',
      top: '64px',
      transform: 'translateX(0)'
    },
    footerStyle: {
      border: 'none'
    }
  })

  const historyVisible = computed(() => fullScreen.value && open.value)
  const width = computed(() => (fullScreen.value ? 'calc(100% - 300px)' : 450))
  const actionGroup = computed((): GroupItem<ActionType>[] => [
    {
      tip: '创建对话',
      icon: 'plus_gray',
      action: 'ADD'
    },
    {
      tip: '历史记录',
      icon: 'history',
      action: 'RECORDS',
      clickEvent: openRecord
    },
    {
      tip: '窗口全屏',
      icon: 'full_screen',
      action: 'FULLSCREEN',
      clickEvent: toggleFullScreen
    },
    {
      tip: '退出全屏',
      icon: 'exit_screen',
      action: 'EXITSCREEN',
      clickEvent: toggleFullScreen
    },
    {
      icon: 'close',
      action: 'CLOSE',
      clickEvent: closed
    }
  ])

  const checkActions = computed(() =>
    fullScreen.value
      ? filterActions(['EXITSCREEN', 'CLOSE'])
      : filterActions(['ADD', 'RECORDS', 'FULLSCREEN', 'CLOSE'])
  )

  const filterActions = (showActions?: ActionType[]) => {
    if (!showActions) return actionGroup.value
    return actionGroup.value.filter(
      (action) => action.action && showActions.includes(action.action)
    )
  }

  const openDrawer = () => {
    open.value = true
    initActionHandler()
  }

  const openRecord = () => {
    show.value = true
  }

  const toggleFullScreen = () => {
    fullScreen.value = !fullScreen.value
  }

  const closed = () => {
    open.value = false
  }

  const onActions = (item: GroupItem<ActionType>) => {
    const handler = item.action && actionHandlers.value.get(item.action)
    if (handler) {
      handler()
    } else {
      console.warn(`Unknown action: ${item.action}`)
    }
  }

  const initActionHandler = () => {
    actionGroup.value.forEach((act) => {
      act.action &&
        actionHandlers.value.set(act.action, act.clickEvent || (() => {}))
    })
  }

  defineExpose({
    openDrawer
  })
</script>

<template>
  <div class="ai-assistant">
    <chat-history :visible="historyVisible" history-type="large" />
    <a-drawer
      v-model:open="open"
      :title="title"
      :width="width"
      :mask="false"
      :closable="false"
      placement="right"
      v-bind="styleConfig"
    >
      <template #extra>
        <button-group :groups="checkActions" @on-click="onActions">
          <template #icon="{ item }">
            <svg-icon :name="item.icon" />
          </template>
        </button-group>
      </template>

      <chat-history :visible="show" history-type="small">
        <template #extra>
          <button-group
            :groups="filterActions(['CLOSE'])"
            @on-click="onActions"
          >
            <template #icon="{ item }">
              <svg-icon :name="item.icon" />
            </template>
          </button-group>
        </template>
      </chat-history>

      <div class="chat">
        <empty-content v-if="false" />
        <div class="chat-content">
          <!-- <chat-item /> -->
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
  .ai-assistant-desc {
    display: block;
    text-align: center;
    font-size: 12px;
    margin: $space-sm 0;
  }

  .chat {
    height: 100%;

    .chat-content {
      width: 100%;
      max-width: 800px;
      margin: 0 auto;
    }
  }
</style>
