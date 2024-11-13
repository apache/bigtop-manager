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
  import { storeToRefs } from 'pinia'
  import { computed, ref, watchEffect } from 'vue'
  import { AuthorizedPlatform } from '@/api/llm-config/types'
  import { useFormItem } from '@/store/llm-config/config'
  import { useLlmConfigStore } from '@/store/llm-config/index'
  import { addFormItemEvents } from '@/components/common/auto-form/helper'
  import type { FormItemState } from '@/components/common/auto-form/types'

  enum Mode {
    EDIT = 'llmConfig.edit_authorization',
    ADD = 'llmConfig.add_authorization'
  }

  interface Emits {
    (event: 'onOk'): void
    (event: 'onTest'): void
  }

  const emits = defineEmits<Emits>()

  const llmConfigStore = useLlmConfigStore()
  const { formItemConfig, createNewFormItem } = useFormItem()

  const open = ref(false)
  const mode = ref<keyof typeof Mode>('ADD')
  const autoFormRef = ref<Comp.AutoFormInstance | null>(null)
  const {
    loading,
    loadingTest,
    currPlatForm,
    platforms,
    getFormDisabled,
    formCredentials
  } = storeToRefs(llmConfigStore)

  const getDisabledItems = computed(() =>
    mode.value === 'EDIT' ? ['platformId', 'model'] : []
  )
  const getBaseFormItems = computed(() =>
    addFormItemEvents(formItemConfig, 'platformId', {
      change: onPlatformChange
    })
  )
  const getFormItems = computed((): FormItemState[] => {
    const tmpBaseFormItems = [...getBaseFormItems.value]
    const newFormItems = formCredentials.value?.map((v) =>
      createNewFormItem('input', v.name, v.displayName)
    ) as FormItemState[]
    if (newFormItems) {
      newFormItems.length > 0 && tmpBaseFormItems.splice(2, 0, ...newFormItems)
    }
    return tmpBaseFormItems
  })

  watchEffect(() => {
    !open.value && llmConfigStore.resetState()
  })

  const handleOpen = async (payload?: AuthorizedPlatform) => {
    open.value = true
    mode.value = payload ? 'EDIT' : 'ADD'
    payload && (currPlatForm.value = payload)
    await llmConfigStore.getPlatforms()
    autoFormRef?.value?.setOptionsVal('platformId', platforms.value)
  }

  const handleOk = async () => {
    const validate = await autoFormRef.value?.getFormValidation()
    if (!validate) return
    const success = await llmConfigStore.addAuthorizedPlatform()
    if (success) {
      handleCancel()
      emits('onOk')
    }
  }

  const handleTest = async () => {
    const success = await llmConfigStore.testAuthorizedPlatform()
    success && emits('onTest')
  }

  const handleCancel = () => {
    autoFormRef.value?.resetForm()
    open.value = false
  }

  const onPlatformChange = async () => {
    const { platformId, model } = currPlatForm.value
    const item = platforms.value.find((item) => item.id === platformId)
    if (!model) {
      currPlatForm.value.model = ''
    }
    await llmConfigStore.getPlatformCredentials()
    autoFormRef?.value?.setOptionsVal('model', item?.supportModels || [])
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
      :title="Mode[mode] && $t(Mode[mode])"
      :mask-closable="false"
      :destroy-on-close="true"
      @ok="handleOk"
      @cancel="handleCancel"
    >
      <auto-form
        ref="autoFormRef"
        v-model:form-value="currPlatForm"
        :show-button="false"
        :form-items="getFormItems"
        :form-disabled="getFormDisabled"
        :disabled-items="getDisabledItems"
      />
      <template #footer>
        <footer>
          <a-space size="middle">
            <a-button
              :disabled="loading"
              :loading="loadingTest"
              type="primary"
              @click="handleTest"
            >
              {{ $t('llmConfig.availability') }}
            </a-button>
          </a-space>
          <a-space size="middle">
            <a-button :disabled="getFormDisabled" @click="handleCancel">
              {{ $t('common.cancel') }}
            </a-button>
            <a-button
              :disabled="loadingTest"
              :loading="loading"
              type="primary"
              @click="handleOk"
            >
              {{ $t('common.confirm') }}
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
