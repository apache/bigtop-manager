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
import { useServiceStore } from '@/store/service'
import type { ClusterVO } from '@/api/cluster/types.ts'

export const useClusterStore = defineStore(
  'cluster',
  () => {
    const route = useRoute()
    const serviceStore = useServiceStore()
    const loading = ref(false)
    const clusters = ref<ClusterVO[]>([])
    const currCluster = ref<ClusterVO>({})
    const clusterMap = ref<Record<string, ClusterVO>>({})
    const clusterId = computed(() => (route.params.id as string) || undefined)
    const clusterCount = computed(() => Object.values(clusterMap.value).length)

    const loadClusters = async () => {
      try {
        const clusterList = await getClusterList()
        clusterMap.value = clusterList.reduce(
          (pre, cluster) => {
            pre[cluster.id!] = cluster
            return pre
          },
          {} as Record<string, ClusterVO>
        )
      } catch (error) {
        clusterMap.value = {}
        console.log('error :>> ', error)
      }
    }

    const getClusterDetail = async () => {
      if (clusterId.value == undefined) {
        currCluster.value = {}
        return
      }
      try {
        loading.value = true
        currCluster.value = await getCluster(Number(clusterId.value))
        await serviceStore.getServices(Number(clusterId.value))
      } catch (error) {
        currCluster.value = {}
        console.log('error :>> ', error)
      } finally {
        loading.value = false
      }
    }

    const addCluster = async () => {
      await loadClusters()
    }

    const delCluster = async () => {
      await loadClusters()
    }

    return {
      clusters,
      clusterMap,
      loading,
      currCluster,
      clusterCount,
      loadClusters,
      getClusterDetail,
      addCluster,
      delCluster
    }
  },
  {
    persist: {
      storage: sessionStorage,
      paths: ['clusterMap']
    }
  }
)
