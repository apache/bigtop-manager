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
  import { computed, ref, watchEffect } from 'vue'
  import { AuthorizedPlatform } from '@/api/llm-config/types'
  import { useLlmConfig } from '@/composables/llm-config/use-llm-config'

  enum Mode {
    EDIT = 'llmConfig.edit_authorization',
    ADD = 'llmConfig.add_authorization'
  }

  interface Emits {
    (event: 'onOk'): void
    (event: 'onTest'): void
  }

  interface Payload {
    mode: keyof typeof Mode
    metaData?: AuthorizedPlatform
  }

  const emits = defineEmits<Emits>()

  const open = ref(false)
  const title = ref(Mode.ADD)
  const mode = ref<keyof typeof Mode>('ADD')
  const autoFormRef = ref<Comp.AutoFormInstance | null>(null)
  const {
    loading,
    loadingTest,
    currPlatForm,
    getFormDisabled,
    getFormItems,
    getPlatforms,
    addAuthorizedPlatform,
    testAuthorizedPlatform,
    resetState
  } = useLlmConfig(autoFormRef)

  const getDisabledItems = computed(() =>
    mode.value === 'EDIT' ? ['platformId', 'model'] : []
  )

  watchEffect(() => {
    !open.value && resetState()
  })

  const handleOpen = async (payload: Payload) => {
    open.value = true
    title.value = Mode[payload.mode]
    mode.value = payload.mode
    payload.metaData && (currPlatForm.value = payload.metaData)
    getPlatforms()
  }

  const handleOk = async () => {
    try {
      const validate = await autoFormRef.value?.getFormValidation()
      if (!validate) return
      await addAuthorizedPlatform()
      handleCancel()
      emits('onOk')
    } catch (error) {
      console.log('error :>> ', error)
    }
  }

  const handleTest = async () => {
    await testAuthorizedPlatform()
    emits('onTest')
  }

  const handleCancel = () => {
    title.value = Mode.ADD
    currPlatForm.value = {}
    autoFormRef.value?.resetForm()
    open.value = false
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
      :title="title && $t(title)"
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
