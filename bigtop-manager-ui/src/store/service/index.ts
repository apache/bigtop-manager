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
import { useRoute } from 'vue-router'
import { getServiceList } from '@/api/service'
import { useStackStore } from '@/store/stack/index'
import type { ServiceVO } from '@/api/service/types'

export const useServiceStore = defineStore(
  'service',
  () => {
    const route = useRoute()
    const stackStore = useStackStore()
    const services = ref<ServiceVO[]>([])
    const total = ref(0)
    const loading = ref(false)
    const { stacks } = storeToRefs(stackStore)

    const serviceNames = computed(() => services.value.map((v) => v.name))
    const locateStackWithService = computed(() =>
      stacks.value.filter((item) =>
        item.services.some((service) => service.name && serviceNames.value.includes(service.name))
      )
    )

    const getServices = async () => {
      try {
        loading.value = true
        const params = route.params as unknown as { id: string; clusterName: string }
        const data = await getServiceList(parseInt(params.id))
        services.value = data.content
        total.value = data.total
      } catch (error) {
        console.log('error :>> ', error)
      } finally {
        loading.value = false
      }
    }

    return {
      services,
      getServices,
      locateStackWithService
    }
  },
  {
    persist: false
  }
)
