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
  import { useLlmConfigStore } from '@/store/llm-config/index'
  import { message } from 'ant-design-vue'

  import type { FormItem } from '@/components/common/form-builder/types'
  import type { AuthorizedPlatform } from '@/api/llm-config/types'

  enum Mode {
    EDIT = 'llmConfig.edit_authorization',
    ADD = 'llmConfig.add_authorization'
  }

  interface Emits {
    (event: 'onOk'): void
  }

  const emits = defineEmits<Emits>()

  const { t } = useI18n()
  const llmConfigStore = useLlmConfigStore()

  const open = ref(false)
  const mode = ref<keyof typeof Mode>('ADD')
  const formRef = ref<Comp.FormBuilderInstance | null>(null)
  const disabledFormKeys = shallowRef(['platformId'])

  const { loading, loadingTest, currPlatform, platforms, isDisabled, formKeys, formCredentials, supportModels } =
    storeToRefs(llmConfigStore)

  const isEdit = computed(() => mode.value === 'EDIT')

  const disabledItems = computed(() => (isEdit.value ? [...disabledFormKeys.value, ...formKeys.value] : []))

  const baseFormItems = computed((): FormItem[] => [
    {
      type: 'input',
      field: 'name',
      label: t('llmConfig.name'),
      required: true
    },
    {
      type: 'select',
      field: 'platformId',
      label: t('llmConfig.platform_name'),
      required: true,
      props: {
        options: platforms.value.map((v) => ({ label: v.name, value: v.id }))
      }
    },
    {
      type: 'select',
      field: 'model',
      label: t('llmConfig.model'),
      required: true,
      props: {
        options: (supportModels.value ?? []).map((v) => ({ label: v, value: v }))
      }
    },
    {
      type: 'textarea',
      field: 'desc',
      required: true,
      label: t('llmConfig.desc')
    }
  ])

  const newFormItems = computed(
    () =>
      formCredentials.value?.map((v) => ({
        type: 'input',
        field: v.name,
        label: v.displayName,
        required: true
      })) ?? []
  )

  const formItems = computed((): FormItem[] =>
    [...baseFormItems.value.slice(0, 2), ...newFormItems.value, ...baseFormItems.value.slice(2)].map((item) => ({
      ...item,
      hidden: disabledItems.value.includes(item.field)
    }))
  )

  watchEffect(() => {
    if (!open.value) {
      llmConfigStore.resetState()
    }
  })

  const handleOpen = async (payload?: AuthorizedPlatform) => {
    open.value = true
    mode.value = payload ? 'EDIT' : 'ADD'
    currPlatform.value = payload ?? currPlatform.value
    await llmConfigStore.getPlatforms()
    if (isEdit.value) {
      await llmConfigStore.getPlatformCredentials()
      llmConfigStore.getAuthPlatformDetail()
    }
  }

  const handleOk = async () => {
    const validate = await formRef.value?.validate()
    if (!validate) return
    const api = isEdit.value ? llmConfigStore.updateAuthPlatform : llmConfigStore.addAuthorizedPlatform
    const success = await api()
    if (success) {
      const text = isEdit.value ? 'common.update_success' : 'common.created'
      message.success(t(text))
      handleCancel()
      emits('onOk')
    }
  }

  const handleTest = async () => {
    const success = await llmConfigStore.testAuthorizedPlatform()
    if (success) {
      message.success(t('common.test_success'))
    }
  }

  const handleCancel = () => {
    formRef.value?.resetForm()
    open.value = false
  }

  const onPlatformChange = async () => {
    if (currPlatform.value.model) {
      currPlatform.value.model = undefined
    }
    await llmConfigStore.getPlatformCredentials()
  }

  defineExpose({
    handleOpen
  })
</script>

<template>
  <div class="update-llm-config">
    <a-modal
      :open="open"
      :width="480"
      :title="Mode[mode] && t(Mode[mode])"
      :mask-closable="false"
      :centered="true"
      :destroy-on-close="true"
      @ok="handleOk"
      @cancel="handleCancel"
    >
      <form-builder
        ref="formRef"
        v-model="currPlatform"
        :form-items="formItems"
        :form-config="{ disabled: isDisabled }"
      >
        <template #platformId="{ item }">
          <a-select
            v-model:value="item[item.field]"
            :options="item.props?.options"
            :placeholder="t('common.select_error', [t('llmConfig.model').toLowerCase()])"
            @change="onPlatformChange"
          ></a-select>
        </template>
      </form-builder>
      <template #footer>
        <footer>
          <a-space size="middle">
            <a-button :disabled="loading" :loading="loadingTest" type="primary" @click="handleTest">
              {{ t('llmConfig.test') }}
            </a-button>
          </a-space>
          <a-space size="middle">
            <a-button :disabled="isDisabled" @click="handleCancel">
              {{ t('common.cancel') }}
            </a-button>
            <a-button :disabled="loadingTest" :loading="loading" type="primary" @click="handleOk">
              {{ t('common.confirm') }}
            </a-button>
          </a-space>
        </footer>
      </template>
    </a-modal>
  </div>
</template>

<style lang="scss" scoped>
  footer {
    width: 100%;
    display: flex;
    justify-content: space-between;
  }
</style>
