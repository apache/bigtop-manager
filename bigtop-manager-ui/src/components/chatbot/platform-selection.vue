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
  import { computed, watch, ref, toRefs, toRaw } from 'vue'
  import { useI18n } from 'vue-i18n'
  import type { SelectData, Option } from './select-menu.vue'
  import type { ChatbotConfig, SupportedPlatform } from '@/api/chatbot/types'

  interface PlatformSelection {
    visible: boolean
    chatPayload: ChatbotConfig
    currPage?: Option
  }

  const { t } = useI18n()
  const { loading, fetchSupportedPlatforms } = useChatBot()
  const props = defineProps<PlatformSelection>()
  const { visible, currPage, chatPayload } = toRefs(props)
  const supportedPlatforms = ref<SupportedPlatform[]>([])
  const emits = defineEmits(['update:currPage', 'update:chatPayload'])

  const getSupportedPlatforms = async () => {
    const data = await fetchSupportedPlatforms()
    supportedPlatforms.value = data as SupportedPlatform[]
    loading.value = false
  }

  const formattedOptions = computed<Option[]>(() => {
    return supportedPlatforms.value.map((platform: SupportedPlatform) => ({
      ...platform,
      nextPage: 'platform-auth-form'
    })) as Option[]
  })

  const platformSeletions = computed<SelectData[]>(() => [
    {
      title: t('ai.select_platform_to_authorize'),
      hasDel: false,
      options: formattedOptions.value
    }
  ])

  watch(
    currPage,
    (val) => {
      if (visible.value && val?.nextPage === 'platform-selection') {
        getSupportedPlatforms()
      }
    },
    {
      immediate: true,
      deep: true
    }
  )

  const onSelect = async (option: Option) => {
    const transformedData = {
      ...toRaw(chatPayload.value),
      authId: option.id,
      platformName: option.name,
      supportModels: option.supportModels
    }
    emits('update:chatPayload', transformedData)
    emits('update:currPage', option)
  }
</script>

<template>
  <div class="platform-selection">
    <a-spin :spinning="loading">
      <select-menu :select-data="platformSeletions" @select="onSelect" />
    </a-spin>
  </div>
</template>

<style lang="scss" scoped>
  .platform-selection {
    height: 100%;
    @include flexbox($direction: column, $justify: space-between);
  }
</style>
