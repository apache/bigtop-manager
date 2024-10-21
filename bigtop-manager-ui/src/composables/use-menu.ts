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

import { ref, watch, onMounted, computed } from 'vue'
import { RouteRecordRaw, useRoute, useRouter } from 'vue-router'
import { dynamicRoutes } from '@/router/routes/index'

export function useNavigation() {
  const route = useRoute()
  const router = useRouter()
  const headerSelectedKey = ref('')
  const sideMenuSelectedKey = ref('')
  const headerMenus = ref<RouteRecordRaw[]>(dynamicRoutes)
  const dynamicParams = ref<{ cluster: string; id: string }[]>([])

  const siderMenus = computed(() => {
    const res = headerMenus.value.filter(
      (v) => v.path === headerSelectedKey.value
    )
    return res[0]?.children ? res[0].children : []
  })

  function onHeaderClick({ key }: any) {
    headerSelectedKey.value = key
    const children = siderMenus.value[0]?.children
    if (children && children.length > 0) {
      sideMenuSelectedKey.value = children[0].path
    } else {
      sideMenuSelectedKey.value = siderMenus.value[0].path
    }
    router.push({ path: sideMenuSelectedKey.value })
  }

  function onSiderClick({ key }: any) {
    sideMenuSelectedKey.value = key
    router.push({ path: key })
  }

  async function updateDynamicRoutes() {
    await fetchDynamicParams()
    const routesList = dynamicParams.value.map((param) => {
      const route = {
        name: `Cluster-${param.cluster}`,
        path: `/cluster-mange/clusters/${param.cluster}/${param.id}`,
        component: () => import('@/pages/cluster-mange/cluster/index.vue'),
        meta: {
          title: param.cluster
        }
      }
      router.addRoute('Clusters', route)
      return route
    })
    router.options.routes.forEach((v) => {
      if (v.path == '/cluster-mange/') {
        v.children && (v.children[0].children = routesList)
      }
    })
    headerMenus.value = [
      ...router.options.routes.filter((v) =>
        dynamicRoutes.map((v) => v.path).includes(v.path)
      )
    ]
  }

  async function fetchDynamicParams() {
    try {
      const response = await new Promise((resolve) => {
        setTimeout(() => {
          return resolve([
            { cluster: 'test1', id: '1' },
            { cluster: 'test2', id: '2' }
          ])
        })
      })
      dynamicParams.value = response as any
    } catch (error) {
      console.error('Error fetching dynamic params:', error)
    }
  }

  onMounted(async () => {
    await updateDynamicRoutes()
    headerSelectedKey.value =
      route.matched[0]?.path || headerMenus.value[0].path
    if (siderMenus.value && siderMenus.value[0]) {
      const children = siderMenus.value[0]?.children
      if (children && children.length > 0) {
        sideMenuSelectedKey.value = children[0].path
      } else {
        sideMenuSelectedKey.value = siderMenus.value[0].path
      }
    }

    router.push({ path: sideMenuSelectedKey.value })
  })

  watch(route, (newRoute) => {
    sideMenuSelectedKey.value =
      newRoute.matched[newRoute.matched.length - 1].path
  })

  return {
    headerSelectedKey,
    sideMenuSelectedKey,
    headerMenus,
    siderMenus,
    onHeaderClick,
    onSiderClick
  }
}
