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
  import { takeServiceConfigSnapshot } from '@/api/service'
  import { message } from 'ant-design-vue'

  import type { FormItem } from '@/components/common/form-builder/types'
  import type { ServiceParams, SnapshotData } from '@/api/service/types'

  const emits = defineEmits(['success'])

  const { t } = useI18n()
  const open = ref(false)
  const loading = ref(false)
  const serviceInfo = shallowRef<ServiceParams>()
  const formRef = ref<Comp.FormBuilderInstance | null>(null)
  const formValue = ref<SnapshotData>({})
  const formItems = computed((): FormItem[] => [
    {
      type: 'input',
      field: 'name',
      label: t('service.snapshot_name'),
      required: true
    },
    {
      type: 'textarea',
      field: 'desc',
      label: t('service.snapshot_description'),
      required: true
    }
  ])

  const handleOpen = (data: ServiceParams) => {
    serviceInfo.value = data
    open.value = true
  }

  const handleOk = async () => {
    const validate = await formRef.value?.validate()
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
      :open="open"
      width="600px"
      :centered="true"
      :title="t('service.capture_snapshot')"
      :destroy-on-close="true"
      :confirm-loading="loading"
      @ok="handleOk"
      @cancel="handleCancel"
    >
      <form-builder ref="formRef" v-model="formValue" :form-items="formItems" />
    </a-modal>
  </div>
</template>

<style lang="scss" scoped></style>
