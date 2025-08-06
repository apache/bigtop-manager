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
</script>

<template>
  <div class="chat-item">
    <div class="chat-item-avatar">
      <svg-icon :name="isUser ? 'chat-avatar' : 'chatbot'" />
    </div>
    <article class="chat-item-message">
      <div class="msg-wrp">
        <markdown-view :mark-raw="$props.record.message" />
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
      }
    }
  }
</style>
