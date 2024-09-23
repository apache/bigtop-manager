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
  import { toRefs, computed, toRaw } from 'vue'
  import { useI18n } from 'vue-i18n'
  import type { SelectData, Option } from './select-menu.vue'
  import type { ChatbotConfig } from '@/api/chatbot/types'

  interface PreChatPorps {
    visible: boolean
    chatPayload: ChatbotConfig
    currPage?: Option
  }

  const { t } = useI18n()
  const props = defineProps<PreChatPorps>()
  const { chatPayload } = toRefs(props)
  const emits = defineEmits(['update:currPage', 'update:chatPayload'])

  const platformModel = computed<SelectData[]>(() => [
    {
      title: t('ai.select_model'),
      options: (chatPayload.value?.supportModels as string)
        .split(',')
        .map((v) => ({ name: v, nextPage: 'thread-selector' })) as Option[]
    }
  ])

  const onSelect = async (option: Option) => {
    if (option.nextPage === 'thread-selector') {
      const transformedData = {
        ...toRaw(chatPayload.value),
        model: option.name
      }
      emits('update:chatPayload', transformedData)
    }
    emits('update:currPage', option)
  }
</script>

<template>
  <div class="pre-chat">
    <select-menu :select-data="platformModel" @select="onSelect" />
  </div>
</template>

<style scoped></style>
