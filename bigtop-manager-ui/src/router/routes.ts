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

import { h } from 'vue'
import { RouteRecordRaw } from 'vue-router'
import {
  AppstoreOutlined,
  DesktopOutlined,
  PieChartOutlined,
  SettingOutlined,
  UserOutlined,
  ProfileOutlined,
  BarsOutlined
} from '@ant-design/icons-vue'

const initialRoutes: RouteRecordRaw[] = [
  {
    path: '/dashboard',
    component: () => import('@/pages/dashboard/index.vue'),
    meta: {
      title: 'Dashboard',
      icon: h(PieChartOutlined)
    }
  }
]

const layoutRoutes: RouteRecordRaw[] = [
  ...initialRoutes,
  {
    path: '/hosts',
    component: () => import('@/pages/hosts/index.vue'),
    meta: {
      title: 'Hosts',
      icon: h(DesktopOutlined)
    }
  },
  {
    name: 'services',
    path: '/services/:serviceName',
    component: () => import('@/pages/service/index.vue'),
    meta: {
      title: 'Services',
      icon: h(AppstoreOutlined)
    }
  },
  {
    path: '/cluster/',
    meta: {
      title: 'Cluster',
      icon: h(SettingOutlined)
    },
    children: [
      {
        path: 'stack',
        component: () => import('@/pages/cluster/stack/index.vue'),
        meta: {
          title: 'Stack',
          icon: h(BarsOutlined)
        }
      },
      {
        path: 'account',
        component: () => import('@/pages/cluster/account/index.vue'),
        meta: {
          title: 'Account',
          icon: h(UserOutlined)
        }
      }
    ]
  }
]

const notDisplayedRoutes = [
  {
    path: '/user/',
    meta: {
      title: 'user',
      icon: h(UserOutlined)
    },
    children: [
      {
        path: 'profile',
        component: () => import('@/pages/user/profile/index.vue'),
        meta: {
          title: 'Profile',
          icon: h(ProfileOutlined)
        }
      },
      {
        path: 'settings',
        component: () => import('@/pages/user/settings/index.vue'),
        meta: {
          title: 'Settings',
          icon: h(SettingOutlined)
        }
      }
    ]
  }
]

const routes: RouteRecordRaw[] = [
  { path: '/login', component: () => import('@/pages/login/index.vue') },
  {
    path: '/',
    redirect: '/dashboard',
    component: () => import('@/layouts/index.vue'),
    children: [...layoutRoutes, ...notDisplayedRoutes]
  },
  {
    path: '/:pathMatch(.*)',
    component: () => import('@/pages/error/404.vue')
  }
]

export { initialRoutes, layoutRoutes }
export default routes
