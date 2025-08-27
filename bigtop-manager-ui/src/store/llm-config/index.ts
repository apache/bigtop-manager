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

import * as llmServer from '@/api/llm-config/index'

import type { AuthorizedPlatform, PlatformCredential, UpdateAuthorizedPlatformConfig } from '@/api/llm-config/types'

type CurrPlatformKeys = keyof AuthorizedPlatform

type Platform = {
  id: number
  name: string
  supportModels: string[]
}

type ActiveAuthPlatform = {
  llmConfigId: string | number
  platformName: string
  model: string
}

export const useLlmConfigStore = defineStore(
  'llm-config',
  () => {
    const loading = ref<boolean>(false)
    const loadingTest = ref<boolean>(false)
    const testPassed = ref<boolean>(false)
    const platforms = ref<Platform[]>([])
    const formCredentials = ref<PlatformCredential[]>([])
    const authorizedPlatforms = ref<AuthorizedPlatform[]>([])
    const currPlatform = ref<Partial<AuthorizedPlatform>>({})
    const currAuthPlatform = shallowRef<ActiveAuthPlatform>()

    const formKeys = computed(() => formCredentials.value.map((v) => v.name))
    const isDisabled = computed(() => loading.value || loadingTest.value)
    const supportModels = computed(() => {
      const { platformId } = currPlatform.value
      return platforms.value.find((item) => item.id === platformId)?.supportModels
    })
    const authCredentials = computed(() =>
      formCredentials.value.map((v) => ({
        key: v.name,
        value: currPlatform.value[`${v.name as CurrPlatformKeys}`] as string
      }))
    )

    watch(
      () => authorizedPlatforms.value,
      (val) => {
        const active = val.filter((v) => v.status === 1)[0] as AuthorizedPlatform
        if (!active) {
          currAuthPlatform.value = undefined
          return
        }
        const { platformName, model, id } = active
        currAuthPlatform.value = { llmConfigId: id, platformName, model }
      },
      {
        deep: true
      }
    )

    const getAuthorizedPlatformConfig = () => {
      return {
        ...currPlatform.value,
        testPassed: testPassed.value,
        authCredentials: authCredentials.value
      } as UpdateAuthorizedPlatformConfig
    }

    const getAuthorizedPlatforms = async (hasLoading = true) => {
      try {
        if (hasLoading) {
          loading.value = true
        }
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
        const { platformId } = currPlatform.value
        formCredentials.value =
          typeof platformId !== 'undefined' ? await llmServer.getPlatformCredentials(platformId) : []
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    const getAuthPlatformDetail = async () => {
      try {
        const authId = currPlatform.value.id as number
        const data = await llmServer.getAuthPlatformDetail(authId)
        Object.assign(currPlatform.value, data.authCredentials)
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    const addAuthorizedPlatform = async () => {
      try {
        loading.value = true
        const authorizedPlatformConfig = getAuthorizedPlatformConfig()
        return await llmServer.addAuthorizedPlatform(authorizedPlatformConfig)
      } catch (error) {
        console.log('error :>> ', error)
        return false
      } finally {
        loading.value = false
      }
    }

    const updateAuthPlatform = async () => {
      try {
        loading.value = true
        const authorizedPlatformConfig = getAuthorizedPlatformConfig()
        return await llmServer.updateAuthPlatform(authorizedPlatformConfig)
      } catch (error) {
        console.log('error :>> ', error)
        return false
      } finally {
        loading.value = false
      }
    }

    const testAuthorizedPlatform = async () => {
      try {
        loadingTest.value = true
        const authorizedPlatformConfig = getAuthorizedPlatformConfig()
        const data = await llmServer.testAuthorizedPlatform(authorizedPlatformConfig)
        testPassed.value = data
        return data
      } catch (error) {
        console.log('error :>> ', error)
        return false
      } finally {
        loadingTest.value = false
      }
    }

    const deleteAuthPlatform = async (authId: number | string) => {
      return await llmServer.deleteAuthPlatform(authId as number)
    }

    const deactivateAuthorizedPlatform = async (authId: number | string) => {
      return await llmServer.deactivateAuthorizedPlatform(authId as number)
    }

    const activateAuthorizedPlatform = async (authId: number | string) => {
      return await llmServer.activateAuthorizedPlatform(authId as number)
    }

    const resetState = () => {
      loading.value = false
      currPlatform.value = {}
      formCredentials.value = []
      platforms.value = []
    }

    return {
      loading,
      loadingTest,
      currPlatform,
      formKeys,
      formCredentials,
      authorizedPlatforms,
      supportModels,
      platforms,
      isDisabled,
      currAuthPlatform,
      getPlatforms,
      getAuthPlatformDetail,
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
    persist: {
      storage: sessionStorage,
      paths: ['currAuthPlatform']
    }
  }
)
