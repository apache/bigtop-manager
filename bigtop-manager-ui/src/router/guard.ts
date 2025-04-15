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

import type { NavigationGuardNext, Router } from 'vue-router'
import { useClusterStore } from '@/store/cluster'
function setCommonGuard(router: Router) {
  router.beforeEach(async (to, from, next) => {
    if (to.name === 'Clusters' && from.name !== 'Login') {
      checkClusterSelect(next)
    } else {
      next()
    }
  })
}

function checkClusterSelect(next: NavigationGuardNext) {
  const clusterStore = useClusterStore()
  clusterStore.clusterCount === 0 && clusterStore.loadClusters()
  clusterStore.clusterCount === 0 && next({ name: 'Default' })
}

function createRouterGuard(router: Router) {
  /** common guard */
  setCommonGuard(router)
}

export { createRouterGuard }
