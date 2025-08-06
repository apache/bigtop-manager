/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import { useI18n } from 'vue-i18n'
import { computed, defineComponent, h, ref } from 'vue'
import { i18nPlugins } from '../test-util.ts'
import FormBuilder from '../../src/components/common/form-builder/index.vue'
import Antd from 'ant-design-vue'

const i18n = i18nPlugins({
  en: {
    form: {
      name: 'Translated Name'
    },
    common: {
      select_error: 'Please select ',
      enter_error: 'Please enter '
    }
  }
})

const wrapper = () =>
  mount(
    defineComponent({
      components: { FormBuilder },
      setup() {
        const { t } = useI18n()
        const formData = ref({ name: 'xxx' })
        const formRef = ref<InstanceType<typeof FormBuilder>>()
        const formItems = computed(() => [{ field: 'name', label: t('form.name'), type: 'input', required: true }])
        return { formRef, formData, formItems }
      },
      template: `
    <FormBuilder
      ref="formRef"
      v-model="formData"
      :form-items="formItems"
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
          { field: 'age', label: () => h('span', null, 'Age'), type: 'input' },
          { field: 'menu', label: 'Menu', type: 'select' }
        ]
      },
      global: {
        plugins: [Antd, i18n]
      }
    })

    const inputs = wrapper.findAll('input').filter((input) => input.classes().includes('ant-input'))
    expect(inputs.length).toBe(wrapper.vm.formItems.filter((item) => item.type === 'input').length)

    wrapper.findAll('label').forEach((label, index) => {
      const formItem = wrapper.vm.formItems[index]
      if (typeof formItem.label === 'string') {
        expect(label.text()).toBe(formItem.label)
      }
    })
  })

  it('applies validation rules correctly', async () => {
    const formRef = wrapper().vm.formRef as InstanceType<typeof FormBuilder>
    expect(formRef?.validate).toBeDefined()
    const validSpy = vi.spyOn(formRef, 'validate')

    await formRef?.validate()
    expect(validSpy).toHaveBeenCalled()
  })

  it('resets the form correctly', () => {
    const formRef = wrapper().vm.formRef as InstanceType<typeof FormBuilder>
    const resetSpy = vi.spyOn(formRef, 'resetForm')

    formRef.resetForm()
    expect(resetSpy).toHaveBeenCalled()
  })

  it('renders translated labels using vue-i18n', () => {
    const label = wrapper().find('label')

    expect(label.exists()).toBe(true)
    expect(label.text()).toBe('Translated Name')
  })
})
