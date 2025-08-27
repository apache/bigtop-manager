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

import { dynamicRoutes as dr } from '@/router/routes/index'
import { useClusterStore } from '../cluster'

import type { RouteRecordRaw } from 'vue-router'

export const useMenuStore = defineStore(
  'menu',
  () => {
    const router = useRouter()
    const route = useRoute()
    const clusterStore = useClusterStore()

    const baseRoutesMap = ref<Record<string, RouteRecordRaw>>({})
    const headerMenus = ref<RouteRecordRaw[]>([])
    const siderMenus = ref<RouteRecordRaw[]>([])
    const headerSelectedKey = ref()
    const siderMenuSelectedKey = ref()
    const routePathFromClusters = shallowRef('/cluster-manage/clusters')

    const clusterList = computed(() => Object.values(clusterStore.clusterMap))

    /**
     * Build the mapping of base routes from dynamic routes.
     */
    const buildMenuMap = () => {
      baseRoutesMap.value = dr.reduce((buildRes, { path, name, meta, children }) => {
        if (!meta?.hidden) {
          buildRes[path] = { path, name, meta, children }
        }
        return buildRes
      }, {})
    }

    /**
     * Setup the header menu based on the current route.
     */
    const setupHeader = () => {
      if (route.matched[0].path === '/login') {
        headerSelectedKey.value = '/cluster-manage'
      } else {
        headerSelectedKey.value = route.matched[0].path ?? '/cluster-manage'
      }
      headerMenus.value = Object.values(baseRoutesMap.value)
      siderMenus.value = baseRoutesMap.value[headerSelectedKey.value]?.children || []
    }

    /**
     * Setup the sider menu and handle cluster-based navigation.
     */
    const setupSider = async () => {
      siderMenus.value = baseRoutesMap.value[headerSelectedKey.value]?.children || []

      // If the first sider menu has a redirect, set it as the selected key
      if (siderMenus.value[0]?.redirect) {
        siderMenuSelectedKey.value = siderMenus.value[0].redirect
        return
      }

      const targetPath =
        clusterList.value.length > 0
          ? `${routePathFromClusters.value}/${clusterList.value[0].id}`
          : `${routePathFromClusters.value}/default`

      navigateToSider(targetPath)
    }

    /**
     * Navigate to a specific sider menu path.
     * @param path - The path to navigate to.
     */
    const navigateToSider = (path: string) => {
      router.push({ path })
    }

    const onHeaderClick = (key: string) => {
      headerSelectedKey.value = key
      router.push({ path: key })
      setupSider()
    }

    const onSiderClick = (key: string) => {
      router.push({ path: key })
    }

    /**
     * Update the sider menu based on the last cluster in the list.
     */
    const updateSider = async () => {
      try {
        await clusterStore.loadClusters()
        const lastClusterId = clusterList.value[clusterList.value.length - 1]?.id
        if (lastClusterId) {
          await nextTick()
          navigateToSider(`${routePathFromClusters.value}/${lastClusterId}`)
        }
      } catch (error) {
        console.error('Error updating sider menu:', error)
      }
    }

    /**
     * Initialize the menu by setting up the header and sider menus.
     */
    const setupMenu = () => {
      buildMenuMap()
      setupHeader()
      setupSider()
    }

    const $reset = () => {
      headerMenus.value = []
      siderMenus.value = []
      headerSelectedKey.value = undefined
      siderMenuSelectedKey.value = undefined
      baseRoutesMap.value = {}
      clusterStore.$reset()
    }

    return {
      headerMenus,
      siderMenus,
      headerSelectedKey,
      siderMenuSelectedKey,
      routePathFromClusters,
      baseRoutesMap,
      setupMenu,
      setupSider,
      updateSider,
      onHeaderClick,
      onSiderClick,
      reset: $reset
    }
  },
  {
    persist: {
      storage: localStorage,
      paths: [
        'headerMenus',
        'siderMenus',
        'siderMenuSelectedKey',
        'headerSelectedKey',
        'routePathFromClusters',
        'baseRoutesMap'
      ]
    }
  }
)
