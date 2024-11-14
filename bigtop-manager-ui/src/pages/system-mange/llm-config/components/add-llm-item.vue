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
  import { computed, ref, shallowRef, watchEffect } from 'vue'
  import { useFormItemConfig } from '@/store/llm-config/config'
  import { useLlmConfigStore } from '@/store/llm-config/index'
  import type { FormItemState } from '@/components/common/auto-form/types'
  import type { AuthorizedPlatform } from '@/api/llm-config/types'

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
  const { formItemConfig, createNewFormItem } = useFormItemConfig()

  const open = ref(false)
  const mode = ref<keyof typeof Mode>('ADD')
  const autoFormRef = ref<Comp.AutoFormInstance | null>(null)
  const disableFormKeys = shallowRef(['platformId', 'model'])
  const {
    loading,
    loadingTest,
    currPlatForm,
    formKeys,
    platforms,
    isDisabled,
    formCredentials,
    supportModels
  } = storeToRefs(llmConfigStore)

  const isEdit = computed(() => mode.value === 'EDIT')
  const disabledItems = computed(() =>
    isEdit.value ? [...disableFormKeys.value, ...formKeys.value] : []
  )
  const formItems = computed((): FormItemState[] => {
    const newFormItems =
      formCredentials.value?.map((v) =>
        createNewFormItem('input', v.name, v.displayName)
      ) ?? []
    return [
      ...formItemConfig.value.slice(0, 2),
      ...newFormItems,
      ...formItemConfig.value.slice(2)
    ]
  })

  watchEffect(() => {
    !open.value && llmConfigStore.resetState()
  })

  const bindConfigToFormItems = () => {
    autoFormRef?.value?.setOptions('platformId', platforms.value)
    autoFormRef?.value?.setFormItemEvents('platformId', {
      change: onPlatformChange
    })
  }

  const handleOpen = async (payload?: AuthorizedPlatform) => {
    open.value = true
    mode.value = payload ? 'EDIT' : 'ADD'
    currPlatForm.value = payload ?? currPlatForm.value
    await llmConfigStore.getPlatforms()
    bindConfigToFormItems()
    isEdit.value && llmConfigStore.getAuthPlatformDetail()
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
    if (currPlatForm.value.model) {
      currPlatForm.value.model = undefined
    }
    await llmConfigStore.getPlatformCredentials()
    autoFormRef?.value?.setOptions('model', supportModels.value || [])
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
        :form-items="formItems"
        :form-disabled="isDisabled"
        :disabled-items="disabledItems"
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
            <a-button :disabled="isDisabled" @click="handleCancel">
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
