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
  import { computed, onMounted, ref, toRaw, watch } from 'vue'
  import type { FormItemProps } from 'ant-design-vue'
  import type { ColProps } from 'ant-design-vue/es/grid/Col'

  type FormState = Record<string, unknown>

  export interface FormItemState {
    type: string
    field: string
    slot?: string
    slotLabel?: string
    defaultValue?: string
    fieldMap?: { label: string; value: string }
    formItemProps?: FormItemProps
    controlProps?: any
    defaultOptionsMap?: unknown[]
    on?: any
  }

  export interface FormOptions {
    hideOk: boolean
    hideCancel: boolean
    okText: string
    cancelText: string
  }

  interface Props {
    formValue: Record<string, unknown>
    formItems: FormItemState[]
    formOptions?: FormOptions
    labelCol?: ColProps
    wrapperCol?: ColProps
    formDisabled?: boolean
    disabledItems?: string[]
    hiddenItems?: string[]
    showButton?: boolean
  }

  interface Emits {
    (event: 'update:formValue', formValue: Record<string, unknown>): void
  }

  const props = withDefaults(defineProps<Props>(), {
    showButton: true,
    disabledItems: () => [],
    hiddenItems: () => [],
    labelCol: () => {
      return { span: 7 }
    },
    wrapperCol: () => {
      return { span: 14 }
    },
    formOptions: () => {
      return {
        hideOk: false,
        hideCancel: false,
        okText: 'Submit',
        cancelText: 'Reset'
      }
    },
    on: () => {}
  })

  const emits = defineEmits<Emits>()

  const formRef = ref()
  const formState = ref<FormState>({})
  const tmpCacheFormState = ref<FormState>({})
  const optionsMap = ref<Record<string, unknown>>({})

  const ruleFormTmpResolve = computed({
    get() {
      const len = Object.keys(props.formValue).length
      return len ? props.formValue : tmpCacheFormState.value
    },
    set(val) {
      tmpCacheFormState.value = val || {}
      emits('update:formValue', val)
    }
  })

  watch(
    formState,
    (newState) => {
      emits('update:formValue', newState)
    },
    {
      deep: true
    }
  )

  const initForm = () => {
    const newForm: FormState = {}
    props.formItems.forEach(({ field, defaultValue }) => {
      newForm[field] = defaultValue || ''
    })
    Object.assign(formState.value, {
      ...newForm,
      ...ruleFormTmpResolve.value
    })
  }

  const setOptionsVal = (field: string, list: unknown[]) => {
    optionsMap.value[field] = list
  }

  const setFormValue = (form: FormState) => {
    let oldForm = { ...toRaw(formState.value) }
    formState.value = { ...oldForm, ...form }
  }

  const onSubmit = () => {
    formRef.value
      .validate()
      .then(() => {
        emits('update:formValue', toRaw(formState.value))
      })
      .catch((error: Error) => {
        console.log('error', error)
      })
  }

  const resetForm = () => {
    formRef.value.resetFields()
  }

  const onReset = () => {}

  onMounted(() => {
    initForm()
  })

  defineExpose({
    onSubmit,
    resetForm,
    setOptionsVal,
    setFormValue
  })
</script>

<template>
  <div>
    <a-form
      ref="formRef"
      label-align="left"
      :model="formState"
      :label-col="props.labelCol"
      :wrapper-col="props.wrapperCol"
      :disabled="props.formDisabled"
    >
      <div v-for="item in formItems" :key="item.field">
        <slot :="{ item, state: formState }" :name="item.field">
          <a-form-item
            v-if="!props.hiddenItems.includes(item.field)"
            v-bind="item.formItemProps"
          >
            <!-- input -->
            <a-input
              v-if="item.type == 'input'"
              v-model:value="formState[item.field]"
              v-bind="item.controlProps"
              v-on="item.on || {}"
            />

            <!-- textarea  -->
            <a-textarea
              v-if="item.type == 'textarea'"
              v-model:value="formState[item.field]"
              v-bind="item.controlProps"
              v-on="item.on || {}"
            />

            <!-- select -->
            <a-select
              v-if="item.type == 'select'"
              v-model:value="formState[item.field]"
              v-bind="item.controlProps"
              v-on="item.on || {}"
            >
              <a-select-option
                v-for="(child, childIndex) in optionsMap[item.field] ||
                item.defaultOptionsMap ||
                []"
                :key="childIndex"
                :value="
                  child[(item.fieldMap && item.fieldMap.value) || 'value']
                "
              >
                {{ child[(item.fieldMap && item.fieldMap.label) || 'label'] }}
              </a-select-option>
            </a-select>

            <!-- radio -->
            <a-radio-group
              v-if="item.type == 'radio'"
              v-model:value="formState[item.field]"
              v-bind="item.controlProps"
              v-on="item.on || {}"
            >
              <a-radio
                v-for="(child, childIndex) in optionsMap[item.field] ||
                item.defaultOptionsMap ||
                []"
                :key="childIndex"
                :value="
                  child[(item.fieldMap && item.fieldMap.value) || 'value']
                "
              >
                {{ child[(item.fieldMap && item.fieldMap.label) || 'label'] }}
              </a-radio>
            </a-radio-group>
          </a-form-item>
        </slot>
      </div>
      <a-form-item v-if="showButton">
        <slot name="actions" :on-submit="onSubmit" :on-reset="onReset">
          <a-space>
            <a-button type="primary" @click="onSubmit">
              {{ props.formOptions.okText }}
            </a-button>
            <a-button @click="onReset">
              {{ props.formOptions.hideCancel }}
            </a-button>
          </a-space>
        </slot>
      </a-form-item>
    </a-form>
  </div>
</template>
