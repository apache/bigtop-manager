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
  import { computed, onMounted, onUnmounted, ref, watchPostEffect } from 'vue'
  import { usePngImage } from '@/utils/tools'
  import { useI18n } from 'vue-i18n'
  import type {
    FormItemState,
    FormState
  } from '@/components/common/auto-form/types'

  const { t } = useI18n()
  const helper = usePngImage('helper')
  const autoFormRef = ref<Comp.AutoFormInstance | null>(null)
  const formValue = ref<FormState>({})
  const formItems = computed((): FormItemState[] => [
    {
      type: 'input',
      field: 'name',
      formItemProps: {
        name: 'name',
        label: t('xxxx.name'),
        rules: [
          { required: true, message: '请输入名字', trigger: 'blur' },
          { min: 3, max: 50, message: '长度3-50', trigger: 'blur' },
          {
            pattern: /^[0-9a-zA-Z_]{1,}$/,
            message: '请输入正确的名字，字母/数字/下划线,'
          }
        ]
      },
      controlProps: {
        placeholder: '请输入你的名字'
      }
    },
    {
      type: 'input',
      field: 'platform',
      formItemProps: {
        name: 'platform',
        label: '平台',
        rules: [{ required: true, message: '请输入平台', trigger: 'blur' }]
      }
    },
    {
      type: 'input',
      field: 'apiKey',
      formItemProps: {
        name: 'apiKey',
        label: 'API Key',
        rules: [{ required: true, message: '请输入API Key', trigger: 'blur' }]
      }
    },
    {
      type: 'input',
      field: 'model',
      formItemProps: {
        name: 'model',
        label: '模型',
        rules: [{ required: true, message: '请输入模型', trigger: 'blur' }]
      }
    },
    {
      type: 'input',
      field: 'port',
      slot: 'port',
      slotLabel: 'gRPC端口',
      formItemProps: {
        name: 'port',
        rules: [{ required: true, message: '请输入备注', trigger: 'blur' }]
      }
    },
    {
      type: 'select',
      field: 'resGroupId',
      defaultValue: '',
      fieldMap: {
        label: 'name',
        value: 'resourceGroupId'
      },
      formItemProps: {
        name: 'resGroupId',
        label: '选择资源组',
        rules: [{ required: true, message: '请选择资源组', trigger: 'blur' }]
      },
      controlProps: {
        placeholder: '请选择资源组'
      }
    },
    {
      type: 'textarea',
      field: 'remark',
      formItemProps: {
        name: 'remark',
        label: '备注',
        rules: [{ required: true, message: '请输入备注', trigger: 'blur' }]
      }
    },
    {
      type: 'radio',
      field: 'type',
      defaultValue: '0',
      defaultOptionsMap: [
        { value: '0', label: '向导' },
        { value: '1', label: '脚本' }
      ],
      formItemProps: {
        name: 'type',
        label: '创建类型',
        rules: [{ required: true, message: '请选择创建类型', trigger: 'blur' }]
      }
    }
  ])

  const resourceList = [
    {
      name: '资源组1',
      resourceGroupId: 'id_001'
    },
    {
      name: '资源组2',
      resourceGroupId: 'id_002'
    },
    {
      name: '资源组3',
      resourceGroupId: 'id_003'
    }
  ]

  onMounted(() => {
    autoFormRef.value?.setOptions('resGroupId', resourceList)
  })

  onUnmounted(() => {
    autoFormRef.value?.resetForm()
  })

  watchPostEffect(() => {
    autoFormRef.value?.setOptions('resGroupId', resourceList)
    autoFormRef.value?.resetForm()
  })
</script>

<template>
  <auto-from
    ref="autoFormRef"
    v-model:form-value="formValue"
    :form-items="formItems"
    :show-button="false"
    :hidden-items="['type']"
  >
    <template #port="{ item, state }">
      <a-form-item v-bind="item.formItemProps">
        <template #label>
          <div class="label-style">
            <span>{{ item.slotLabel }}</span>
            <img :src="helper" />
          </div>
        </template>
        <a-input v-model:value="state[item.field]" />
      </a-form-item>
    </template>
  </auto-from>
</template>

<style lang="scss" scoped>
  .label-style {
    display: flex;
    align-items: center;
    img {
      width: 14px;
      line-height: 22px;
      margin-left: 2px;
      vertical-align: middle;
      cursor: pointer;
    }
  }
</style>
