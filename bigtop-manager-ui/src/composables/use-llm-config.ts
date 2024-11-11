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

import { type Ref, ref } from 'vue'
import * as llmServer from '@/api/llm-config/index'

interface Platforms {
  id: number
  name: string
  supportModels: string[]
}

interface UseLlmConfig {
  loading: Ref<boolean>
  platforms: Ref<Platforms[]>
  getPlatforms: () => Promise<void>
}

export function useLlmConfig(): UseLlmConfig {
  const loading = ref<boolean>(false)
  const platforms = ref<Platforms[]>([])

  const getPlatforms = async () => {
    try {
      const data = await llmServer.getPlatforms()
      platforms.value = data.map((platform) => {
        return {
          ...platform,
          supportModels: platform.supportModels.split(',')
        } as Platforms
      })
    } catch (err) {
      console.log('error :>> ', err)
    }
  }

  return { loading, platforms, getPlatforms }
}
