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

import { FormItemState } from '@/components/common/auto-form/types'
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

export function useFormItemConfig() {
  const { t } = useI18n()
  const formItemConfig = computed((): FormItemState[] => [
    {
      type: 'input',
      field: 'name',
      formItemProps: {
        name: 'name',
        label: t('llmConfig.name'),
        rules: [
          {
            required: true,
            message: t('common.enter_error', [
              `${t('llmConfig.name')}`.toLowerCase()
            ]),
            trigger: 'blur'
          }
        ]
      },
      controlProps: {
        placeholder: t('common.enter_error', [
          `${t('llmConfig.name')}`.toLowerCase()
        ])
      }
    },
    {
      type: 'select',
      field: 'platformId',
      defaultValue: '',
      fieldMap: {
        label: 'name',
        value: 'id'
      },
      formItemProps: {
        name: 'platformId',
        label: t('llmConfig.platform_name'),
        rules: [
          {
            required: true,
            message: t('common.select_error', [
              t('llmConfig.platform_name').toLowerCase()
            ]),
            trigger: 'blur'
          }
        ]
      },
      controlProps: {
        placeholder: t('common.select_error', [
          t('llmConfig.platform_name').toLowerCase()
        ])
      }
    },
    {
      type: 'select',
      field: 'model',
      formItemProps: {
        name: 'model',
        label: t('llmConfig.model'),
        rules: [
          {
            required: true,
            message: t('common.select_error', [
              t('llmConfig.model').toLowerCase()
            ]),
            trigger: 'blur'
          }
        ]
      },
      controlProps: {
        placeholder: t('common.select_error', [
          t('llmConfig.model').toLowerCase()
        ])
      }
    },
    {
      type: 'textarea',
      field: 'desc',
      formItemProps: {
        name: 'desc',
        label: t('llmConfig.desc'),
        rules: [
          {
            required: true,
            message: t('common.enter_error', [t('llmConfig.desc')]),
            trigger: 'blur'
          }
        ]
      },
      controlProps: {
        placeholder: t('common.enter_error', [t('llmConfig.desc')])
      }
    }
  ])
  const createNewFormItem = (
    type: string,
    field: string,
    label: string
  ): FormItemState => {
    return {
      type,
      field,
      formItemProps: {
        name: field,
        label,
        rules: [
          {
            required: true,
            message: t('common.enter_error', [`${field}`]),
            trigger: 'blur'
          }
        ]
      },
      controlProps: {
        placeholder: t('common.enter_error', [`${field}`])
      }
    }
  }

  return { formItemConfig, createNewFormItem }
}
