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
  import type { ChatMessageItem } from '@/api/ai-assistant/types'

  interface Props {
    record: ChatMessageItem
  }

  const props = defineProps<Props>()
  const isUser = computed(() => props.record.sender === 'USER')
  const isTool = computed(() => props.record.messageType === 'tool')
  const activeKey = ref<string[]>([])
  const toolOutput = computed(() => props.record.toolOutput || '-')
  const toolInput = computed(() => props.record.toolInput || '-')
  const toolError = computed(() => props.record.toolError || '-')

  watch(
    () => props.record.executionId,
    () => {
      activeKey.value = []
    },
    { immediate: true }
  )
</script>

<template>
  <div class="chat-item">
    <div class="chat-item-avatar">
      <svg-icon :name="isUser ? 'chat-avatar' : 'chatbot'" />
    </div>
    <article class="chat-item-message">
      <div class="msg-wrp">
        <template v-if="isTool">
          <a-collapse v-model:active-key="activeKey" :bordered="false" :ghost="true" class="tool-collapse">
            <a-collapse-panel :key="props.record.executionId || props.record.toolName || 'tool'">
              <template #header>
                <div class="tool-meta">
                  <span class="tool-name">{{ props.record.toolName }}</span>
                  <a-tag
                    :color="
                      props.record.toolStatus === 'failed'
                        ? 'error'
                        : props.record.toolStatus === 'completed'
                          ? 'success'
                          : 'processing'
                    "
                  >
                    {{ props.record.toolStatus }}
                  </a-tag>
                </div>
              </template>
              <div class="tool-section">
                <span class="tool-section-title">Input</span>
                <markdown-view :mark-raw="toolInput" />
              </div>
              <div v-if="props.record.toolStatus === 'completed'" class="tool-section">
                <span class="tool-section-title">Output</span>
                <markdown-view :mark-raw="toolOutput" />
              </div>
              <div v-if="props.record.toolStatus === 'failed'" class="tool-section">
                <span class="tool-section-title">Error</span>
                <markdown-view :mark-raw="toolError" />
              </div>
            </a-collapse-panel>
          </a-collapse>
        </template>
        <markdown-view v-else :mark-raw="$props.record.message" />
      </div>
    </article>
  </div>
</template>

<style lang="scss" scoped>
  .chat-item {
    gap: $space-md;
    display: flex;
    margin: $space-md 0;
    &-avatar {
      width: 32px;
      height: 32px;
      flex-shrink: 0;
      border: 1px solid #e5e5e5;
      border-radius: 50%;

      display: flex;
      align-items: center;
      justify-content: center;
    }
    &-message {
      flex: 1;
      border-radius: 4px;
      background-color: #f7f9fc;
      .msg-wrp {
        height: auto;
        width: 100%;
        padding: $space-sm $space-md;

        .tool-meta {
          display: flex;
          align-items: center;
          gap: $space-sm;

          .tool-name {
            font-size: 12px;
            font-weight: 600;
            color: #5b6474;
          }
        }

        .tool-collapse {
          :deep(.ant-collapse-header) {
            padding: 0 !important;
          }

          :deep(.ant-collapse-content-box) {
            padding: $space-sm 0 0 0 !important;
          }
        }

        .tool-section {
          margin-top: $space-sm;

          &:first-child {
            margin-top: 0;
          }

          .tool-section-title {
            display: inline-block;
            margin-bottom: 4px;
            font-size: 12px;
            font-weight: 600;
            color: #8c96a6;
            text-transform: uppercase;
          }
        }
      }
    }
  }
</style>
