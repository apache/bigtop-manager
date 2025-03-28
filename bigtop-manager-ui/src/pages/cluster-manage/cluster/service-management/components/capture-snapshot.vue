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
  import { useI18n } from 'vue-i18n'
  import { computed, ref, shallowRef } from 'vue'
  import { takeServiceConfigSnapshot } from '@/api/service'
  import type { FormItemState } from '@/components/common/auto-form/types'
  import type { ServiceParams, SnapshotData } from '@/api/service/types'
  import { message } from 'ant-design-vue'

  const { t } = useI18n()
  const open = ref(false)
  const loading = ref(false)
  const serviceInfo = shallowRef<ServiceParams>()
  const autoFormRef = ref<Comp.AutoFormInstance | null>(null)
  const formValue = ref<SnapshotData>({})
  const emits = defineEmits(['success'])
  const formItems = computed((): FormItemState[] => [
    {
      type: 'input',
      field: 'name',
      formItemProps: {
        name: 'name',
        label: t('service.snapshot_name'),
        rules: [
          {
            required: true,
            message: t('common.enter_error', [`${t('service.snapshot_name')}`.toLowerCase()]),
            trigger: 'blur'
          }
        ]
      },
      controlProps: {
        placeholder: t('common.enter_error', [`${t('service.snapshot_name')}`.toLowerCase()])
      }
    },
    {
      type: 'textarea',
      field: 'desc',
      formItemProps: {
        name: 'desc',
        label: t('service.snapshot_description'),
        rules: [
          {
            required: true,
            message: t('common.enter_error', [`${t('service.snapshot_description')}`.toLowerCase()]),
            trigger: 'blur'
          }
        ]
      },
      controlProps: {
        placeholder: t('common.enter_error', [`${t('service.snapshot_description')}`.toLowerCase()])
      }
    }
  ])

  const handleOpen = (data: ServiceParams) => {
    serviceInfo.value = data
    open.value = true
  }

  const handleOk = async () => {
    const validate = await autoFormRef.value?.getFormValidation()
    if (!validate || !serviceInfo.value) return
    loading.value = true
    try {
      await takeServiceConfigSnapshot(serviceInfo.value, formValue.value)
      message.success(t('common.success_msg'))
      emits('success')
      handleCancel()
    } catch (e) {
      console.log(e)
    } finally {
      loading.value = false
    }
  }

  const handleCancel = () => {
    formValue.value = {}
    open.value = false
  }

  defineExpose({
    handleOpen
  })
</script>

<template>
  <div class="set-source">
    <a-modal
      v-model:open="open"
      width="50%"
      :centered="true"
      :mask="false"
      :title="$t('service.capture_snapshot')"
      :mask-closable="false"
      :destroy-on-close="true"
      :confirm-loading="loading"
      @ok="handleOk"
      @cancel="handleCancel"
    >
      <auto-form ref="autoFormRef" v-model:form-value="formValue" :show-button="false" :form-items="formItems" />
    </a-modal>
  </div>
</template>

<style lang="scss" scoped></style>
