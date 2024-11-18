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
  import { computed, ref, toRaw, toRefs, watchEffect } from 'vue'
  import { useI18n } from 'vue-i18n'
  import type { SelectData, Option } from './select-menu.vue'
  import type { FormInstance } from 'ant-design-vue'
  import type { ChatbotConfig, CredentialFormItem } from '@/api/chatbot/types'

  interface PlatformAuthFormProps {
    visible: boolean
    chatPayload: ChatbotConfig
    currPage?: Option
  }
  type FormState = { [key: string]: string }

  const { t } = useI18n()
  const {
    loading,
    checkLoading,
    testAuthPlatform,
    fetchCredentialFormModelOfPlatform
  } = useChatBot()
  const props = defineProps<PlatformAuthFormProps>()
  const { currPage, visible, chatPayload } = toRefs(props)
  const formRef = ref<FormInstance>()
  const formState = ref<FormState>({})
  const credentialFormModel = ref<CredentialFormItem[]>([])
  const emits = defineEmits(['update:currPage', 'update:chatPayload'])

  const platformAuthForm = computed<SelectData[]>(() => [
    {
      title: t('ai.authorizing_platform', [currPage.value?.name]),
      options: []
    }
  ])

  watchEffect(() => {
    formState.value = credentialFormModel.value.reduce((acc, cur) => {
      acc[cur.name] = ''
      return acc
    }, {} as FormState)
  })

  watchEffect(async () => {
    if (currPage.value?.nextPage === 'platform-auth-form' && visible.value) {
      const { authId } = chatPayload.value
      const data = await fetchCredentialFormModelOfPlatform(
        authId as string | number
      )
      credentialFormModel.value = data as CredentialFormItem[]
      loading.value = false
    }
  })

  const onSuccess = async () => {
    const { authId } = chatPayload.value
    const data = await testAuthPlatform(
      authId as string | number,
      toRaw(formState.value)
    )
    if (data) {
      const { id: authId, platformName, supportModels } = data
      emits('update:currPage', {
        ...currPage.value,
        nextPage: 'model-selector'
      })
      emits('update:chatPayload', {
        ...toRaw(chatPayload.value),
        ...{
          authId,
          platformName,
          supportModels
        }
      })
    }
    checkLoading.value = false
  }

  const onCheck = async () => {
    if (!formRef.value) {
      return
    }
    formRef.value
      .validate()
      .then(onSuccess)
      .catch((error) => {
        console.log('error', error)
      })
  }
</script>

<template>
  <div class="platform-auth-form">
    <a-spin :spinning="loading">
      <select-menu :select-data="platformAuthForm">
        <template #select-custom-content>
          <a-form ref="formRef" :model="formState" :colon="false">
            <a-form-item
              v-for="item in credentialFormModel"
              :key="item.name"
              :label="item.displayName"
              :name="item.name"
              :rules="[
                {
                  required: true,
                  message: `Please input ${item.displayName}!`
                }
              ]"
            >
              <a-input
                v-model:value="formState[`${item.name}`]"
                :placeholder="`please input ${item.displayName}`"
              />
            </a-form-item>
          </a-form>
        </template>
      </select-menu>
    </a-spin>
    <footer>
      <a-button :loading="checkLoading" type="primary" @click="onCheck">{{
        loading ? $t('common.loading_text_verifying') : $t('common.confirm')
      }}</a-button>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
  .platform-auth-form {
    height: 100%;
    @include flexbox($direction: column, $justify: space-between);
  }
  footer {
    width: 100%;
    @include flexbox($justify: flex-end, $align: center);
    padding-bottom: 20px;
  }
</style>
