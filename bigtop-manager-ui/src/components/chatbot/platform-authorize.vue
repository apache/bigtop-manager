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
  import useChatbot from './chatbot'
  import { storeToRefs } from 'pinia'
  import { computed, ref, toRefs, watchEffect, toRaw } from 'vue'
  import type { SelectData, Option } from './select-menu.vue'
  import type { FormInstance } from 'ant-design-vue'
  import { useI18n } from 'vue-i18n'

  interface PlatformAuthorizeProps {
    currPage?: Option
  }
  type FormState = { [key: string]: string }

  const { t } = useI18n()
  const chatbot = useChatbot()
  const formRef = ref<FormInstance>()
  const formState = ref<FormState>({})
  const props = defineProps<PlatformAuthorizeProps>()
  const emits = defineEmits(['update:currPage'])
  const { currPage } = toRefs(props)
  const { loading, supportedPlatForms, credentialFormModel } =
    storeToRefs(chatbot)

  const formattedOptions = computed<Option[]>(() =>
    supportedPlatForms.value.map((v) => ({
      ...v,
      action: 'PLATFORM_AUTH'
    }))
  )

  const PLATFORM_MANAGEMENT = computed<SelectData[]>(() => [
    {
      subTitle: t('ai.select_platform_to_authorize'),
      options: formattedOptions.value
    }
  ])

  const PLATFORM_AUTH = computed<SelectData[]>(() => [
    {
      subTitle: t('ai.authorizing_platform', [currPage.value?.name]),
      options: []
    }
  ])

  watchEffect(() => {
    formState.value = credentialFormModel.value.reduce((acc, cur) => {
      acc[cur.name] = ''
      return acc
    }, {} as FormState)
  })

  const onSelect = async (type: string, option: Option) => {
    const { id, name, supportModels } = option
    formState.value = {}
    chatbot.updateCurrPlatform({
      platformId: id,
      platformName: name,
      supportModels
    })
    await chatbot.fetchCredentialFormModelofPlatform()
    emits('update:currPage', { ...option, action: type })
  }

  const onCheck = async () => {
    if (!formRef.value) {
      return
    }
    formRef.value
      .validate()
      .then(async () => {
        const isPass = await chatbot.testAuthofPlatform(toRaw(formState.value))
        loading.value = false
        if (isPass) {
          emits('update:currPage', {
            ...currPage.value,
            action: 'PLATFORM_MODEL'
          })
        }
      })
      .catch((error) => {
        console.log('error', error)
      })
  }
</script>

<template>
  <div class="platform-authorize">
    <template v-if="currPage?.action == 'PLATFORM_MANAGEMENT'">
      <select-menu
        :select-data="PLATFORM_MANAGEMENT"
        @select="onSelect('PLATFORM_AUTH', $event)"
      />
    </template>

    <template v-if="currPage?.action == 'PLATFORM_AUTH'">
      <select-menu :select-data="PLATFORM_AUTH">
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
    </template>
    <footer v-if="currPage?.action == 'PLATFORM_AUTH'">
      <a-button :loading="loading" type="primary" @click="onCheck">{{
        loading ? $t('common.loadingText_verifying') : $t('common.confirm')
      }}</a-button>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
  .platform-authorize {
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
  }

  footer {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    padding-bottom: 20px;
  }
</style>
