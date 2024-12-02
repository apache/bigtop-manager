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
  import { computed, h, onActivated, ref, toRaw, toRefs, watch } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { Modal } from 'ant-design-vue/es/components'
  import { ExclamationCircleFilled } from '@ant-design/icons-vue/lib/icons'
  import type { SelectData, Option } from './select-menu.vue'
  import type { AuthorizedPlatform, ChatbotConfig } from '@/api/chatbot/types'

  interface PlatformAuthSelectorProps {
    visible: boolean
    chatPayload: ChatbotConfig
    currPage?: Option
  }
  const { t } = useI18n()
  const { loading, fetchAuthorizedPlatforms, fetchDelAuthorizedPlatform } = useChatBot()
  const props = defineProps<PlatformAuthSelectorProps>()
  const { visible, currPage, chatPayload } = toRefs(props)
  const authorizedPlatforms = ref<AuthorizedPlatform[]>([])
  const emits = defineEmits(['update:currPage', 'update:chatPayload'])

  const formattedOptions = computed<Option[]>(() =>
    authorizedPlatforms.value.map((v: AuthorizedPlatform) => ({
      ...v,
      id: v.id,
      name: v.platformName,
      nextPage: 'model-selector'
    }))
  )

  watch(visible, async (val) => {
    if (val && currPage.value?.nextPage === 'platform-auth-selector') {
      getAllAuthorizedPlatforms()
    }
  })

  const getAllAuthorizedPlatforms = async () => {
    const data = await fetchAuthorizedPlatforms()
    authorizedPlatforms.value = data as AuthorizedPlatform[]
    loading.value = false
  }

  const platformAuthSelectors = computed<SelectData[]>(() => [
    {
      title: t('ai.select_authorized_platform'),
      emptyOptionsText: t('ai.no_authorized_platform'),
      isDeletable: true,
      options: formattedOptions.value
    },
    {
      title: t('ai.or_you_can'),
      isDeletable: false,
      options: [
        {
          nextPage: 'platform-selection',
          name: t('ai.authorize_new_platform')
        }
      ]
    }
  ])

  const onSelect = (option: Option) => {
    if (option.nextPage === 'model-selector') {
      const transformedData = {
        ...toRaw(chatPayload.value),
        authId: option.id,
        platformName: option.name,
        supportModels: option.supportModels
      }
      emits('update:chatPayload', transformedData)
    }
    emits('update:currPage', option)
  }

  const onRemove = (option: Option) => {
    Modal.confirm({
      title: t('common.delete_confirm_title'),
      icon: h(ExclamationCircleFilled),
      content: t('common.delete_confirm_content', [option.name]),
      async onOk() {
        const { id: authId } = option
        await fetchDelAuthorizedPlatform(authId as string | number)
        getAllAuthorizedPlatforms()
        loading.value = false
      }
    })
  }

  onActivated(() => {
    getAllAuthorizedPlatforms()
  })
</script>

<template>
  <div>
    <a-spin :spinning="loading">
      <select-menu :select-data="platformAuthSelectors" @select="onSelect" @remove="onRemove"></select-menu>
    </a-spin>
  </div>
</template>

<style lang="scss" scoped></style>
