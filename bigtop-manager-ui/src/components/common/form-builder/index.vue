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
  import { getFormItemComponent } from './config'
  import { isString } from 'lodash-es'

  import type { FormInstance, FormProps } from 'ant-design-vue'
  import type { FormItem } from './types'

  interface Props {
    formItems: FormItem[]
    formConfig?: FormProps
    span?: number
    rules?: Record<string, any>
    gutter?: number[] | number | Record<string, any>
  }

  defineOptions({ name: 'FormBuilder' })

  const props = withDefaults(defineProps<Props>(), {
    formItems: () => [],
    formConfig: () => ({}),
    rules: () => [],
    gutter: () => [8, 8],
    span: 24
  })

  /**
   *
   * Form data
   *
   * @extends
   *   defineProps<{ modelValue: Record<string, any> }>()
   *   defineEmits(['update:modelValue'])
   *
   *   const formData = computed({
   *    get: () => props.modelValue,
   *    set: (val) => emit('update:modelValue', val),
   *   })
   */
  const formData = defineModel<Record<string, any>>({ default: () => ({}) })

  const selectType = new Set(['select', 'date', 'time', 'treeSelect'])
  const baseFieldReg = /^(?:type|label|props|on|span|key|hidden|required|rules|col|formProps)$/

  const { t } = useI18n()
  const formRef = ref<FormInstance>()
  const labelRefs = ref<HTMLElement[]>()

  // Filter hidden form item
  const formItemsComputed = computed(() => props.formItems.filter((item) => item.hidden !== true))

  // Calculate the max width of the label
  const defaultLabelCol = computed(() => {
    let defaultLabelWidth = 60
    labelRefs.value?.forEach((label: HTMLElement) => {
      const newWidth = label!.offsetWidth + 20
      if (defaultLabelWidth < newWidth) {
        defaultLabelWidth = newWidth
      }
    })

    return `${defaultLabelWidth}px`
  })

  // Merge rules
  const requiredRules = computed(() => {
    return props.formItems.reduce((pre, item) => {
      const { type = 'input', label, required = false } = item
      if (!pre[item.field] && isString(type) && isString(label) && required) {
        const errorMsg = `${selectType.has(type) ? 'common.select_error' : 'common.enter_error'}`
        pre[item.field] = props.rules[item.field] ?? [
          {
            required: true,
            message: t(`${errorMsg}`) + label.toLowerCase(),
            trigger: 'blur'
          }
        ]
      }
      return pre
    }, {})
  })

  /**
   * Get form item props
   * @param formItem
   */
  function getFormItemProps(formItem: FormItem) {
    const { formProps = {} } = formItem
    const mergedProps = {
      ...formProps // else props
    }
    // do something...

    return mergedProps
  }

  const ComponentItem = {
    props: ['item'],
    setup({ item }: { item: FormItem }) {
      // Filter invalid prop
      const props = Object.keys(item).reduce<Record<string, any>>(
        (prev, key) => {
          if (!baseFieldReg.test(key)) {
            prev[key] = item[key]
          }
          return prev
        },
        { ...item.props, formData: formData.value }
      )

      // Default placeholder
      if (!('placeholder' in props)) {
        if (typeof item.label != 'function') {
          const prefix = selectType.has((item.type as string) ?? '') ? 'common.select_error' : 'common.enter_error'
          props.placeholder = t(`${prefix}`) + item.label?.toLowerCase()
        }
      }

      const tag = getFormItemComponent(item.type)
      const slots = item.slots ? { default: () => item.slots } : undefined

      return () =>
        h(
          tag,
          {
            ...props,
            modelValue: formData.value[item.field],
            'onUpdate:modelValue': (val: string) => {
              formData.value[item.field] = val
            }
          },
          slots
        )
    }
  }

  function validate() {
    return formRef.value?.validate()
  }

  function resetForm(name?: string) {
    formRef.value?.resetFields(name)
  }

  defineExpose({
    validate,
    resetForm
  })
</script>

<template>
  <a-form
    ref="formRef"
    :model="formData"
    :rules="requiredRules"
    v-bind="{ labelCol: { style: { width: defaultLabelCol } }, ...{ colon: false, labelAlign: 'left' }, ...formConfig }"
  >
    <a-row :gutter="props.gutter as any">
      <a-col v-for="item of formItemsComputed" :key="item.field" :span="item.span || span">
        <a-form-item :name="item.field" v-bind="getFormItemProps(item)">
          <template #label>
            <div ref="labelRefs">
              <span v-if="typeof item.label === 'function'">
                <component :is="item.label as Function" />
              </span>
              <span v-else>{{ item.label }}</span>
            </div>
          </template>
          <slot :name="item.field" :item="item">
            <component :is="ComponentItem" :item="item" />
          </slot>
        </a-form-item>
      </a-col>
    </a-row>
  </a-form>
</template>
