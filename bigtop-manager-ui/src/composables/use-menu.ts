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

import { onMounted, ref } from 'vue'
import { useRouter, RouteRecordRaw, useRoute } from 'vue-router'
import { dynamicRoutes } from '@/router/routes/index'

const useMenu = () => {
  const router = useRouter()
  const route = useRoute()
  const headerSelectedKey = ref('')
  const siderMenuSelectedKey = ref('')
  const headerMenus = ref<RouteRecordRaw[]>([])
  const siderMenus = ref<RouteRecordRaw[]>([])
  const dynamicRoutesParams = ref<RouteRecordRaw[]>([])
  const baseRoutesMap = ref<Map<string, RouteRecordRaw[]>>(new Map())

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

  async function setupDynamicRoutes() {
    const dynamicParams = (await new Promise((resolve) => {
      setTimeout(() => {
        resolve([
          { cluster: 'cluster1', id: '101' },
          { cluster: 'cluster2', id: '102' }
        ])
      }, 1000)
    })) as any

    dynamicRoutesParams.value = dynamicParams.map((param: any) => {
      const dynamicRoute = {
        name: `Cluster_${param.cluster}_${param.id}`,
        path: '/clusters/:cluster/:id'.replace(
          ':cluster/:id',
          `${param.cluster}/${param.id}`
        ),
        component: () => import('@/pages/cluster-mange/cluster/index.vue'),
        meta: { title: `${param.cluster}` }
      }
      return dynamicRoute
    })
    setupMenus()
  }

  function injectDynamicRoutesParams() {
    siderMenus.value.forEach((v) => {
      if (v.name === 'Clusters') {
        v.children = [...dynamicRoutesParams.value]
      }
    })
  }

  function findActivePath(menu: RouteRecordRaw): string {
    return menu.children ? findActivePath(menu.children[0]) : menu.path
  }

  function setupMenus() {
    const currheadPath = route.matched[0].path
    const activeMenus = baseRoutesMap.value.get(currheadPath)

    headerMenus.value = dynamicRoutes
    headerSelectedKey.value = currheadPath || [...baseRoutesMap.value.keys()][0]

    siderMenus.value = currheadPath
      ? (activeMenus as RouteRecordRaw[])
      : [...baseRoutesMap.value.values()][0]
    injectDynamicRoutesParams()

    const filterPath = route.matched[route.matched.length - 1].path
    if ([...baseRoutesMap.value.keys()].includes(filterPath)) {
      siderMenuSelectedKey.value = findActivePath(siderMenus.value[0])
    } else {
      siderMenuSelectedKey.value = route.path
    }

    router.push(siderMenuSelectedKey.value)
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

  onMounted(() => {
    setBaseRoutesMap()
    setupDynamicRoutes()
  })

  return {
    headerSelectedKey,
    siderMenuSelectedKey,
    headerMenus,
    siderMenus,
    onHeaderClick,
    onSiderClick,
    setupDynamicRoutes
  }
}

export default useMenu
