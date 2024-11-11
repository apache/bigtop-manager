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
  import { computed, ref } from 'vue'
  import { useI18n } from 'vue-i18n'
  import AutoForm from '@/components/common/auto-form/index.vue'
  import type {
    BaseType,
    FormItemState,
    FormState
  } from '@/components/common/auto-form/types'
  import { AuthorizedPlatform } from '@/api/llm-config/types'
  import { useLlmConfig } from '@/composables/use-llm-config'

  type AutoFromInstance = InstanceType<typeof AutoForm>

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
  const { t } = useI18n()
  const { getPlatforms, platforms } = useLlmConfig()
  const loading = ref(false)
  const loadingTest = ref(false)
  const open = ref(false)
  const title = ref(Mode.ADD)
  const mode = ref<keyof typeof Mode>('ADD')
  const formValue = ref<FormState<AuthorizedPlatform | BaseType>>({})
  const autoFormRef = ref<AutoFromInstance | null>(null)

  const getFormDisabled = computed(() => loading.value || loadingTest.value)
  const getFormItems = computed((): FormItemState[] => [
    {
      type: 'input',
      field: 'name',
      formItemProps: {
        name: 'name',
        label: t('llmConfig.name'),
        rules: [
          {
            required: true,
            message: t('common.enter_error', [
              `${t('llmConfig.name')}`.toLowerCase()
            ]),
            trigger: 'blur'
          }
        ]
      },
      controlProps: {
        placeholder: t('common.enter_error', [
          `${t('llmConfig.name')}`.toLowerCase()
        ])
      }
    },
    {
      type: 'select',
      field: 'platformId',
      defaultValue: '',
      fieldMap: {
        label: 'name',
        value: 'id'
      },
      formItemProps: {
        name: 'platformId',
        label: t('llmConfig.platform_name'),
        rules: [
          {
            required: true,
            message: t('common.enter_error', [
              t('llmConfig.platform_name').toLowerCase()
            ]),
            trigger: 'blur'
          }
        ]
      },
      controlProps: {
        placeholder: t('common.enter_error', [
          t('llmConfig.platform_name').toLowerCase()
        ])
      },
      on: {
        change: onPlatformChange
      }
    },
    {
      type: 'input',
      field: 'apiKey',
      formItemProps: {
        name: 'apiKey',
        label: 'API Key',
        rules: [
          {
            required: true,
            message: t('common.enter_error', ['API Key']),
            trigger: 'blur'
          }
        ]
      },
      controlProps: {
        placeholder: t('common.enter_error', ['API Key'])
      }
    },
    {
      type: 'select',
      field: 'model',
      formItemProps: {
        name: 'model',
        label: t('llmConfig.model'),
        rules: [
          {
            required: true,
            message: t('common.enter_error', [
              t('llmConfig.model').toLowerCase()
            ]),
            trigger: 'blur'
          }
        ]
      },
      controlProps: {
        placeholder: t('common.enter_error', [
          t('llmConfig.model').toLowerCase()
        ])
      }
    },
    {
      type: 'textarea',
      field: 'desc',
      formItemProps: {
        name: 'desc',
        label: t('llmConfig.desc'),
        rules: [
          {
            required: true,
            message: t('common.enter_error', [t('llmConfig.desc')]),
            trigger: 'blur'
          }
        ]
      },
      controlProps: {
        placeholder: t('common.enter_error', [t('llmConfig.desc')])
      }
    }
  ])
  const getDisabledItems = computed(() =>
    mode.value === 'EDIT' ? ['platformId', 'model', 'apiKey'] : []
  )

  const handleOpen = async (payload: Payload) => {
    open.value = true
    title.value = Mode[payload.mode]
    mode.value = payload.mode
    payload.metaData && (formValue.value = payload.metaData)
    await getPlatforms()
    handleAutoFormConfig()
  }

  const handleOk = async () => {
    addAuthorization()
  }

  const handleTest = () => {
    testAuthorization()
  }

  const handleCancel = () => {
    title.value = Mode.ADD
    formValue.value = {}
    autoFormRef.value?.resetForm()
    open.value = false
  }

  const handleAutoFormConfig = () => {
    autoFormRef.value?.setOptionsVal('platformId', platforms.value)
  }

  const onPlatformChange = () => {
    const { platformId } = formValue.value
    formValue.value.model = ''
    const selectItems = platforms.value.filter((item) => item.id === platformId)
    autoFormRef.value?.setOptionsVal('model', selectItems)
  }

  defineExpose({
    handleOpen
  })

  const addAuthorization = async () => {
    try {
      const validate = await autoFormRef.value?.getFormValidation()
      if (!validate) return
      loading.value = true
      setTimeout(() => {
        // api
        console.log('formValue.value :>> ', formValue.value)
        loading.value = false
        handleCancel()
        emits('onOk')
      }, 1000)
    } catch (error) {
      console.log('error :>> ', error)
    }
  }

  const testAuthorization = () => {
    try {
      loadingTest.value = true
      setTimeout(() => {
        loadingTest.value = false
      }, 1000)
      emits('onTest')
    } catch (error) {
      console.log('error :>> ', error)
    }
  }
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
        v-model:form-value="formValue"
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
