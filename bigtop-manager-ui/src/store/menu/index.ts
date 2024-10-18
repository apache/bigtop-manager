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
import { computed, reactive, ref, toRaw } from 'vue'
import { dynamicRoutes } from '@/router/routes/index'
import { useClusterStore } from '@/store/cluster/index'
import { type MenuItem } from './types'
import { type RouteRecordRaw, useRouter } from 'vue-router'

export const useMenuStore = defineStore(
  'menu',
  () => {
    const router = useRouter()
    const clusterStore = useClusterStore()
    const reactiveRoutes = reactive(dynamicRoutes)
    const currSiderRoutes = ref<RouteRecordRaw[]>([])
    const siderMenus = computed(() => initSiderMenu(currSiderRoutes.value))
    const headerMenus = computed(() => splitDynamicRoutesToCreateHeaderMenus())

    const splitDynamicRoutesToCreateHeaderMenus = () => {
      const map = new Map()
      reactiveRoutes.forEach((route) => {
        if (!route.meta?.hidden) {
          const key = `${route.path}_${route.meta?.title}`
          const exist = map.get(key) || []
          map.set(key, [...exist, ...(route.children || [])])
        }
      })
      return map
    }

    const updateSiderRoutes = (selectedKeys: string[], to?: string) => {
      const res = [...toRaw(headerMenus.value).keys()].find((v) =>
        v.includes(selectedKeys[0])
      )
      currSiderRoutes.value = headerMenus.value.get(res) || []
      router.push({ path: to || selectedKeys[0] })
    }

    const initSiderMenuItem = (route: RouteRecordRaw): MenuItem => {
      const menuItem: MenuItem = {
        ...route.meta,
        key: route.name === 'Clusters' ? route.path.split('/')[2] : route.path,
        to: route.path,
        label: route.meta?.title || '',
        hidden: Boolean(route.meta?.hidden)
      }
      if (route.children !== undefined || route.name === 'Clusters') {
        menuItem.children = []
      }
      return menuItem
    }

    const initSiderMenu = (routes: RouteRecordRaw[]) => {
      return routes.reduce((pre, route) => {
        const menuItem = initSiderMenuItem(route)
        if (route.name === 'Clusters') {
          clusterStore.clusters.forEach((cluster) => {
            menuItem.to = menuItem.to.replace(
              ':cluster',
              `${menuItem?.clusterName}/${clusterStore.clusters[0].id}`
            )
            menuItem.children?.push({
              key: cluster?.clusterName,
              to: route.path.replace(':cluster', `${cluster?.clusterName}`),
              title: cluster?.clusterName,
              label: cluster?.clusterName
            })
          })
        } else if (route.children !== undefined) {
          route.children.forEach((child) => {
            menuItem.children?.push({
              key: child.path,
              to: `${route.path}${child.path}`,
              title: child.meta?.title,
              icon: child.meta?.icon,
              label: child.meta?.title || ''
            })
          })
        } else {
          menuItem.key = route.path.split('/').at(-1)
        }
        pre.push(menuItem)
        return pre
      }, [] as MenuItem[])
    }

    return {
      siderMenus,
      headerMenus,
      updateSiderRoutes
    }
  },
  {
    persist: false
  }
)
