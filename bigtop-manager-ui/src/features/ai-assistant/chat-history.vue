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
  import { useAiChatStore } from '@/store/ai-assistant'
  import { formatTime } from '@/utils/transform'
  import { EllipsisOutlined } from '@ant-design/icons-vue'
  import { message, Modal, Empty } from 'ant-design-vue'

  import SvgIcon from '@/components/base/svg-icon/index.vue'

  import type { ChatThread, ThreadId } from '@/api/ai-assistant/types'

  interface Props {
    historyType: 'large' | 'small'
    visible?: boolean
  }

  interface ThreadOperation {
    key: 'delete' | 'rename'
    danger?: boolean
  }

  type ThreadOperationHandler = Record<'delete' | 'rename', (thread: ChatThread, idx: number) => void>

  const { t } = useI18n()
  const aiChatStore = useAiChatStore()
  const { threads, currThread, threadLimit } = storeToRefs(aiChatStore)
  const props = defineProps<Props>()
  const { visible, historyType } = toRefs(props)

  const open = ref(false)
  const isRename = ref(false)
  const title = ref('aiAssistant.history')
  const newName = ref('aiAssistant.new_chat')
  const selectKey = ref<ThreadId[]>([])
  const editThread = ref<ChatThread>({})
  const threadOperations = shallowRef<ThreadOperation[]>([{ key: 'rename' }, { key: 'delete', danger: true }])
  const threadOperationHandlers = shallowReactive<ThreadOperationHandler>({
    delete: (thread, idx) => handleDeleteConfirm(thread, idx),
    rename: (thread) => {
      editThread.value = thread
      isRename.value = true
      newName.value = thread.name as string
    }
  })

  watch(currThread, (val) => {
    selectKey.value = [val?.threadId || '']
  })

  watch(
    () => [open.value, visible.value],
    async ([open, visible]) => {
      if (open || visible) {
        await aiChatStore.getThreadsFromAuthPlatform()
        if (Object.keys(currThread.value || {}).length <= 0) {
          currThread.value = threads.value[0]
        }
        selectKey.value = [currThread.value?.threadId || '']
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

  const handleRenameConfirm = async (thread: ChatThread) => {
    if (newName.value == '') newName.value = t('aiAssistant.new_chat')
    isRename.value = false
    const checkRes = await aiChatStore.updateChatThread(editThread.value, newName.value)
    if (checkRes) {
      thread.name = newName.value
      editThread.value = {}
      newName.value = ''
    }
  }

  const handleDeleteConfirm = (thread: ChatThread, idx: number) => {
    Modal.confirm({
      title: () =>
        h('div', { style: { display: 'flex' } }, [
          h(SvgIcon, { name: 'unknown', style: { width: '24px', height: '24px' } }),
          h('p', t('common.delete_msg'))
        ]),
      style: { top: '30vh' },
      icon: null,
      async onOk() {
        const success = await aiChatStore.deleteChatThread(thread)
        if (success) {
          message.success(t('common.delete_success'))
          await aiChatStore.getThreadsFromAuthPlatform()
          const threadCounts = threads.value.length
          if (threadCounts === 0) {
            currThread.value = {}
          } else {
            const isEqual = thread.threadId === currThread.value.threadId
            if (idx >= threadCounts) {
              currThread.value = threads.value[0]
            }
            if (isEqual) {
              currThread.value = threads.value[idx]
              aiChatStore.getThread(currThread.value.threadId as ThreadId)
            }
          }
        }
      }
    })
  }

  const handleThreadActions = (thread: ChatThread, idx: number, { key }: any) => {
    threadOperationHandlers[key](thread, idx)
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
      :title="t(title)"
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
        <a-empty v-if="threads.length == 0" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
        <a-menu v-else v-model:selected-keys="selectKey" @select="handleSelect">
          <a-menu-item v-for="thread in threads" :key="thread.threadId" :title="thread.name">
            <div class="chat-history-item">
              <a-typography-link :ellipsis="true" :content="`${thread.name}`" />
              <span>{{ formatTime(thread.createTime) }}</span>
            </div>
          </a-menu-item>
        </a-menu>
      </main>
    </a-drawer>
    <template v-else>
      <div v-if="visible" class="chat-history-large">
        <header>
          <a-typography-title :level="5">{{ t(title) }}</a-typography-title>
        </header>
        <main>
          <a-empty v-if="threads.length == 0" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
          <a-menu v-else v-model:selected-keys="selectKey" @select="handleSelect">
            <a-menu-item v-for="(thread, idx) in threads" :key="thread.threadId" :title="thread.name">
              <div class="chat-history-item">
                <a-input
                  v-if="isRename && editThread.threadId === thread.threadId"
                  :key="thread.threadId"
                  :ref="
                    (el: any) => {
                      isRename && el.focus()
                    }
                  "
                  v-model:value="newName"
                  @blur="handleRenameConfirm(thread)"
                />
                <template v-else>
                  <a-typography-link :ellipsis="true" :content="`${thread.name}`" />
                  <a-dropdown :trigger="['click']">
                    <a-button type="text" shape="circle" @click.stop>
                      <template #icon>
                        <EllipsisOutlined />
                      </template>
                    </a-button>
                    <template #overlay>
                      <a-menu @click="handleThreadActions(thread, idx, $event)">
                        <a-menu-item v-for="action in threadOperations" :key="action.key" :danger="action.danger">
                          <span>{{ t(`common.${action.key}`) }}</span>
                        </a-menu-item>
                      </a-menu>
                    </template>
                  </a-dropdown>
                </template>
              </div>
            </a-menu-item>
          </a-menu>
          <div class="chat-history-limit">
            <a-typography-text size="small" type="secondary" :content="t('aiAssistant.show_only10_records')" />
          </div>
        </main>
        <footer>
          <a-button type="primary" :disabled="threadLimit" @click="aiChatStore.createChatThread(false)">
            {{ t('aiAssistant.new_chat') }}
          </a-button>
        </footer>
      </div>
    </template>
  </div>
</template>

<style lang="scss" scoped>
  .chat-history {
    &-limit {
      text-align: center;
      span {
        font-size: 12px;
      }
    }
    &-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: $space-sm;
      height: 100%;
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
