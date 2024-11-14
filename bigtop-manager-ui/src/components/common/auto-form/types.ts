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
import type { FormItemProps, ColProps } from 'ant-design-vue'

export type FormState<T = Record<string, any>> = T

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
}

export interface FormOptions {
  hideOk: boolean
  hideCancel: boolean
  okText: string
  cancelText: string
}

export interface Props {
  formValue: FormState
  formItems: FormItemState[]
  formOptions?: FormOptions
  labelCol?: ColProps
  wrapperCol?: ColProps
  formDisabled?: boolean
  disabledItems?: string[] | null
  hiddenItems?: string[]
  showButton?: boolean
}

export interface Emits {
  (event: 'update:formValue', formValue: FormState): void
  (event: 'onSubmit', validate: boolean): void
}
