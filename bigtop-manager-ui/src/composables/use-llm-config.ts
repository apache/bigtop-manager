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

import { computed, ComputedRef, type Ref, ref } from 'vue'
import * as llmServer from '@/api/llm-config/index'
import { AuthorizedPlatform, PlatformCredential } from '@/api/llm-config/types'

interface Platforms {
  id: number
  name: string
  supportModels: string[]
}

interface UseLlmConfig {
  loading: Ref<boolean>
  loadingTest: Ref<boolean>
  currPlatForm: Ref<AuthorizedPlatform[] | Record<string, unknown>>
  platforms: Ref<Platforms[]>
  formCredentials: Ref<PlatformCredential[] | any[]>
  getFormDisabled: ComputedRef<boolean>
  getPlatforms: () => Promise<void>
  getPlatformCredentials: () => Promise<void>
  addAuthorizedPlatform: () => Promise<void>
  resetState: () => void
}

export function useLlmConfig(): UseLlmConfig {
  const loading = ref<boolean>(false)
  const loadingTest = ref<boolean>(false)
  const currPlatForm = ref<AuthorizedPlatform | Record<string, unknown>>({})
  const formCredentials = ref<PlatformCredential[]>([])
  const platforms = ref<Platforms[]>([])
  const getFormDisabled = computed(() => loading.value || loadingTest.value)

  const getPlatforms = async () => {
    try {
      const data = await llmServer.getPlatforms()
      platforms.value = data.map((platform) => {
        return {
          ...platform,
          supportModels: platform.supportModels.split(',')
        } as Platforms
      })
    } catch (error) {
      console.log('error :>> ', error)
    }
  }

  const getPlatformCredentials = async () => {
    try {
      const { platformId } = currPlatForm.value
      if (platformId == undefined) return
      const data = await llmServer.getPlatformCredentials(platformId as string)
      formCredentials.value = data
    } catch (error) {
      console.log('error :>> ', error)
    }
  }

  const addAuthorizedPlatform = async () => {
    try {
    } catch (error) {
      console.log('error :>> ', error)
    }
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
    platforms,
    getFormDisabled,
    getPlatforms,
    getPlatformCredentials,
    addAuthorizedPlatform,
    resetState
  }
}
