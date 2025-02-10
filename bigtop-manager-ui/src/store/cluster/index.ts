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
import { ClusterVO } from '@/api/cluster/types.ts'
import { ref } from 'vue'
import { getClusterList } from '@/api/cluster'

export const useClusterStore = defineStore(
  'cluster',
  () => {
    const clusters = ref<ClusterVO[]>([])
    const count = ref(0)

    const addCluster = async () => {
      await loadClusters()
    }

    const delCluster = async () => {
      if (count.value < 0) {
        count.value = 0
        return
      }
      count.value = count.value - 1
      await loadClusters()
    }

    const loadClusters = async () => {
      const data = await getClusterList()
      clusters.value = data
    }

    return {
      clusters,
      count,
      addCluster,
      delCluster,
      loadClusters
    }
  },
  {
    persist: false
    // persist: {
    //   storage: sessionStorage,
    //   paths: ['clusters', 'count']
    // }
  }
)
