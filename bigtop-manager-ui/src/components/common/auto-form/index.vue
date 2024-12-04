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
  import { computed, onMounted, ref, toRaw, toRefs, watch } from 'vue'
  import { Emits, FormState, Props } from './types'

  const props = withDefaults(defineProps<Props>(), {
    showButton: true,
    disabledItems: null, // Has higher priority than controlProps of formItems
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
    }
  })

  const emits = defineEmits<Emits>()

  const formRef = ref()
  const formState = ref<FormState>({})
  const tmpCacheFormState = ref<FormState>({})
  const optionMap = ref<Record<string, unknown>>({})
  const formItemEvents = ref<Record<string, unknown>>({})
  const { formValue } = toRefs(props)

  const ruleFormTmpResolve = computed({
    get() {
      const len = Object.keys(formValue.value).length
      return len ? formValue.value : tmpCacheFormState.value
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
    props.formItems.forEach((item) => {
      const { field, defaultValue } = item
      newForm[field] = defaultValue || undefined
    })
    Object.assign(formState.value, {
      ...newForm,
      ...ruleFormTmpResolve.value
    })
  }

  const getFormValidation = async () => {
    try {
      await formRef.value.validateFields()
      return Promise.resolve(true)
    } catch (error) {
      console.log('Failed:', error)
      return Promise.resolve(false)
    }
  }

  const setOptions = (field: string, list: unknown[]) => {
    optionMap.value[field] = list
  }

  const setFormItemEvents = (field: string, events: any) => {
    formItemEvents.value[field] = events
  }

  const setFormValue = <T,>(form: FormState<T>) => {
    const oldForm = { ...toRaw(formState.value) }
    formState.value = { ...oldForm, ...form }
  }

  const resetForm = () => {
    formRef.value.resetFields()
  }

  const onSubmit = async () => {
    try {
      await formRef.value.validateFields()
      emits('onSubmit', true)
    } catch (error) {
      console.log('Failed:', error)
      emits('onSubmit', false)
    }
  }

  const onReset = () => {
    resetForm()
  }

  onMounted(() => {
    initForm()
  })

  defineExpose({
    resetForm,
    getFormValidation,
    setOptions,
    setFormItemEvents,
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
      <div v-for="item in props.formItems" :key="item.field">
        <slot :="{ item, state: formState }" :name="item.field">
          <a-form-item v-if="!props.hiddenItems.includes(item.field)" v-bind="item.formItemProps">
            <!-- input -->
            <a-input
              v-if="item.type == 'input'"
              v-model:value="formState[item.field]"
              v-bind="item.controlProps"
              :disabled="disabledItems ? disabledItems.includes(item.field) : item.controlProps.disabled"
              v-on="formItemEvents[item.field] || {}"
            />

            <!-- textarea  -->
            <a-textarea
              v-if="item.type == 'textarea'"
              v-model:value="formState[item.field]"
              v-bind="item.controlProps"
              :disabled="disabledItems ? disabledItems.includes(item.field) : item.controlProps.disabled"
              v-on="formItemEvents[item.field] || {}"
            />

            <!-- select -->
            <a-select
              v-if="item.type == 'select'"
              v-model:value="formState[item.field]"
              v-bind="item.controlProps"
              :disabled="disabledItems ? disabledItems.includes(item.field) : item.controlProps.disabled"
              v-on="formItemEvents[item.field] || {}"
            >
              <a-select-option
                v-for="(child, childIndex) in optionMap[item.field] || item.defaultOptionsMap || []"
                :key="childIndex"
                :value="typeof child === 'string' ? child : child[(item.fieldMap && item.fieldMap.value) || 'value']"
              >
                {{ typeof child === 'string' ? child : child[(item.fieldMap && item.fieldMap.label) || 'label'] }}
              </a-select-option>
            </a-select>

            <!-- radio -->
            <a-radio-group
              v-if="item.type == 'radio'"
              v-model:value="formState[item.field]"
              v-bind="item.controlProps"
              :disabled="disabledItems ? disabledItems.includes(item.field) : item.controlProps.disabled"
              v-on="formItemEvents[item.field] || {}"
            >
              <a-radio
                v-for="(child, childIndex) in optionMap[item.field] || item.defaultOptionsMap || []"
                :key="childIndex"
                :value="child[(item.fieldMap && item.fieldMap.value) || 'value']"
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
