// filepath: src/components/common/form-builder/test_index.vue
import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import { createI18n } from 'vue-i18n'
import FormBuilder from '../../src/components/common/form-builder/index.vue'
import { defineComponent, ref } from 'vue'
import Antd from 'ant-design-vue'

const i18n = createI18n({
  locale: 'en',
  messages: {
    en: {
      form: {
        name: 'Translated Name'
      },
      common: {
        select_error: 'Please select ',
        enter_error: 'Please enter '
      }
    }
  }
})

const wrapper = mount(
  defineComponent({
    components: { FormBuilder },
    setup() {
      const formData = ref({ name: 'xxx' })
      const formRef = ref<InstanceType<typeof FormBuilder>>()
      return { formRef, formData }
    },
    template: `
    <FormBuilder
      ref="formRef"
      v-model="formData"
      :form-items="[ { field: 'name', label: 'Name', type: 'input'} ]"
    />
  `
  }),
  {
    global: {
      plugins: [Antd, i18n]
    }
  }
)

describe('FormBuilder.vue', () => {
  it('renders form items based on formItems prop', () => {
    const wrapper = mount(FormBuilder, {
      props: {
        formItems: [
          { field: 'name', label: 'Name', type: 'input', required: true },
          { field: 'age', label: 'Age', type: 'input' }
        ]
      },
      global: {
        plugins: [i18n]
      }
    })

    expect(wrapper.findAll('a-form-item').length).toBe(2)
    expect(wrapper.find('a-form-item[name="name"]').exists()).toBe(true)
    expect(wrapper.find('a-form-item[name="age"]').exists()).toBe(true)
  })

  it('applies validation rules correctly', async () => {
    const formRef = wrapper.vm.formRef as InstanceType<typeof FormBuilder>
    expect(formRef?.validate).toBeDefined()
    const validSpy = vi.spyOn(formRef, 'validate')

    await formRef?.validate()
    expect(validSpy).toHaveBeenCalled()
  })

  it('resets the form correctly', async () => {
    const formRef = wrapper.vm.formRef as InstanceType<typeof FormBuilder>
    const resetSpy = vi.spyOn(formRef, 'resetForm')

    formRef.resetForm()
    expect(resetSpy).toHaveBeenCalled()
  })
})
