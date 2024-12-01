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
  import useChatBot from '@/composables/use-chat-bot'
  import { toRefs, computed, h, ref, watchEffect, toRaw, onActivated } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { Modal } from 'ant-design-vue/es/components'
  import { ExclamationCircleFilled } from '@ant-design/icons-vue/lib/icons'
  import type { SelectData, Option } from './select-menu.vue'
  import type { ChatbotConfig, ChatThread, ChatThreadDelCondition } from '@/api/chatbot/types'

  interface PreChatProps {
    visible: boolean
    chatPayload: ChatbotConfig
    currPage?: Option
  }

  const { loading, fetchChatThreads, fetchCreateChatThread, fetchDelChatThread } = useChatBot()
  const { t } = useI18n()
  const props = defineProps<PreChatProps>()
  const { currPage, visible, chatPayload } = toRefs(props)
  const chatThreads = ref<ChatThread[]>([])
  const emits = defineEmits(['update:currPage', 'update:chatPayload'])

  const formattedOptions = computed<Option[]>(() => {
    return chatThreads.value.map((v) => ({
      ...v,
      id: v.threadId,
      name: t('ai.thread_name', [v.threadId]),
      nextPage: 'chat-window'
    }))
  })

  const chatThreadsSelectData = computed<SelectData[]>(() => {
    const selectData = [
      {
        title: t('ai.select_thread_to_chat'),
        isDeletable: true,
        options: formattedOptions.value
      },
      {
        title: t('ai.or_you_can'),
        isDeletable: false,
        options: [
          {
            nextPage: 'chat-window',
            name: t('ai.create_new_thread')
          }
        ]
      }
    ]
    return chatThreads.value.length === 10 ? [selectData[0]] : selectData
  })

  const getAllChatThreads = async () => {
    const { authId, model } = chatPayload.value
    const data = (await fetchChatThreads(
      authId as string | number,
      model as string
    )) as ChatThread[]
    chatThreads.value = data
  }

  watchEffect(async () => {
    if (currPage.value?.nextPage === 'thread-selector' && visible.value) {
      getAllChatThreads()
    }
  })

  const onSelect = async (option: Option) => {
    if (option.nextPage === 'chat-window' && option.id) {
      emits('update:chatPayload', {
        ...toRaw(chatPayload.value),
        threadId: option.id,
        threadName: option.name
      })
    } else {
      const data = (await fetchCreateChatThread({
        authId: chatPayload.value.authId as string | number,
        model: chatPayload.value.model as string
      })) as ChatThread
      emits('update:chatPayload', {
        ...toRaw(chatPayload.value),
        ...data,
        threadName: `Thread ${data.threadId}`
      })
    }
    emits('update:currPage', option)
  }

  const onRemove = (option: Option) => {
    Modal.confirm({
      title: t('common.delete_confirm_title'),
      icon: h(ExclamationCircleFilled),
      content: t('common.delete_confirm_content', [option.name]),
      async onOk() {
        await fetchDelChatThread({
          threadId: option.id,
          authId: chatPayload.value.authId
        } as ChatThreadDelCondition)
        getAllChatThreads()
      }
    })
  }

  onActivated(() => {
    chatThreads.value = []
  })
</script>

<template>
  <div>
    <a-spin :spinning="loading">
      <select-menu :select-data="chatThreadsSelectData" @remove="onRemove" @select="onSelect" />
    </a-spin>
  </div>
</template>

<style scoped></style>
