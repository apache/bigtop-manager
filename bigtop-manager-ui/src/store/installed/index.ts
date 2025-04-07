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
import { ref } from 'vue'
import { useServiceStore } from '@/store/service'
import type { ServiceVO } from '@/api/service/types.ts'

export interface InstalledMapItem {
  serviceId: number
  serviceName: string
  serviceDisplayName: string
  clusterId: number
}

export const useInstalledStore = defineStore(
  'installed',
  () => {
    const serviceStore = useServiceStore()
    const installedServiceMap = ref<Record<string, InstalledMapItem[]>>({})

    const getInstalledNamesOrIdsOfServiceByKey = (key: string, flag: 'names' | 'ids' = 'names') => {
      return installedServiceMap.value[key].map((value) => {
        if (flag === 'ids') {
          return value.serviceId
        } else {
          return value.serviceName
        }
      })
    }

    const setInstalledMapKey = (key: string) => {
      installedServiceMap.value[key] = []
    }

    const setInstalledMapKeyOfValue = (key: string, value: InstalledMapItem[]) => {
      installedServiceMap.value[key] = value
    }

    const getServicesOfInfra = async () => {
      await serviceStore.getServices(0)
    }

    const getInstalledServicesDetailByKey = async (key: string): Promise<ServiceVO[] | undefined> => {
      try {
        const serviceIds = getInstalledNamesOrIdsOfServiceByKey(key, 'ids')
        const allDetail = serviceIds?.map((id) =>
          serviceStore.getServiceDetail(Number(key), Number(id))
        ) as Promise<ServiceVO>[]
        return await Promise.all(allDetail)
      } catch (error) {
        console.log(error)
      }
    }

    return {
      serviceStore,
      installedServiceMap,
      getServicesOfInfra,
      setInstalledMapKey,
      setInstalledMapKeyOfValue,
      getInstalledNamesOrIdsOfServiceByKey,
      getInstalledServicesDetailByKey
    }
  },
  {
    persist: {
      storage: sessionStorage,
      key: 'installed',
      paths: ['installedServiceMap']
    }
  }
)
