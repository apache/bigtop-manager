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
import { computed, ref } from 'vue'
import { dynamicRoutes } from '@/router/routes/index'
import { RouteRecordRaw } from 'vue-router'

export const useMenuStore = defineStore(
  'menu',
  () => {
    const currSiderRoutes = ref<RouteRecordRaw[]>([])

    const headerMenus = computed(() => {
      const map = new Map()
      dynamicRoutes.forEach((route) => {
        if (!route.meta?.hidden) {
          const key = `${route.path}-${route.meta?.title}`
          const exist = map.get(key) || []
          map.set(key, [...exist, ...(route.children || [])])
        }
      })
      return map
    })

    const setCurrSiderRoutes = (routes: RouteRecordRaw[]) => {
      currSiderRoutes.value = routes
    }

    return {
      headerMenus,
      currSiderRoutes,
      setCurrSiderRoutes
    }
  },
  {
    persist: true
  }
)
