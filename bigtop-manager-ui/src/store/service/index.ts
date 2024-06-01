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
import { computed, ref, watch } from 'vue'
import { ServiceVO } from '@/api/service/types.ts'
import { useClusterStore } from '@/store/cluster'
import { getService } from '@/api/service'
import { MergedServiceVO } from '@/store/service/types.ts'
import { useStackStore } from '@/store/stack'

export const useServiceStore = defineStore(
  'service',
  () => {
    const clusterStore = useClusterStore()
    const stackStore = useStackStore()
    const { clusterId } = storeToRefs(clusterStore)
    const { currentStack } = storeToRefs(stackStore)

    const installedServices = ref<ServiceVO[]>([])
    const loadingServices = ref<boolean>(true)
    const mergedServices = computed(() => {
      const mergedServices: MergedServiceVO[] = []
      const installedServiceNames: string[] = []
      installedServices.value.forEach((service) => {
        mergedServices.push({
          installed: true,
          ...service
        })

        installedServiceNames.push(service.serviceName)
      })

      currentStack.value?.services.forEach((service) => {
        if (!installedServiceNames.includes(service.serviceName)) {
          mergedServices.push({
            installed: false,
            ...service
          })
        }
      })

      return mergedServices
    })

    const loadServices = async () => {
      if (clusterId.value != 0) {
        installedServices.value = await getService(clusterId.value)
        loadingServices.value = false
      }
    }

    watch(clusterId, async () => {
      if (clusterId.value != 0) {
        loadingServices.value = true
        await loadServices()
      }
    })

    return {
      installedServices,
      loadingServices,
      mergedServices,
      loadServices
    }
  },
  { persist: false }
)
