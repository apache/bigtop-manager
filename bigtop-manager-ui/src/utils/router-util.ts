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
import type { RouteRecordRaw } from 'vue-router'

interface RouteModuleType {
  routes: RouteRecordRaw[]
}

type RoutePriorityMap = { [key: string]: number }
export const routePriorityMap: RoutePriorityMap = {
  Dashboard: 1,
  Hosts: 2,
  Services: 3,
  Cluster: 4
}

export function mergeRouteModules(
  routeModules: Record<string, unknown>
): RouteRecordRaw[] {
  const mergedRoutes: RouteRecordRaw[] = []
  for (const routeModule of Object.values(routeModules)) {
    const moduleRoutes = (routeModule as RouteModuleType)?.routes ?? []
    mergedRoutes.push(...moduleRoutes)
  }

  return mergedRoutes
}
