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
  import { ref } from 'vue'
  import { useAiChatStore } from '@/store/ai-assistant'

  const aiChatStore = useAiChatStore()
  const message = ref('')

  const sendMessage = async () => {
    aiChatStore.setChatRecordForSender('USER', message.value)
    aiChatStore.collectReciveMessage(message.value)
    message.value = ''
  }
</script>
<template>
  <div class="chat-input">
    <a-textarea
      v-model:value="message"
      :bordered="false"
      :auto-size="{ minRows: 1, maxRows: 6 }"
      placeholder="请输入你的问题"
    />
    <a-button type="text" shape="circle" @click="sendMessage">
      <template #icon>
        <svg-icon name="send" />
      </template>
    </a-button>
  </div>
</template>

<style lang="scss" scoped>
  .chat-input {
    margin: 0 auto;
    width: 100%;
    max-width: 800px;
    display: flex;
    border: 1px solid $color-fill;
    border-radius: $space-sm;
    min-height: 40px;
    align-items: center;
    padding-inline-end: 10px;
  }
</style>
