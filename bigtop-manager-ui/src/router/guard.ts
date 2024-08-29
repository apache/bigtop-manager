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

import type { Router } from 'vue-router'
import type { ServiceVO } from '@/api/service/types.ts'
import { storeToRefs } from 'pinia'
import { useServiceStore } from '@/store/service'
import { useClusterStore } from '@/store/cluster'
function setCommonGuard(router: Router) {
  router.beforeEach(async (to) => {
    if (to.name === 'services') {
      const clusterStore = useClusterStore()
      const serviceStore = useServiceStore()
      const { clusterId } = storeToRefs(clusterStore)
      const { installedServices } = storeToRefs(serviceStore)

      if (clusterId.value === 0) {
        await clusterStore.loadClusters()
        await serviceStore.loadServices()
      }

      const installedServiceNames = installedServices.value.map(
        (service: ServiceVO) => service.serviceName
      )

      if (!installedServiceNames.includes(to.params.serviceName as string)) {
        return '/404'
      }
    }
  })
}

function createRouterGuard(router: Router) {
  /** common guard */
  setCommonGuard(router)
}

export { createRouterGuard }
