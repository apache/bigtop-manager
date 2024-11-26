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
  import EmptyContent from './empty-content.vue'
  import ChatInput from './chat-input.vue'

  const open = ref(false)
  const title = ref('AI 助理')
  const actions = ref(['plus_gray', 'history', 'full_screen', 'close'])

  const afterOpenChange = (bool: boolean) => {
    console.log('open', bool)
  }

  const openDrawer = () => {
    open.value = true
  }

  const onActions = (action: string) => {
    console.log('action :>> ', action)
  }

  defineExpose({
    openDrawer
  })
</script>

<template>
  <a-drawer
    v-model:open="open"
    :title="title"
    :closable="false"
    :mask="false"
    :width="450"
    placement="right"
    :content-wrapper-style="{ top: '64px' }"
    @after-open-change="afterOpenChange"
  >
    <template #extra>
      <a-space>
        <a-button
          v-for="icon in actions"
          :key="icon"
          shape="circle"
          type="text"
          @click="onActions(icon)"
        >
          <svg-icon :name="icon" />
        </a-button>
      </a-space>
    </template>
    <div class="ai-assistant">
      <empty-content />
      <chat-input />
    </div>
  </a-drawer>
</template>

<style lang="scss" scoped>
  .ai-assistant {
    height: 100%;
  }
</style>
