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
