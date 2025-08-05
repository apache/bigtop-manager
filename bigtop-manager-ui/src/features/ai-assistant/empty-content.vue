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
  import { usePngImage } from '@/utils/tools'
  import { useAiChatStore } from '@/store/ai-assistant'

  interface Emits {
    (event: 'goSetUp'): void
  }

  const emits = defineEmits<Emits>()

  const imageStyle = shallowRef({
    height: '200px',
    marginBottom: '16px'
  })
  const { t } = useI18n()
  const aiChatStore = useAiChatStore()
  const { hasActivePlatform, threads, chatRecords } = storeToRefs(aiChatStore)
  const emptyState = usePngImage('ai-helper')
  const disabledState = usePngImage('ai-disabled')

  const goSetUpLlmConfig = () => {
    emits('goSetUp')
  }

  const quickAsk = (message: string) => {
    if (threads.value.length === 0) {
      chatRecords.value = []
    }
    aiChatStore.setChatRecordForSender('USER', message)
    aiChatStore.collectReceiveMessage(message)
  }
</script>

<template>
  <div class="empty-content">
    <a-empty :image="hasActivePlatform ? emptyState : disabledState" :image-style="imageStyle">
      <template #description>
        <template v-if="hasActivePlatform">
          <div class="say-hello">
            <a-typography-title :level="5">
              <div> {{ t('aiAssistant.greeting') }} </div>
              <div> {{ t('aiAssistant.help') }} </div>
            </a-typography-title>
          </div>
          <div class="feature-desc">
            <a-typography-link underline @click="quickAsk(t('aiAssistant.bigtop_manager'))">
              {{ t('aiAssistant.bigtop_manager') }}
            </a-typography-link>
            <a-typography-link underline @click="quickAsk(t('aiAssistant.can_do_for_you'))">
              {{ t('aiAssistant.can_do_for_you') }}
            </a-typography-link>
            <a-typography-link underline @click="quickAsk(t('aiAssistant.big_data_news'))">
              {{ t('aiAssistant.big_data_news') }}
            </a-typography-link>
          </div>
        </template>
        <template v-else>
          <a-typography-text>
            <div> {{ t('aiAssistant.ai_disabled_msg') }} </div>
          </a-typography-text>
          <a-typography-link underline @click="goSetUpLlmConfig"> {{ t('aiAssistant.go_set_up') }} </a-typography-link>
        </template>
      </template>
    </a-empty>
  </div>
</template>

<style lang="scss" scoped>
  .empty-content {
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .say-hello {
    margin-bottom: 48px;
  }

  .feature-desc {
    display: grid;
    gap: $space-sm;
  }
</style>
