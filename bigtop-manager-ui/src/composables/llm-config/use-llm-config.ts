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

import { computed, type Ref, ref } from 'vue'
import * as llmServer from '@/api/llm-config/index'
import { useI18n } from 'vue-i18n'
import { Modal, message } from 'ant-design-vue'
import { addFormItemEvents } from '@/components/common/auto-form/helper'
import { useFormItem } from './use-form-item'

import type {
  AuthorizedPlatform,
  PlatformCredential,
  UpdateAuthorizedPlatformConfig
} from '@/api/llm-config/types'

import type {
  FormItemState,
  FormState
} from '@/components/common/auto-form/types'

interface Platform {
  id: number
  name: string
  supportModels: string[]
}

type BaseType = Record<string, unknown>

export function useLlmConfig(autoFormRef?: Ref<Comp.AutoFormInstance | null>) {
  const { t } = useI18n()
  const { formItemConfig, createNewFormItem } = useFormItem()

  const loading = ref<boolean>(false)
  const loadingTest = ref<boolean>(false)
  const testPassed = ref<boolean>(false)
  const platforms = ref<Platform[]>([])
  const formCredentials = ref<PlatformCredential[]>([])
  const authorizedPlatforms = ref<AuthorizedPlatform[]>([])
  const currPlatForm = ref<FormState<AuthorizedPlatform | BaseType>>({})
  const getFormDisabled = computed(() => loading.value || loadingTest.value)
  const getBaseFormItems = computed(() =>
    addFormItemEvents(formItemConfig, 'platformId', {
      change: onPlatformChange
    })
  )
  const getFormItems = computed((): FormItemState[] => {
    const tmpBaseFormItems = [...getBaseFormItems.value]
    const newFormItems = formCredentials.value?.map((v) =>
      createNewFormItem('input', v.name, v.displayName)
    ) as FormItemState[]
    if (newFormItems) {
      newFormItems.length > 0 && tmpBaseFormItems.splice(2, 0, ...newFormItems)
    }
    return tmpBaseFormItems
  })

  const getAuthorizedPlatforms = async () => {
    try {
      loading.value = true
      const res = await llmServer.getAuthorizedPlatforms()
      authorizedPlatforms.value = res
      loading.value = false
    } catch (error) {
      console.log('error :>> ', error)
    } finally {
      loading.value = false
    }
  }

  const getPlatforms = async () => {
    try {
      const data = await llmServer.getPlatforms()
      platforms.value = data.map((platform) => {
        return {
          ...platform,
          supportModels: platform.supportModels.split(',')
        } as Platform
      })
      autoFormRef?.value?.setOptionsVal('platformId', platforms.value)
    } catch (error) {
      console.log('error :>> ', error)
    }
  }

  const getPlatformCredentials = async () => {
    try {
      const { platformId } = currPlatForm.value
      if (platformId == undefined) return
      const data = await llmServer.getPlatformCredentials(platformId as number)
      formCredentials.value = data
    } catch (error) {
      console.log('error :>> ', error)
    }
  }

  const addAuthorizedPlatform = async () => {
    try {
      loading.value = true
      const params = {
        ...currPlatForm.value,
        testPassed: testPassed.value,
        authCredentials: getAuthCredentials()
      } as UpdateAuthorizedPlatformConfig
      await llmServer.addAuthorizedPlatform(params)
      getAuthorizedPlatforms()
      message.success('add success')
      loading.value = false
    } catch (error) {
      console.log('error :>> ', error)
    } finally {
      loading.value = false
    }
  }

  const testAuthorizedPlatform = async () => {
    try {
      loadingTest.value = true
      const params = {
        ...currPlatForm.value,
        testPassed: testPassed.value,
        authCredentials: getAuthCredentials()
      } as UpdateAuthorizedPlatformConfig
      await llmServer.testAuthorizedPlatform(params)
      message.success('test success')
    } catch (error) {
      console.log('error :>> ', error)
    } finally {
      loadingTest.value = false
    }
  }

  const onPlatformChange = () => {
    const { platformId, model } = currPlatForm.value
    const selectItems = platforms.value.find((item) => item.id === platformId)
    model != '' && (currPlatForm.value.model = '')
    getPlatformCredentials()
    autoFormRef?.value?.setOptionsVal(
      'model',
      selectItems?.supportModels as string[]
    )
  }

  const getAuthCredentials = () => {
    return formCredentials.value.map((v: PlatformCredential) => {
      return {
        key: v.name,
        value: currPlatForm.value[
          `${v.name as keyof typeof currPlatForm.value}`
        ] as string
      }
    })
  }

  const deleteAuthPlatform = () => {
    Modal.confirm({
      title: t('llmConfig.delete_authorization'),
      onOk() {
        console.log('OK')
      },
      onCancel() {
        console.log('Cancel')
      }
    })
  }

  const resetState = () => {
    loading.value = false
    currPlatForm.value = {}
    formCredentials.value = []
    platforms.value = []
  }

  return {
    loading,
    loadingTest,
    currPlatForm,
    formCredentials,
    authorizedPlatforms,
    getFormDisabled,
    getFormItems,
    getPlatforms,
    getAuthorizedPlatforms,
    getPlatformCredentials,
    addAuthorizedPlatform,
    testAuthorizedPlatform,
    deleteAuthPlatform,
    resetState
  }
}
