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
import { ref, watch } from 'vue'
import { HostVO } from '@/api/hosts/types.ts'
import { getHosts } from '@/api/hosts'
import { formatFromByte } from '@/utils/storage.ts'
import { useClusterStore } from '@/store/cluster'
import { MONITOR_SCHEDULE_INTERVAL } from '@/utils/constant.ts'
import { useIntervalFn, Pausable } from '@vueuse/core'

export const useHostStore = defineStore(
  'host',
  () => {
    const clusterStore = useClusterStore()

    const { clusterId } = storeToRefs(clusterStore)
    const hosts = ref<HostVO[]>([])
    const loading = ref<boolean>(true)
    const intervalFn = ref<Pausable | undefined>()

    watch(clusterId, async () => {
      await refreshHosts()
    })

    const refreshHosts = async () => {
      if (clusterId.value !== 0) {
        const res = await getHosts(clusterId.value)
        hosts.value = res.map((v: HostVO) => {
          v.totalMemorySize = formatFromByte(parseInt(v.totalMemorySize))
          v.freeMemorySize = formatFromByte(parseInt(v.freeMemorySize))
          v.freeDisk = formatFromByte(parseInt(v.freeDisk))
          v.totalDisk = formatFromByte(parseInt(v.totalDisk))

          return v
        })

        if (loading.value) {
          loading.value = false
        }
      }
    }

    intervalFn.value = useIntervalFn(
      async () => {
        await refreshHosts()
      },
      MONITOR_SCHEDULE_INTERVAL,
      { immediate: false, immediateCallback: true }
    )

    const resumeIntervalFn = () => intervalFn.value?.resume()
    const pauseIntervalFn = () => intervalFn.value?.pause()

    return { hosts, loading, refreshHosts, resumeIntervalFn, pauseIntervalFn }
  },
  { persist: false }
)
