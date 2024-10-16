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

import { defineStore, storeToRefs } from 'pinia'
import { getCurrentUser, updateUser } from '@/api/user'
import { computed, h, shallowRef } from 'vue'
import { UserReq, UserVO } from '@/api/user/types.ts'
import { MenuItem } from '@/store/user/types.ts'
import { dynamicRoutes as layoutRoutes } from '@/router/routes/index'
import { useClusterStore } from '@/store/cluster'
import { RouteRecordRaw } from 'vue-router'
import { useServiceStore } from '@/store/service'
import SvgIcon from '@/components/common/svg-icon/index.vue'
import { routePriorityMap } from '@/utils/router-util'

export const useUserStore = defineStore(
  'user',
  () => {
    const userVO = shallowRef<UserVO>()

    const clusterStore = useClusterStore()
    const serviceStore = useServiceStore()
    const { selectedCluster } = storeToRefs(clusterStore)
    const { installedServices } = storeToRefs(serviceStore)

    const initMenu = (routes: RouteRecordRaw[]) => {
      const items: MenuItem[] = []
      routes.forEach((route) => {
        // filter invaible route
        if (routePriorityMap[`${route.meta?.title}`] == -1) {
          return
        }
        const menuItem: MenuItem = {
          key: route.meta?.title?.toLowerCase(),
          to: route.path,
          ...route.meta,
          hidden: Boolean(route.meta?.hidden),
          priority: routePriorityMap[`${route.meta?.title}`] || -1
        }

        if (route.meta?.title === 'Services') {
          menuItem.children = []
          installedServices.value.forEach((service) => {
            const color = service.isHealthy ? '#52c41a' : '#f5222d'
            menuItem.children?.push({
              key: service.serviceName,
              to: '/services/' + service.serviceName,
              title: service.displayName,
              icon: h(SvgIcon, {
                name: 'circle-filled',
                style: `font-size: 8px; color: ${color}; vertical-align:inherit`
              })
            })
          })
        } else if (route.children !== undefined && route?.meta && !route?.meta.alwaysShow) {
          menuItem.children = []
          route.children.forEach((child) => {
            menuItem.children?.push({
              key: child.meta?.title?.toLowerCase(),
              to: route.path + child.path,
              title: child.meta?.title,
              icon: child.meta?.icon
            })
          })
        } else {
        }

        items.push(menuItem)
      })

      return items
    }

    const menuItems = computed(() => {
      if (selectedCluster.value) {
        return initMenu(layoutRoutes)
      } else {
        return initMenu([])
      }
    })

    const getUserInfo = async () => {
      userVO.value = await getCurrentUser()
    }

    const updateUserInfo = async (editUser: UserReq) => {
      await updateUser(editUser)
      await getUserInfo()
    }

    const logout = async () => {
      userVO.value = undefined
      localStorage.removeItem('Token')
      sessionStorage.removeItem('Token')
    }

    return {
      userVO,
      menuItems,
      getUserInfo,
      updateUserInfo,
      logout
    }
  },
  {
    persist: false
  }
)
