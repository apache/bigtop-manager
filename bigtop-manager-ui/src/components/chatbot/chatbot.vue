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
  import { ref, computed, watchEffect, watch } from 'vue'
  import { useSessionStorage } from '@vueuse/core'
  import PlatformAuthSelector from './platform-auth-selector.vue'
  import PlatformSelection from './platform-selection.vue'
  import PlatformAuthForm from './platform-auth-form.vue'
  import ModelSelector from './model-selector.vue'
  import ThreadSelector from './thread-selector.vue'
  import chatWindow from './chat-window.vue'
  import type { Option } from './select-menu.vue'
  import type { ChatbotConfig } from '@/api/chatbot/types'

  const pages: Record<string, any> = {
    'platform-auth-selector': PlatformAuthSelector,
    'platform-selection': PlatformSelection,
    'platform-auth-form': PlatformAuthForm,
    'model-selector': ModelSelector,
    'thread-selector': ThreadSelector,
    'chat-window': chatWindow
  }
  const style: Record<string, string> = {
    width: '100%',
    boxSizing: 'border-box',
    padding: '24px'
  }
  const withoutBack = ['chat-window', 'platform-auth-selector']

  const visible = ref(false)
  const currPage = ref<Option>({ nextPage: 'platform-auth-selector' })
  const afterPages = ref<Option[]>([currPage.value])
  const chatPayload = useSessionStorage<ChatbotConfig>('chatbot-payload', {})
  const isExpand = useSessionStorage('is-expand', false)

  const getCompName = computed(() => pages[currPage.value.nextPage])
  const showChatOps = computed(() => 'chat-window' === currPage.value?.nextPage)
  const showBack = computed(
    () => withoutBack.includes(currPage.value.nextPage) || afterPages.value.length == 1
  )

  const visibleWindow = (close = false) => {
    close ? (visible.value = false) : (visible.value = !visible.value)
  }

  const resetPageStatus = () => {
    currPage.value = { nextPage: 'platform-auth-selector' }
    afterPages.value = []
  }

  const onBack = () => {
    afterPages.value.pop()
    currPage.value = afterPages.value[afterPages.value.length - 1]
  }

  const skipStep = (nextPage: string) => {
    currPage.value = { nextPage }
    afterPages.value = [currPage.value]
  }

  const onFullScreen = () => {
    isExpand.value = !isExpand.value
  }

  watch(visible, (val) => {
    val && chatPayload.value.threadId && skipStep('chat-window')
  })

  watchEffect(() => {
    if (visible.value) {
      const nextPage = afterPages.value.map((v) => v?.nextPage)
      if (!nextPage?.includes(currPage.value.nextPage)) {
        afterPages.value?.push(currPage.value)
      }
      return
    }
    resetPageStatus()
  })
</script>

<template>
  <a-float-button type="default" @click="() => visibleWindow()">
    <template #icon>
      <div class="chatbot-icon">
        <svg-icon name="robot" style="margin: 0" />
      </div>
    </template>
  </a-float-button>

  <teleport to="body">
    <div :class="[isExpand ? 'chatbot-expand' : 'chatbot']">
      <a-card
        v-show="visible"
        class="base-model"
        :class="[isExpand ? 'chat-model-expand' : 'chat-model']"
      >
        <template #title>
          <header>
            <div class="header-left">
              <svg-icon v-if="!showBack" name="left" @click="onBack" />
              <svg-icon v-if="showChatOps && !isExpand" name="home" @click="resetPageStatus" />
            </div>
            <div v-if="showChatOps" class="header-middle">
              {{ chatPayload?.threadName }}
            </div>
            <div class="header-right">
              <template v-if="showChatOps">
                <svg-icon name="history" @click="skipStep('thread-selector')" />
                <svg-icon name="full-screen" @click="onFullScreen" />
              </template>
              <svg-icon name="close" @click="visibleWindow(true)" />
            </div>
          </header>
        </template>
        <keep-alive>
          <component
            :is="getCompName"
            v-bind="{ style }"
            v-model:currPage="currPage"
            v-model:chat-payload="chatPayload"
            :visible="visible"
            :is-expand="isExpand"
          ></component>
        </keep-alive>
      </a-card>
    </div>
  </teleport>
</template>

<style lang="scss" scoped>
  .chatbot {
    position: fixed;
    bottom: 10%;
    right: 0;
    &-icon {
      @include flexbox($justify: center, $align: center);
    }
  }

  .chatbot-expand {
    position: fixed;
  }

  .chat-model-expand {
    position: fixed;
    width: 80%;
    height: 70%;
    min-width: 350px;
    min-height: 500px;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
  }

  .chat-model {
    width: 350px;
    height: 500px;
    position: absolute;
    right: 20px;
    bottom: 0;
  }

  .base-model {
    border-radius: 14px;
    @include flexbox($direction: column);
    box-shadow: rgba(0, 0, 0, 0.2) 0px 20px 30px;

    :deep(.ant-card-head) {
      padding: 0 14px;
      min-height: 40px;
    }

    :deep(.ant-card-body) {
      height: 100%;
      padding: 0;
      @include flexbox($direction: column);
      overflow: auto;
    }

    header {
      box-sizing: border-box;
      display: flex;

      & > div {
        flex: 1;
      }
    }

    .header {
      &-left {
        @include flexbox($align: center);
      }

      &-middle {
        @include flexbox($justify: center, $align: center);
      }

      &-right {
        @include flexbox($justify: flex-end, $align: center);
      }
    }
  }
</style>
