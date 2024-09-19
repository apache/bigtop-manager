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
  import PlatformSelect from './platform-select.vue'
  import PlatformAuthorize from './platform-authorize.vue'
  import PlatformChat from './platform-chat.vue'
  import PlatformChatThread from './platform-chatthread.vue'
  import useChatbotStore from '@/store/chatbot/index'
  import { getSvgUrl } from '@/utils/tools'
  import { storeToRefs } from 'pinia'
  import { ref, watch, computed, unref } from 'vue'
  import type { Option } from './select-menu.vue'

  const chatbot = useChatbotStore()
  const visible = ref(false)
  const currPage = ref<Option>({
    action: 'SUPPORTED_PLATFORM_SELECT',
    name: ''
  })
  const afterPages = ref<Option[]>([currPage.value])
  const { currThread, isExpand } = storeToRefs(chatbot)
  const style = {
    width: '100%',
    boxSizing: 'border-box',
    padding: '24px'
  }

  watch(
    () => currPage.value,
    (val) => {
      const actions = afterPages.value.map((v) => v.action)
      if (visible.value) {
        if (!actions?.includes(val?.action)) {
          afterPages.value?.push(val)
        }
        if (val?.action === 'PLATFORM_MANAGEMENT') {
          chatbot.fetchSupportedPlatforms()
        } else if (val?.action === 'SUPPORTED_PLATFORM_SELECT') {
          chatbot.fetchAuthorizedPlatforms()
        } else if (val?.action === 'ChAT_THREAD_MANAGEMENT') {
          chatbot.fetchChatThreads()
        }
      }
    },
    {
      immediate: true
    }
  )

  const showBack = computed(
    () =>
      'PLATFORM_CHAT' != currPage.value?.action && afterPages.value.length > 1
  )
  const showChatPageOption = computed(
    () => 'PLATFORM_CHAT' === currPage.value?.action
  )

  const getCompName = computed(() => {
    switch (currPage.value?.action) {
      case 'SUPPORTED_PLATFORM_SELECT':
        return PlatformSelect
      case 'PLATFORM_MANAGEMENT':
      case 'PLATFORM_AUTH':
        return PlatformAuthorize
      case 'PLATFORM_MODEL':
      case 'ChAT_THREAD_MANAGEMENT':
        return PlatformChatThread
      case 'PLATFORM_CHAT':
        return PlatformChat
      default:
        return PlatformSelect
    }
  })

  const visibleWindow = (close = false) => {
    close ? (visible.value = false) : (visible.value = !visible.value)
  }

  const onBack = () => {
    afterPages.value.pop()
    currPage.value = afterPages.value[afterPages.value.length - 1]
  }

  const onHome = () => {
    currPage.value = {
      action: 'SUPPORTED_PLATFORM_SELECT',
      name: ''
    }
    afterPages.value = [currPage.value]
  }

  const onHistory = () => {
    afterPages.value = unref(afterPages).filter(
      (page) => page.action == 'ChAT_THREAD_MANAGEMENT'
    )
    currPage.value = afterPages.value[afterPages.value.length - 1]
  }

  const onFullScreen = () => {
    chatbot.setWindowExpandStatus(!isExpand.value)
  }
</script>

<template>
  <a-float-button type="default" @click="() => visibleWindow()">
    <template #icon>
      <div class="chatbot-icon">
        <img :src="getSvgUrl('robot', 'chatbot')" alt="ai chat" />
      </div>
    </template>
  </a-float-button>

  <teleport to="body">
    <div :class="[isExpand ? 'chatbot-expand' : 'chatbot']">
      <a-spin :spinning="false">
        <a-card
          v-show="visible"
          class="base-model"
          :class="[isExpand ? 'chat-model-expand' : 'chat-model']"
        >
          <template #title>
            <header>
              <div class="header-left">
                <img
                  v-if="showBack"
                  :src="getSvgUrl('left', 'chatbot')"
                  alt="back"
                  @click="onBack"
                />
                <img
                  v-if="showChatPageOption && !isExpand"
                  :src="getSvgUrl('home', 'chatbot')"
                  alt="home"
                  @click="onHome"
                />
              </div>
              <div v-if="showChatPageOption" class="header-middle">
                {{ currThread?.threadName }}
              </div>
              <div class="header-right">
                <template v-if="showChatPageOption">
                  <img
                    :src="getSvgUrl('history', 'chatbot')"
                    alt="history"
                    @click="onHistory"
                  />
                  <img
                    :src="getSvgUrl('full-screen', 'chatbot')"
                    alt="full-screen"
                    @click="onFullScreen"
                  />
                </template>
                <img
                  :src="getSvgUrl('close', 'chatbot')"
                  alt="close"
                  @click="visibleWindow(true)"
                />
              </div>
            </header>
          </template>
          <keep-alive>
            <component
              :is="getCompName"
              v-bind="{ style }"
              v-model:currPage="currPage"
              :visible="visible"
            ></component>
          </keep-alive>
        </a-card>
      </a-spin>
    </div>
  </teleport>
</template>

<style lang="scss" scoped>
  img {
    cursor: pointer;
  }

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
        img {
          margin-left: 10px;
        }
      }
    }
  }
</style>
