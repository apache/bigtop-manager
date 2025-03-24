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

import { computed, ref, watch } from 'vue'
import { defineStore } from 'pinia'
import { useRoute } from 'vue-router'
import { getCluster, getClusterList } from '@/api/cluster'
import { useServiceStore } from '@/store/service'
import { useInstalledStore } from '@/store/installed'
import type { ClusterVO } from '@/api/cluster/types.ts'

export const useClusterStore = defineStore(
  'cluster',
  () => {
    const route = useRoute()
    const installedStore = useInstalledStore()
    const serviceStore = useServiceStore()
    const clusters = ref<ClusterVO[]>([])
    const loading = ref(false)
    const currCluster = ref<ClusterVO>({})
    const clusterId = computed(() => (route.params.id as string) || undefined)

    watch(
      () => clusters.value,
      (val) => {
        val.forEach((cluster) => {
          installedStore.setInstalledMapKey(`${cluster.id}`)
        })
      }
    )

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
        loading.value = true
        currCluster.value = await getCluster(parseInt(clusterId.value))
        currCluster.value.id != undefined && (await serviceStore.getServices(currCluster.value.id))
      } catch (error) {
        console.log('error :>> ', error)
      } finally {
        loading.value = false
      }
    }

    const loadClusters = async () => {
      clusters.value = await getClusterList()
    }

    return {
      clusters,
      loading,
      currCluster,
      addCluster,
      delCluster,
      loadClusters,
      getClusterDetail
    }
  },
  {
    persist: {
      storage: sessionStorage,
      paths: ['clusters']
    }
  }
)
