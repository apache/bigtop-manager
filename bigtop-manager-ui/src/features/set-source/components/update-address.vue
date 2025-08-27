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
  import type { FormItem } from '@/components/common/form-builder/types'

  const emits = defineEmits<{ (event: 'onOk', value: string): void }>()

  const { t } = useI18n()
  const open = ref(false)
  const formRef = ref<Comp.FormBuilderInstance | null>(null)
  const formValue = ref({ newAddress: '' })

  const formItems = computed((): FormItem[] => [
    {
      type: 'input',
      field: 'newAddress',
      label: t('component.new_base_url'),
      required: true
    }
  ])

  const onSubmit = async () => {
    try {
      const validate = await formRef.value?.validate()
      if (!validate) return
      emits('onOk', formValue.value.newAddress)
      toggleVisible()
    } catch (e) {
      console.log('Validation failed', e)
    }
  }

  const toggleVisible = (visible = false) => {
    open.value = visible
    if (visible) {
      formRef.value?.resetForm()
    }
  }

  defineExpose({ toggleVisible })
</script>

<template>
  <a-modal
    v-model:open="open"
    :centered="true"
    :destroy-on-close="true"
    :title="t('component.base_url')"
    @ok="onSubmit"
    @cancel="toggleVisible(false)"
  >
    <form-builder ref="formRef" v-model="formValue" :form-items="formItems" />
  </a-modal>
</template>

<style lang="scss" scoped></style>
