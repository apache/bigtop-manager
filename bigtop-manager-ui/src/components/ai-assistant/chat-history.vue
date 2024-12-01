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
  import { ref, shallowRef, toRefs, watch } from 'vue'
  import { useAiChatStore } from '@/store/ai-assistant'
  import { storeToRefs } from 'pinia'
  import { formatTime } from '@/utils/tools'
  import { EllipsisOutlined } from '@ant-design/icons-vue'
  import type { ChatThread, ThreadId } from '@/api/ai-assistant/types'
  import type { MenuInfo } from 'ant-design-vue/es/menu/src/interface'

  interface Props {
    historyType: 'large' | 'small'
    visible?: boolean
  }

  interface ThreadOperation {
    key: 'delete' | 'rename'
    danger?: boolean
  }

  const aiChatStore = useAiChatStore()
  const { threads, currThread } = storeToRefs(aiChatStore)
  const props = defineProps<Props>()
  const { visible, historyType } = toRefs(props)

  const open = ref(false)
  const title = ref('历史记录')
  const selectKey = ref<ThreadId[]>([])
  const threadOperations = shallowRef<ThreadOperation[]>([
    { key: 'rename' },
    { key: 'delete', danger: true }
  ])

  watch(currThread, (val) => {
    selectKey.value = [val.threadId || '']
  })

  watch(
    () => [open.value, visible.value],
    async ([open, visible]) => {
      if (open || visible) {
        await aiChatStore.getThreadsFromAuthPlatform()
        if (Object.keys(currThread.value).length > 0) {
          selectKey.value = [currThread.value.threadId || '']
        } else {
          currThread.value = threads.value[0]
        }
      }
    },
    {
      immediate: true
    }
  )

  const controlVisible = (visible: boolean) => {
    open.value = visible
  }

  const handleSelect = ({ key }: { key: ThreadId }) => {
    aiChatStore.getThread(key)
  }

  const handleThreadActions = (thread: ChatThread, { key }: MenuInfo) => {
    console.log('thread :>> ', thread)
    console.log('key :>> ', key)
  }

  defineExpose({
    controlVisible
  })
</script>

<template>
  <div class="chat-history">
    <a-drawer
      v-if="historyType === 'small'"
      v-model:open="open"
      :title="title"
      :closable="false"
      :mask="false"
      :get-container="false"
      :style="{ position: 'absolute' }"
      :body-style="{
        padding: '16px'
      }"
      placement="top"
    >
      <template #extra>
        <a-button type="text" shape="circle" @click="controlVisible(false)">
          <template #icon>
            <svg-icon name="close" />
          </template>
        </a-button>
      </template>
      <main>
        <a-menu v-model:selected-keys="selectKey" @select="handleSelect">
          <a-menu-item v-for="item in threads" :key="item.threadId">
            <div class="chat-history-item">
              <span :title="item.name">{{ item.name }}</span>
              <span>{{ formatTime(item.createTime) }}</span>
            </div>
          </a-menu-item>
        </a-menu>
      </main>
    </a-drawer>
    <template v-else>
      <div v-if="visible" class="chat-history-large">
        <header>
          <a-typography-title :level="5">{{ title }}</a-typography-title>
        </header>
        <main>
          <a-menu v-model:selected-keys="selectKey" @select="handleSelect">
            <a-menu-item v-for="thread in threads" :key="thread.threadId" :title="thread.name">
              <div class="chat-history-item">
                <span>{{ thread.name }}</span>
                <a-dropdown :trigger="['click']">
                  <a @click.prevent>
                    <a-button type="text" shape="circle">
                      <template #icon>
                        <EllipsisOutlined />
                      </template>
                    </a-button>
                  </a>
                  <template #overlay>
                    <a-menu @click="handleThreadActions(thread, $event)">
                      <a-menu-item
                        v-for="action in threadOperations"
                        :key="action.key"
                        :danger="action.danger"
                      >
                        <span>{{ $t(`common.${action.key}`) }}</span>
                      </a-menu-item>
                    </a-menu>
                  </template>
                </a-dropdown>
              </div>
            </a-menu-item>
          </a-menu>
        </main>
        <footer>
          <a-button type="primary" @click="aiChatStore.createChatThread"> 新建会话 </a-button>
        </footer>
      </div>
    </template>
  </div>
</template>

<style lang="scss" scoped>
  .chat-history {
    &-item {
      display: flex;
      justify-content: space-between;
      span:last-child {
        color: $color-text;
      }
    }
    :deep(.ant-menu) {
      background: initial !important;
      border: none;
      .ant-menu-item {
        border-radius: 0;
      }
    }
    &-large {
      :deep(.ant-menu-item-selected) {
        background-color: $color-white;
      }
      position: fixed;
      left: 0;
      top: 64px;
      height: 100%;
      z-index: 10;
      width: 300px;
      background-color: #f7f9fc;

      display: grid;
      grid-template-rows: repeat(1, 56px 1fr 158px);

      header {
        padding: $space-md;
      }

      main {
        padding-inline: $space-md;
        overflow: auto;
      }

      footer {
        padding-inline: $space-md;
        text-align: center;
        button {
          height: 40px;
          width: 100%;
        }
      }
    }
  }
</style>
