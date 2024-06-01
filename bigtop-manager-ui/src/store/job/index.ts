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
import { computed, ref, shallowRef, watch } from 'vue'
import { useClusterStore } from '@/store/cluster'
import { JobVO } from '@/api/job/types.ts'
import { getJobs } from '@/api/job'
import { Pausable, useIntervalFn } from '@vueuse/core'
import { MONITOR_SCHEDULE_INTERVAL } from '@/utils/constant.ts'

export const useJobStore = defineStore(
  'job',
  () => {
    const clusterStore = useClusterStore()
    const { clusterId } = storeToRefs(clusterStore)
    const jobs = shallowRef<JobVO[]>([])
    const loadingJobs = ref<boolean>(true)
    const intervalFn = ref<Pausable | undefined>()

    const processJobNum = computed(() => {
      return jobs.value.filter(
        (job: JobVO) => job.state === 'Processing' || job.state === 'Pending'
      ).length
    })

    const loadJobs = async () => {
      const { content } = await getJobs(clusterId.value, {
        pageNum: 1,
        pageSize: 10
      })
      jobs.value = content
    }

    watch(clusterId, async () => {
      if (clusterId.value != 0) {
        loadingJobs.value = true
        await loadJobs()
      }
    })

    intervalFn.value = useIntervalFn(
      async () => {
        await loadJobs()
      },
      MONITOR_SCHEDULE_INTERVAL,
      { immediate: false, immediateCallback: true }
    )
    const resumeIntervalFn = () => intervalFn.value?.resume()
    const pauseIntervalFn = () => intervalFn.value?.pause()

    return {
      jobs,
      processJobNum,
      loadingJobs,
      loadJobs,
      resumeIntervalFn,
      pauseIntervalFn
    }
  },
  { persist: false }
)
