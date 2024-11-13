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

import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import * as llmServer from '@/api/llm-config/index'

import type {
  AuthorizedPlatform,
  PlatformCredential,
  UpdateAuthorizedPlatformConfig
} from '@/api/llm-config/types'

import type { FormState } from '@/components/common/auto-form/types'

interface Platform {
  id: number
  name: string
  supportModels: string[]
}

type BaseType = Record<string, unknown>

export const useLlmConfigStore = defineStore(
  'llmConfig',
  () => {
    const loading = ref<boolean>(false)
    const loadingTest = ref<boolean>(false)
    const testPassed = ref<boolean>(false)
    const platforms = ref<Platform[]>([])
    const formCredentials = ref<PlatformCredential[]>([])
    const authorizedPlatforms = ref<AuthorizedPlatform[]>([])
    const currPlatForm = ref<FormState<AuthorizedPlatform | BaseType>>({})
    const getFormDisabled = computed(() => loading.value || loadingTest.value)

    const getAuthorizedPlatforms = async () => {
      loading.value = true
      try {
        authorizedPlatforms.value = await llmServer.getAuthorizedPlatforms()
      } catch (error) {
        console.log('error :>> ', error)
      } finally {
        loading.value = false
      }
    }

    const getPlatforms = async () => {
      const data = await llmServer.getPlatforms()
      platforms.value = data.map((platform) => {
        return {
          ...platform,
          supportModels: platform.supportModels.split(',')
        } as Platform
      })
    }

    const getPlatformCredentials = async () => {
      try {
        const platformId = currPlatForm.value.platformId as number
        formCredentials.value =
          await llmServer.getPlatformCredentials(platformId)
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    const addAuthorizedPlatform = async () => {
      loading.value = true
      const params = getAuthorizedPlatformConfig()
      try {
        return await llmServer.addAuthorizedPlatform(params)
      } catch (error) {
        console.log('error :>> ', error)
        return false
      } finally {
        loading.value = false
      }
    }

    const updateAuthPlatform = async () => {
      loading.value = true
      const params = getAuthorizedPlatformConfig()
      try {
        return await llmServer.updateAuthPlatform(params)
      } catch (error) {
        console.log('error :>> ', error)
        return false
      } finally {
        loading.value = false
      }
    }

    const testAuthorizedPlatform = async () => {
      loadingTest.value = true
      const params = getAuthorizedPlatformConfig()
      try {
        return await llmServer.testAuthorizedPlatform(params)
      } catch (error) {
        console.log('error :>> ', error)
        return false
      } finally {
        loadingTest.value = false
      }
    }

    const deleteAuthPlatform = async (authId: number) => {
      return await llmServer.deleteAuthPlatform(authId)
    }

    const deactivateAuthorizedPlatform = async (authId: number) => {
      return await llmServer.deactivateAuthorizedPlatform(authId)
    }

    const activateAuthorizedPlatform = async (authId: number) => {
      return await llmServer.activateAuthorizedPlatform(authId)
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

    const getAuthorizedPlatformConfig = () => {
      return {
        ...currPlatForm.value,
        testPassed: testPassed.value,
        authCredentials: getAuthCredentials()
      } as UpdateAuthorizedPlatformConfig
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
      platforms,
      getFormDisabled,
      getPlatforms,
      getAuthorizedPlatforms,
      getPlatformCredentials,
      addAuthorizedPlatform,
      updateAuthPlatform,
      testAuthorizedPlatform,
      deleteAuthPlatform,
      deactivateAuthorizedPlatform,
      activateAuthorizedPlatform,
      resetState
    }
  },
  {
    persist: false
  }
)
