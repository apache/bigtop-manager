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

<script lang="ts" setup>
  import { reactive, ref } from 'vue'
  import type { UnwrapRef } from 'vue'
  import type { Rule } from 'ant-design-vue/es/form'

  interface FormState {
    name: string
    platform: string
    apiKey: string
    model: string
    resource: string
    remark: string
  }

  const formRef = ref()
  const labelCol = { span: 7 }
  const wrapperCol = { span: 14 }
  const formState: UnwrapRef<FormState> = reactive({
    name: '',
    platform: '',
    apiKey: '',
    model: '',
    resource: '',
    remark: ''
  })

  const rules: Record<string, Rule[]> = {
    name: [
      {
        required: true,
        message: 'Please input name',
        trigger: 'change'
      },
      { min: 3, max: 5, message: 'Length should be 3 to 5', trigger: 'blur' }
    ],
    platform: [
      {
        required: true,
        message: 'Please select platform',
        trigger: 'change'
      }
    ],
    apiKey: [
      {
        required: true,
        message: 'Please pick a date',
        trigger: 'change',
        type: 'object'
      }
    ],
    model: [
      {
        required: true,
        message: 'Please input model',
        trigger: 'change'
      }
    ],
    remark: [
      { required: true, message: 'Please input activity form', trigger: 'blur' }
    ]
  }

  // const onSubmit = () => {
  //   formRef.value
  //     .validate()
  //     .then(() => {
  //       console.log('values', formState, toRaw(formState))
  //     })
  //     .catch((error) => {
  //       console.log('error', error)
  //     })
  // }
  // const resetForm = () => {
  //   formRef.value.resetFields()
  // }
</script>

<template>
  <div>
    <a-form
      ref="formRef"
      label-align="left"
      :model="formState"
      :rules="rules"
      :label-col="labelCol"
      :wrapper-col="wrapperCol"
    >
      <a-form-item label="名字" name="name">
        <a-input v-model:value="formState.name" />
      </a-form-item>
      <a-form-item label="平台" name="platform">
        <a-input v-model:value="formState.platform" />
      </a-form-item>
      <a-form-item label="API Key" required name="apiKey">
        <a-input v-model:value="formState.apiKey" />
      </a-form-item>
      <a-form-item label="模型" name="model">
        <a-input v-model:value="formState.model" />
      </a-form-item>
      <a-form-item label="备注" name="remark">
        <a-textarea v-model:value="formState.remark" />
      </a-form-item>
    </a-form>
  </div>
</template>
