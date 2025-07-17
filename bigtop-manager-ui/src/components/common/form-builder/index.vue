<script lang="ts" setup>
  import type { FormInstance } from 'ant-design-vue'
  import type { IFormItem } from './types'

  import { h, ref, computed } from 'vue'
  import { getFormItemComponent } from './config'
  import { isString } from 'lodash-es'
  import { useI18n } from 'vue-i18n'

  interface Props {
    formItems: IFormItem[]
    formConfig?: Record<string, any>
    span?: number
    rules?: Record<string, any>
    gutter?: number[] | number | Record<string, any>
  }

  const { t } = useI18n()

  defineOptions({
    name: 'FormBuilder'
  })

  const props = withDefaults(defineProps<Props>(), {
    formItems: () => [],
    formConfig: () => ({ colon: false }),
    rules: () => [],
    gutter: () => [8, 8],
    span: 14
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
  const formData = defineModel<Record<string, any>>({
    default: () => ({})
  })

  const selectType = new Set(['select', 'date', 'time', 'treeSelect'])
  const baseFieldReg = /^(?:type|label|props|on|span|key|hidden|required|rules|col|formProps)$/

  const formRef = ref<FormInstance>()
  const labelRefs = ref<HTMLElement[]>()

  // Filter hidden form item
  const formItemsComputed = computed(() => props.formItems.filter((item) => item.hidden !== true))

  // Calculate the max width of the label
  const defaultLabelCol = computed(() => {
    let defaultLabelWidth = '60px'
    labelRefs.value?.forEach((label: HTMLElement) => {
      if (Number.parseInt(defaultLabelWidth) < label!.offsetWidth) {
        defaultLabelWidth = `${Number.parseFloat(`${label!.offsetWidth + 20}`).toFixed(2)}px`
      }
    })

    return defaultLabelWidth
  })

  // Merge rules
  const requiredRules = computed(() => {
    const rules = props.formItems.reduce((pre, item) => {
      const { type = 'input', label, required = false } = item
      if (!pre[item.field] && isString(type) && isString(label) && required) {
        const errorMsg = `${selectType.has(type) ? 'common.select_error' : 'common.enter_error'}`
        pre[item.field] = [
          {
            required: true,
            message: t(`${errorMsg}`) + label.toLowerCase(),
            trigger: 'blur'
          },
          ...(props.rules[item.field] ?? [])
        ]
      }
      return pre
    }, {})
    return rules
  })

  /**
   * Get form item props
   * @param formItem
   */
  function getFormItemProps(formItem: IFormItem) {
    const { formProps = {} } = formItem
    const mergedProps = {
      ...formProps // else props
    }

    return mergedProps
  }

  const ComponentItem = {
    props: ['item'],
    setup({ item }: { item: IFormItem }) {
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

      // Add default placeholder
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

  defineExpose({
    validate
  })
</script>

<template>
  <a-form
    ref="formRef"
    :model="formData"
    :rules="requiredRules"
    v-bind="{ labelCol: { style: { width: defaultLabelCol } }, ...formConfig }"
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
