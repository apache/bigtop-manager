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
import { useRouter, RouteRecordRaw, useRoute } from 'vue-router'
import { dynamicRoutes } from '@/router/routes/index'
import { defineStore, storeToRefs } from 'pinia'
import { useClusterStore } from '@/store/cluster/index'
import { ClusterVO } from '@/api/cluster/types'

export const useMenuStore = defineStore(
  'menu',
  () => {
    const router = useRouter()
    const route = useRoute()
    const clusterStore = useClusterStore()
    const { clusters } = storeToRefs(clusterStore)

    const headerSelectedKey = ref('')
    const siderMenuSelectedKey = ref('')
    const siderMenus = ref<RouteRecordRaw[]>([])
    const dynamicRoutesParams = ref<RouteRecordRaw[]>([])
    const baseRoutesMap = ref<Map<string, RouteRecordRaw[]>>(new Map())

    const headerMenus = computed(() =>
      dynamicRoutes.filter((v) => v.meta?.title && !v.meta.hidden)
    )

    const isNoCluster = computed(
      () =>
        route.matched.at(-1)?.path.includes(':cluster/:id') &&
        clusters.value.length == 0
    )

    watch(route, () => {
      setupMenus()
    })

    function setBaseRoutesMap() {
      dynamicRoutes.forEach((route) => {
        if (!route.meta?.hidden) {
          const exist = baseRoutesMap.value.get(route.path) || []
          baseRoutesMap.value.set(route.path, [
            ...exist,
            ...(route.children || [])
          ])
        }
      })
    }

    function findActivePath(menu: RouteRecordRaw): string {
      return menu?.children ? findActivePath(menu.children[0]) : menu?.path
    }

    function createRoutesByDynamicRoutesParams() {
      dynamicRoutesParams.value = clusters.value.map((param: ClusterVO) => {
        const dynamicRoute = {
          name: `Cluster_${param.clusterName}_${param.id}`,
          path: '/cluster-mange/clusters/:cluster/:id'.replace(
            ':cluster/:id',
            `${param.clusterName}/${param.id}`
          ),
          component: () => import('@/pages/cluster-mange/cluster/detail.vue'),
          meta: { title: `${param.clusterName}` }
        }
        return dynamicRoute
      })
    }

    function injectDynamicRoutesParams() {
      siderMenus.value.forEach((v) => {
        if (v.name === 'Clusters') {
          if (dynamicRoutesParams.value.length == 0) {
            Reflect.deleteProperty(v, 'children')
          } else {
            v.children = [...dynamicRoutesParams.value]
          }
        }
      })
    }

    function setupMenus() {
      const matched = route.matched
      const activeMenus = baseRoutesMap.value.get(matched[0].path)
      const baseRoutesMapKeys = [...baseRoutesMap.value.keys()]
      const baseRoutesMapValues = [...baseRoutesMap.value.values()]
      headerSelectedKey.value = matched[0].path || baseRoutesMapKeys[0]
      siderMenus.value = matched[0].path
        ? (activeMenus as RouteRecordRaw[])
        : baseRoutesMapValues[0]
      injectDynamicRoutesParams()
      const filterPath = matched[matched.length - 1].path
      if (baseRoutesMapKeys.includes(filterPath) || isNoCluster.value) {
        siderMenuSelectedKey.value = findActivePath(siderMenus.value[0])
      } else {
        siderMenuSelectedKey.value = route.path
      }
      router.replace(siderMenuSelectedKey.value)
    }

    function onHeaderClick(key: string) {
      headerSelectedKey.value = key
      siderMenus.value = baseRoutesMap.value.get(key) || []
      injectDynamicRoutesParams()
      siderMenuSelectedKey.value = findActivePath(siderMenus.value[0])
      router.push(siderMenuSelectedKey.value)
    }

    function onSiderClick(key: string) {
      siderMenuSelectedKey.value = key
      router.push(key)
    }

    async function updateSiderMenu(isDelete = false) {
      isDelete
        ? await clusterStore.delCluster()
        : await clusterStore.addCluster()
      createRoutesByDynamicRoutesParams()
      injectDynamicRoutesParams()
      siderMenuSelectedKey.value = dynamicRoutesParams.value.at(-1)?.path || ''
      router.push(siderMenuSelectedKey.value)
    }

    function setupDynamicRoutes() {
      createRoutesByDynamicRoutesParams()
      setupMenus()
    }

    return {
      headerSelectedKey,
      siderMenuSelectedKey,
      headerMenus,
      siderMenus,
      onHeaderClick,
      onSiderClick,
      setBaseRoutesMap,
      setupDynamicRoutes,
      updateSiderMenu
    }
  },
  {
    persist: false
  }
)
