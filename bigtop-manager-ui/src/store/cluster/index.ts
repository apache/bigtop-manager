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

import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { useRoute } from 'vue-router'
import { getCluster, getClusterList } from '@/api/cluster'
import { ClusterVO } from '@/api/cluster/types.ts'

export const useClusterStore = defineStore(
  'cluster',
  () => {
    const route = useRoute()
    const clusters = ref<ClusterVO[]>([])
    const currCluster = ref<ClusterVO>({})
    const clusterId = computed(() => (route.params.id as string) || undefined)

    const addCluster = async () => {
      await loadClusters()
    }

    const delCluster = async () => {
      await loadClusters()
    }

    const getClusterDetail = async () => {
      if (clusterId.value == undefined) {
        return
      }
      try {
        currCluster.value = await getCluster(parseInt(clusterId.value))
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    const loadClusters = async () => {
      clusters.value = await getClusterList()
    }

    return {
      clusters,
      currCluster,
      addCluster,
      delCluster,
      loadClusters,
      getClusterDetail
    }
  },
  {
    persist: false
    // persist: {
    //   storage: sessionStorage,
    //   paths: ['clusters',]
    // }
  }
)
