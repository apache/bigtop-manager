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
  import SelectMenu from './select-menu.vue'
  import useChatbotStore from '@/store/chatbot/index'
  import { storeToRefs } from 'pinia'
  import { toRefs, computed, h } from 'vue'
  import type { SelectData, Option } from './select-menu.vue'
  import type {
    Platform,
    ChatThreadCondition,
    ChatThread
  } from '@/api/chatbot/types'
  import { useI18n } from 'vue-i18n'
  import { Modal } from 'ant-design-vue/es/components'
  import { ExclamationCircleFilled } from '@ant-design/icons-vue/lib/icons'

  interface PreChatPorps {
    currPage?: Option
  }

  const { t } = useI18n()
  const chatbot = useChatbotStore()
  const props = defineProps<PreChatPorps>()
  const emits = defineEmits(['update:currPage'])
  const { currPage } = toRefs(props)
  const { currPlatform, chatThreads } = storeToRefs(chatbot)

  const PLATFORM_MODEL = computed<SelectData[]>(() => [
    {
      subTitle: t('ai.select_model'),
      options: currPlatform.value?.supportModels
        .split(',')
        .map((v) => ({ name: v, action: '' }))
    }
  ])

  const formattedOptions = computed<Option[]>(() => {
    return chatThreads.value.map((v) => ({
      ...v,
      id: v.threadId,
      name: t('ai.thread_name', [v.threadId]),
      action: 'SELSECT_THREAD_TO_CHAT'
    }))
  })

  const ChAT_THREAD_MANAGEMENT = computed<SelectData[]>(() => [
    {
      subTitle: t('ai.select_thread_to_chat'),
      hasDel: true,
      options: formattedOptions.value
    },
    {
      subTitle: t('ai.or_you_can'),
      hasDel: false,
      options: [
        {
          action: 'CREATE_THREAD_TO_CHAT',
          name: t('ai.create_new_thread')
        }
      ]
    }
  ])

  const onSelect = async (type: string, option: Option) => {
    if (type == 'ChAT_THREAD_MANAGEMENT') {
      const newPlatFrom = {
        ...currPlatform.value,
        currModel: option.name
      } as Platform
      chatbot.updateCurrPlatform(newPlatFrom)
      emits('update:currPage', { ...currPage.value, action: type })
    }

    if (type == 'PLATFORM_CHAT') {
      if (option.action === 'SELSECT_THREAD_TO_CHAT') {
        const { id: threadId, createTime, updateTime } = option
        chatbot.updateCurrThread({
          threadName: option.name,
          threadId,
          createTime,
          updateTime
        } as ChatThread)
        emits('update:currPage', { ...currPage.value, action: type })
      }
      if (option.action === 'CREATE_THREAD_TO_CHAT') {
        const platformInfo = {
          platformId: currPlatform.value?.platformId,
          model: currPlatform.value?.currModel
        } as ChatThreadCondition
        await chatbot.fetchCreateChatThread(platformInfo)
        emits('update:currPage', { ...currPage.value, action: type })
      }
    }
  }

  const onRemove = (option: Option) => {
    Modal.confirm({
      title: t('common.delete_confirm_title'),
      icon: h(ExclamationCircleFilled),
      content: t('common.delete_confirm_content', [option.name]),
      async onOk() {
        const { threadId, platformId } = option
        await chatbot.fetchDelChatThread({ threadId, platformId })
        chatbot.fetchChatThreads()
      }
    })
  }
</script>

<template>
  <div class="pre-chat">
    <template v-if="currPage?.action === 'PLATFORM_MODEL'">
      <select-menu
        :select-data="PLATFORM_MODEL"
        @select="onSelect('ChAT_THREAD_MANAGEMENT', $event)"
      />
    </template>
    <template v-if="currPage?.action === 'ChAT_THREAD_MANAGEMENT'">
      <select-menu
        :select-data="
          chatThreads.length < 10
            ? ChAT_THREAD_MANAGEMENT
            : ChAT_THREAD_MANAGEMENT.splice(0, 1)
        "
        @remove="onRemove"
        @select="onSelect('PLATFORM_CHAT', $event)"
      />
    </template>
  </div>
</template>

<style scoped></style>
