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

import {
  Cascader,
  Checkbox,
  CheckboxGroup,
  DatePicker,
  Input,
  InputNumber,
  InputPassword,
  Radio,
  RadioGroup,
  Select,
  Slider,
  Switch,
  Textarea,
  TimePicker,
  TreeSelect
} from 'ant-design-vue'
import { isString } from 'lodash-es'
import { type Component, h, defineComponent } from 'vue'

/**
 * Transforms a component's `v-model:value` binding into the standard `v-model` binding.
 *
 * @param component - The component to transform.
 * @param key - The key to use for the model value, defaults to 'value'.
 * @returns A new component that supports `v-model` shorthand.
 */
export function transformModelValue(component: Component, key = 'value'): Component {
  return defineComponent({
    setup(props: any, { attrs, slots, emit }) {
      return () => {
        const { modelValue = undefined, ..._props } = { ...props, ...attrs }
        return h(
          component,
          {
            ..._props,
            [key]: modelValue,
            [`onUpdate:${key}`]: emit.bind(null, 'update:modelValue')
          },
          slots
        )
      }
    }
  })
}

const formItemMap = new Map<string, Component>([
  ['input', transformModelValue(Input)],
  ['textarea', transformModelValue(Textarea)],
  ['number', transformModelValue(InputNumber)],
  ['password', transformModelValue(InputPassword)],
  ['time', transformModelValue(TimePicker)],
  [
    'date',
    transformModelValue((props, { slots, attrs }) =>
      h(DatePicker, { valueFormat: 'YYYY-MM-DD', ...attrs, ...props }, slots)
    )
  ],
  ['cascader', Cascader],
  ['slider', Slider],
  ['checkbox', transformModelValue(Checkbox, 'checked')],
  ['checkboxGroup', CheckboxGroup],
  ['radio', Radio],
  ['radioGroup', transformModelValue(RadioGroup)],
  ['switch', Switch],
  ['treeSelect', TreeSelect],
  ['select', transformModelValue(Select)]
])

export function getFormItemComponent(type?: string | Component): Component {
  if (type && !isString(type)) return type
  return (type && formItemMap.get(type)) || formItemMap.get('input') || Input
}
