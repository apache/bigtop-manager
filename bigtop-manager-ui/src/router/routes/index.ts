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
import { mergeRouteModules } from '@/utils/router-util'

const dynamicRoutesFiles = import.meta.glob('./modules/**/*.ts', {
  eager: true
})

export const dynamicRoutes: RouteRecordRaw[] =
  mergeRouteModules(dynamicRoutesFiles)

const routes: RouteRecordRaw[] = [
  { path: '/login', component: () => import('@/pages/login/index.vue') },
  {
    path: '/',
    redirect: '/dashboard',
    component: () => import('@/layouts/index.vue'),
    children: [...dynamicRoutes]
  },
  {
    path: '/:pathMatch(.*)',
    component: () => import('@/pages/error/404.vue')
  }
]

export default routes
