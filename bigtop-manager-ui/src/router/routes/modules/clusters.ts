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
    path: '/cluster-manage',
    name: 'ClusterManage',
    component: pageView,
    redirect: '/cluster-manage/clusters',
    meta: {
      title: 'menu.cluster'
    },
    children: [
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
            name: 'Default',
            path: 'default',
            component: () => import('@/layouts/default.vue'),
            meta: {
              hidden: true
            }
          },
          {
            name: 'ClusterDetail',
            path: ':id',
            component: () => import('@/pages/cluster-manage/cluster/index.vue'),
            meta: {
              hidden: true
            }
          },
          {
            name: 'CreateCluster',
            path: 'create-cluster',
            component: () => import('@/features/create-cluster/index.vue'),
            meta: {
              hidden: true
            }
          },
          {
            name: 'CreateService',
            path: ':id/create-service/:creationMode?',
            component: () => import('@/features/create-service/index.vue'),
            meta: {
              hidden: true
            }
          },
          {
            name: 'ServiceDetail',
            path: ':id/service-detail/:serviceId',
            component: () => import('@/features/service-management/index.vue'),
            meta: {
              hidden: true
            }
          },
          {
            name: 'CreateComponent',
            path: ':id/create-component/:serviceId/:creationMode?/:type',
            component: () => import('@/features/create-service/index.vue'),
            meta: {
              hidden: true
            }
          }
        ]
      },
      {
        name: 'Infrastructures',
        path: 'infrastructures',
        redirect: '/cluster-manage/infrastructures/list',
        meta: {
          icon: 'infrastructures',
          title: 'menu.infra'
        },
        children: [
          {
            name: 'InfraList',
            path: 'list',
            component: () => import('@/pages/cluster-manage/infrastructures/index.vue'),
            meta: {
              hidden: true,
              activeMenu: '/cluster-manage/infrastructures/list'
            }
          },
          {
            name: 'CreateInfraService',
            path: 'create-infra-service/:id/:creationMode',
            component: () => import('@/features/create-service/index.vue'),
            meta: {
              hidden: true,
              activeMenu: '/cluster-manage/infrastructures/list'
            }
          },
          {
            name: 'InfraServiceDetail',
            path: 'create-infra-service/service-detail/:id/:serviceId',
            component: () => import('@/features/service-management/index.vue'),
            meta: {
              hidden: true,
              activeMenu: '/cluster-manage/infrastructures/list'
            }
          },
          {
            name: 'CreateInfraComponent',
            path: '/create-infra-service/create-infra-component/:id/:serviceId/:creationMode/:type',
            component: () => import('@/features/create-service/index.vue'),
            meta: {
              hidden: true,
              activeMenu: '/cluster-manage/infrastructures/list'
            }
          }
        ]
      },
      {
        name: 'Components',
        path: 'components',
        redirect: '/cluster-manage/components/list',
        meta: {
          icon: 'components',
          title: 'menu.stacks'
        },
        children: [
          {
            name: 'ComponentList',
            path: 'list',
            component: () => import('@/pages/cluster-manage/components/index.vue'),
            meta: {
              hidden: true,
              activeMenu: '/cluster-manage/components/list'
            }
          }
        ]
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
              activeMenu: '/cluster-manage/hosts/list'
            }
          },
          {
            name: 'HostCreation',
            path: 'add',
            component: () => import('@/features/create-host/index.vue'),
            meta: {
              hidden: true,
              activeMenu: '/cluster-manage/hosts/list'
            }
          },
          {
            name: 'HostDetail',
            path: 'detail',
            component: () => import('@/pages/cluster-manage/hosts/detail.vue'),
            meta: {
              hidden: true,
              activeMenu: '/cluster-manage/hosts/list'
            }
          }
        ]
      }
    ]
  }
]

export { routes }
