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

import { defineStore, storeToRefs } from 'pinia'
import { shallowRef, watch } from 'vue'
import { useClusterStore } from '@/store/cluster'
import { ServiceConfigVO } from '@/api/config/types.ts'
import { getAllConfigs, getLatestConfigs } from '@/api/config'

export const useConfigStore = defineStore(
  'config',
  () => {
    const clusterStore = useClusterStore()
    const { clusterId } = storeToRefs(clusterStore)
    const latestConfigs = shallowRef<ServiceConfigVO[]>([])
    const allConfigs = shallowRef<ServiceConfigVO[]>([])

    watch(clusterId, async () => {
      await loadLatestConfigs()
    })

    const loadLatestConfigs = async () => {
      if (clusterId.value !== 0) {
        latestConfigs.value = await getLatestConfigs(clusterId.value)
      }
    }

    watch(clusterId, async () => {
      await loadAllConfigs()
    })

    const loadAllConfigs = async () => {
      allConfigs.value = await getAllConfigs(clusterId.value)
    }

    return { latestConfigs, loadLatestConfigs, allConfigs, loadAllConfigs }
  },
  { persist: false }
)
