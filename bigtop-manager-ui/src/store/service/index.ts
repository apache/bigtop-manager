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
import { computed, ref } from 'vue'
import { getService, getServiceList } from '@/api/service'
import { useStackStore } from '@/store/stack'
import { InstalledMapItem, useInstalledStore } from '@/store/installed'
import type { ServiceListParams, ServiceVO } from '@/api/service/types'

export const useServiceStore = defineStore(
  'service',
  () => {
    const stackStore = useStackStore()
    const installedStore = useInstalledStore()
    const services = ref<ServiceVO[]>([])
    const total = ref(0)
    const loading = ref(false)
    const { stacks } = storeToRefs(stackStore)
    const serviceNames = computed(() => services.value.map((v) => v.name))
    const locateStackWithService = computed(() => {
      return stacks.value.filter((item) =>
        item.services.some((service) => service.name && serviceNames.value.includes(service.name))
      )
    })

    const getServices = async (clusterId: number, filterParams?: ServiceListParams) => {
      try {
        loading.value = true
        const data = await getServiceList(clusterId, { ...filterParams, pageNum: 1, pageSize: 100 })
        services.value = data.content
        total.value = data.total
        const serviceMap = services.value.map((v) => ({
          serviceId: v.id,
          serviceName: v.name,
          serviceDisplayName: v.displayName,
          clusterId: clusterId
        })) as InstalledMapItem[]
        installedStore.setInstalledMapKeyOfValue(`${clusterId}`, serviceMap)
      } catch (error) {
        console.log('error :>> ', error)
      } finally {
        loading.value = false
      }
    }

    const getServiceDetail = async (clusterId: number, serviceId: number) => {
      try {
        return await getService({ clusterId, id: serviceId })
      } catch (error) {
        console.log(error)
      }
    }

    return {
      services,
      loading,
      getServices,
      getServiceDetail,
      serviceNames,
      locateStackWithService
    }
  },
  {
    persist: false
  }
)
