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

import { RouteRecordRaw } from 'vue-router'
import pageView from '@/layouts/index.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/cluster-mange',
    component: pageView,
    redirect: '/cluster-mange/clusters',
    meta: {
      title: 'menu.cluster'
    },
    children: [
      {
        name: 'Default',
        path: 'default',
        component: () => import('@/layouts/default.vue'),
        meta: {
          hidden: true
        }
      },
      {
        name: 'Clusters',
        path: 'clusters',
        redirect: '',
        meta: {
          icon: 'clusters',
          title: 'menu.cluster'
        },
        children: [
          {
            name: 'ClusterDetail',
            path: ':cluster/:id',
            component: () => import('@/pages/cluster-mange/cluster/index.vue'),
            meta: {
              hidden: true
            }
          },
          {
            name: 'ClusterCreate',
            path: 'create',
            component: () => import('@/pages/cluster-mange/cluster/create.vue'),
            meta: {
              hidden: true
            }
          }
        ]
      },
      {
        name: 'Infrastructures',
        path: 'infrastructures',
        component: () =>
          import('@/pages/cluster-mange/infrastructures/index.vue'),
        meta: {
          icon: 'infrastructures',
          title: 'menu.infra'
        }
      },
      {
        name: 'Components',
        path: 'components',
        component: () => import('@/pages/cluster-mange/components/index.vue'),
        meta: {
          icon: 'components',
          title: 'menu.component'
        }
      },
      {
        name: 'Hosts',
        path: 'hosts',
        redirect: '/cluster-mange/hosts/list',
        meta: {
          icon: 'hosts',
          title: 'menu.host'
        },
        children: [
          {
            name: 'List',
            path: 'list',
            component: () => import('@/pages/cluster-mange/hosts/index.vue'),
            meta: {
              hidden: true,
              activeMenu: '/cluster-mange/hosts'
            }
          },
          {
            name: 'HostCreate',
            path: 'addhost',
            component: () => import('@/pages/cluster-mange/hosts/create.vue'),
            meta: {
              hidden: true,
              activeMenu: '/cluster-mange/hosts'
            }
          }
        ]
      }
    ]
  }
]

export { routes }
