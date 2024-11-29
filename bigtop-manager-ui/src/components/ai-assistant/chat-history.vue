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
  import { ref, toRefs, watch, watchEffect } from 'vue'
  import { useAiChatStore } from '@/store/ai-assistant'
  import { storeToRefs } from 'pinia'
  import type { ThreadId } from '@/api/ai-assistant/types'

  interface Props {
    historyType: 'large' | 'small'
    visible?: boolean
  }

  const aiChatStore = useAiChatStore()
  const { threads, currThread } = storeToRefs(aiChatStore)
  const props = defineProps<Props>()
  const { visible, historyType } = toRefs(props)

  const open = ref(false)
  const title = ref('历史记录')
  const selectKey = ref<ThreadId[]>([])

  watch(currThread, (val) => {
    selectKey.value = [val.threadId || '']
  })

  watchEffect(() => {
    if (open.value || visible.value) {
      selectKey.value = [currThread.value.threadId || '']
      aiChatStore.getThreadsFromAuthPlatform()
    }
  })

  const controlVisible = (visible: boolean) => {
    open.value = visible
  }

  const handleSelect = ({ key }: { key: ThreadId }) => {
    aiChatStore.getThread(key)
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
          <a-menu-item v-for="item in threads" :key="item.threadId" :title="item.name">
            <span>{{ item.name }}</span>
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
            <a-menu-item v-for="item in threads" :key="item.threadId" :title="item.name">
              <span>{{ item.name }}</span>
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
    :deep(.ant-menu) {
      background: initial !important;
      border: none;
      .ant-menu-item {
        border-radius: 0;
      }
      .ant-menu-item-selected {
        background-color: $color-white;
      }
    }
    &-large {
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
