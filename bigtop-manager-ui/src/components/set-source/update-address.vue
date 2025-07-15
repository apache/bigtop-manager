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
  import { FormItemState } from '@/components/common/auto-form/types'

  interface Emits {
    (event: 'onOk', value: string): void
  }

  const emits = defineEmits<Emits>()

  const { t } = useI18n()
  const visible = ref(false)
  const autoFormRef = ref<Comp.AutoFormInstance | null>(null)
  const formValue = ref({ newAdress: '' })

  const formItems = computed((): FormItemState[] => [
    {
      type: 'input',
      field: 'newAdress',
      formItemProps: {
        name: 'newAdress',
        label: t('component.new_base_url'),
        rules: [
          {
            required: true,
            message: t('common.enter_error', [`${t('component.new_base_url')}`.toLowerCase()]),
            trigger: 'blur'
          }
        ]
      },
      controlProps: {
        placeholder: t('common.enter_error', [`${t('component.new_base_url')}`.toLowerCase()])
      }
    }
  ])

  const onSubmit = async () => {
    try {
      const validate = await autoFormRef.value?.getFormValidation()
      if (!validate) return
      emits('onOk', formValue.value.newAdress)
      onCancel()
    } catch (e) {
      console.log('Validation failed', e)
    }
  }

  const onCancel = () => {
    visible.value = false
    formValue.value.newAdress = ''
    autoFormRef.value?.resetForm()
  }

  const show = () => {
    visible.value = true
  }

  defineExpose({ show })
</script>

<template>
  <a-modal
    v-model:open="visible"
    :centered="true"
    :destroy-on-close="true"
    :title="t('component.base_url')"
    @ok="onSubmit"
    @cancel="onCancel"
  >
    <auto-form ref="autoFormRef" v-model:form-value="formValue" :form-items="formItems" :show-button="false" />
  </a-modal>
</template>

<style lang="scss" scoped></style>
