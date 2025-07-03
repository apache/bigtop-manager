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
import { getService, getServiceList } from '@/api/service'
import type { ServiceListParams, ServiceVO } from '@/api/service/types'

export const useServiceStore = defineStore(
  'service',
  () => {
    const services = ref<ServiceVO[]>([])
    const serviceMap = ref<Record<string, (ServiceVO & { clusterId: number })[]>>({})
    const total = ref(0)
    const loading = ref(false)
    const serviceNames = computed(() => services.value.map((v) => v.name))
    const serviceFlatMap = computed(() => {
      const result: Record<string, ServiceVO> = {}

      for (const services of Object.values(serviceMap.value)) {
        for (const service of services) {
          result[service.id!] = service
        }
      }
      return result
    })

    const getServices = async (clusterId: number, filterParams?: ServiceListParams) => {
      try {
        loading.value = true
        const data = await getServiceList(clusterId, { ...filterParams, pageNum: 1, pageSize: 100 })
        services.value = data.content
        total.value = data.total
        serviceMap.value[clusterId] = data.content.map((service) => ({ ...service, clusterId }))
      } catch (error) {
        serviceMap.value = {}
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

    const getServicesOfInfra = async () => {
      await getServices(0)
    }

    const getInstalledNamesOrIdsOfServiceByKey = (key: string, flag: 'names' | 'ids' = 'names') => {
      return Object.values(serviceMap.value[key] || {}).map((service: ServiceVO) => {
        if (flag === 'ids') {
          return service.id
        } else {
          return service.name
        }
      })
    }

    const getInstalledServicesDetailByKey = async (key: string): Promise<ServiceVO[] | undefined> => {
      try {
        const serviceIds = getInstalledNamesOrIdsOfServiceByKey(key, 'ids')
        const allDetail = serviceIds?.map((id) => getServiceDetail(Number(key), Number(id))) as Promise<ServiceVO>[]
        return await Promise.all(allDetail)
      } catch (error) {
        console.log(error)
      }
    }

    return {
      serviceMap,
      services,
      loading,
      serviceNames,
      serviceFlatMap,
      getServices,
      getServiceDetail,
      getServicesOfInfra,
      getInstalledServicesDetailByKey,
      getInstalledNamesOrIdsOfServiceByKey
    }
  },
  {
    persist: {
      storage: sessionStorage,
      paths: ['serviceMap']
    }
  }
)
