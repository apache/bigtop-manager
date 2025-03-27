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
import { RouteExceptions } from '@/enums'
import pageView from '@/layouts/index.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/cluster-manage',
    component: pageView,
    redirect: RouteExceptions.SPECIAL_ROUTE_PATH,
    meta: {
      title: 'menu.cluster'
    },
    children: [
      {
        name: RouteExceptions.DEFAULT_ROUTE_NAME,
        path: 'default',
        component: () => import('@/layouts/default.vue'),
        meta: {
          hidden: true
        }
      },
      {
        name: RouteExceptions.SPECIAL_ROUTE_NAME,
        path: 'clusters',
        redirect: '',
        meta: {
          icon: 'clusters',
          title: 'menu.cluster'
        },
        children: [
          {
            name: 'ClusterDetail',
            path: RouteExceptions.DYNAMIC_ROUTE_MATCH,
            component: () => import('@/pages/cluster-manage/cluster/index.vue'),
            meta: {
              hidden: true
            }
          },
          {
            name: 'CreateCluster',
            path: 'create-cluster',
            component: () => import('@/pages/cluster-manage/cluster/create.vue'),
            meta: {
              hidden: true
            }
          },
          {
            name: 'CreateService',
            path: `${RouteExceptions.DYNAMIC_ROUTE_MATCH}/create-service/:creationMode`,
            component: () => import('@/components/create-service/create.vue'),
            meta: {
              hidden: true
            }
          }
        ]
      },
      {
        name: 'Infrastructures',
        path: 'infrastructures',
        redirect: '/cluster-manage/infra/list',
        meta: {
          icon: 'infrastructures',
          title: 'menu.infra'
        },
        children: [
          {
            name: 'InfraList',
            path: 'list',
            component: () => import('@/pages/cluster-manage/infra/index.vue'),
            meta: {
              hidden: true,
              activeMenu: '/cluster-manage/infrastructures'
            }
          },
          {
            name: 'CreateInfraService',
            path: 'create-infra-service/:id/:creationMode',
            component: () => import('@/components/create-service/create.vue'),
            meta: {
              hidden: true,
              activeMenu: '/cluster-manage/infrastructures'
            }
          }
        ]
      },
      {
        name: 'Components',
        path: 'components',
        component: () => import('@/pages/cluster-manage/components/index.vue'),
        meta: {
          icon: 'components',
          title: 'menu.component'
        }
      },
      {
        name: 'Hosts',
        path: 'hosts',
        redirect: '/cluster-manage/hosts/list',
        meta: {
          icon: 'hosts',
          title: 'menu.host'
        },
        children: [
          {
            name: 'HostList',
            path: 'list',
            component: () => import('@/pages/cluster-manage/hosts/index.vue'),
            meta: {
              hidden: true,
              activeMenu: '/cluster-manage/hosts'
            }
          },
          {
            name: 'HostCreation',
            path: 'add',
            component: () => import('@/pages/cluster-manage/hosts/create.vue'),
            meta: {
              hidden: true,
              activeMenu: '/cluster-manage/hosts'
            }
          }
        ]
      }
    ]
  }
]

export { routes, RouteExceptions }
