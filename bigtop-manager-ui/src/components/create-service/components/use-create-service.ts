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

import { computed, ComputedRef, createVNode, effectScope, Ref, ref, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { ExpandServiceVO, useStackStore } from '@/store/stack'
import { useInstalledStore } from '@/store/installed'
import { execCommand } from '@/api/command'
import useSteps from '@/composables/use-steps'
import SvgIcon from '@/components/common/svg-icon/index.vue'
import type { HostVO } from '@/api/hosts/types'
import type { CommandRequest, CommandVO, ServiceCommandReq } from '@/api/command/types'
import type { ServiceVO } from '@/api/service/types'
import type { ComponentVO } from '@/api/component/types'

interface ProcessResult {
  success: boolean
  conflictService?: ExpandServiceVO
}

interface RouteParams {
  service: string
  serviceId: number
  id: number
  cluster: string
}

interface CompItem extends ComponentVO {
  hosts: HostVO[]
}

const scope = effectScope()
let isChange = false
let selectedServices: Ref<ExpandServiceVO[]>
let selectedServicesMeta: Ref<ExpandServiceVO[]>
let afterCreateRes: Ref<CommandVO>
let servicesOfInfra: ComputedRef<ExpandServiceVO[]>
let servicesOfExcludeInfra: ComputedRef<ExpandServiceVO[] | ServiceVO[]>
let steps: ComputedRef<string[]>

const setupStore = () => {
  scope.run(() => {
    selectedServices = ref<ExpandServiceVO[]>([])
    selectedServicesMeta = ref<ExpandServiceVO[]>([])
    afterCreateRes = ref<{ clusterId: number } & CommandVO>({ id: undefined, clusterId: 0 })
    servicesOfInfra = computed(() => useStackStore().getServicesByExclude(['bigtop', 'extra']) as ExpandServiceVO[])
    servicesOfExcludeInfra = computed(() => useStackStore().getServicesByExclude(['infra']))
    steps = computed(() => [
      'service.select_service',
      'service.assign_component',
      'service.configure_service',
      'service.service_overview',
      'service.install_component'
    ])
  })
}

const useCreateService = () => {
  if (!isChange) {
    setupStore()
    isChange = true
  }
  const installedStore = useInstalledStore()
  const route = useRoute()
  const { t } = useI18n()
  const processedServices = ref(new Set())
  const { current, stepsLimit, previousStep, nextStep } = useSteps(steps.value)
  const clusterId = computed(() => Number(route.params.id))
  const creationMode = computed(() => route.params.creationMode as 'internal' | 'public')
  const creationModeType = computed(() => route.params.type)
  const routeParams = computed(() => route.params as unknown as RouteParams)

  const commandRequest = ref<CommandRequest>({
    command: 'Add',
    commandLevel: 'service',
    clusterId: clusterId.value
  })

  const allComps = computed(() => {
    return new Map(
      selectedServices.value.flatMap((s) =>
        s.components!.map((comp) => [
          comp.name,
          { serviceName: s.name, serviceDisplayName: s.displayName, serviceId: s.id, ...comp }
        ])
      )
    ) as Map<string, CompItem>
  })

  const allCompsMeta = computed(() => {
    return new Map(
      selectedServicesMeta.value.flatMap((s) =>
        s.components!.map((comp) => [
          comp.name,
          { serviceName: s.name, serviceDisplayName: s.displayName, serviceId: s.id, ...comp }
        ])
      )
    ) as Map<string, CompItem>
  })

  watch(
    () => selectedServices.value,
    () => {
      processedServices.value = new Set(selectedServices.value.map((v) => v.name))
    },
    {
      deep: true
    }
  )

  const setDataByCurrent = (val: ExpandServiceVO[]) => {
    selectedServices.value = val
    selectedServicesMeta.value = JSON.parse(JSON.stringify(val))
  }

  const updateHostsForComponent = (compName: string, hosts: HostVO[]) => {
    const [serviceName, componentName] = compName.split('/')
    const service = selectedServices.value.find((svc) => svc.name === serviceName)
    if (!service) return false
    const component = service.components?.find((comp) => comp.name === componentName)
    if (!component) return false
    component.hosts = hosts
  }

  const transformServiceData = (services: ExpandServiceVO[]) => {
    return services.map((service) => ({
      serviceName: service.name,
      installed: service.isInstalled === undefined ? false : service.isInstalled,
      componentHosts: (service.components || []).map((component) => ({
        componentName: component.name,
        hostnames: (component.hosts || []).map((host: HostVO) => host.hostname)
      })),
      configs: service.configs
    })) as ServiceCommandReq[]
  }

  // Validate services from infra
  const validServiceFromInfra = (targetService: ExpandServiceVO, requiredServices: string[]) => {
    const servicesOfInfraNames = servicesOfInfra.value.map((v) => v.name)
    const installedServicesOfInfra = installedStore.getInstalledNamesOrIdsOfServiceByKey('0', 'names')
    const set = new Set(installedServicesOfInfra)
    const missServices = requiredServices.reduce((acc, name) => {
      !set.has(name) && servicesOfInfraNames.includes(name) && acc.push(name)
      return acc
    }, [] as string[])

    if (missServices.length === 0) return false
    message.error(t('service.dependencies_conflict_msg', [targetService.displayName!, missServices.join(',')]))
    return true
  }

  const processDependencies = async (
    targetService: ExpandServiceVO,
    serviceMap: Map<string, ExpandServiceVO>,
    servicesOfInfra: ExpandServiceVO[],
    collected: ExpandServiceVO[]
  ): Promise<ProcessResult> => {
    const dependencies = targetService.requiredServices || []

    if (creationMode.value === 'internal' && validServiceFromInfra(targetService, dependencies)) {
      return {
        success: false,
        conflictService: targetService
      }
    }

    for (const serviceName of dependencies) {
      const dependency = serviceMap.get(serviceName)
      if (!dependency || processedServices.value.has(dependency.name!)) continue

      if (dependency.isInstalled) continue

      const shouldAdd = await confirmRequiredServicesToInstall(targetService, dependency)
      if (!shouldAdd) return { success: false }

      collected.push(dependency)
      processedServices.value.add(dependency.name!)
      const result = await processDependencies(dependency, serviceMap, servicesOfInfra, collected)

      if (!result.success) {
        collected.splice(collected.indexOf(dependency), 1)
        processedServices.value.delete(dependency.name!)
        return result
      }
    }
    return { success: true }
  }

  const getServiceMap = (services: ServiceVO[]) => {
    return new Map(services.map((s) => [s.name as string, s as ExpandServiceVO]))
  }

  const handlePreSelectedServiceDependencies = async (preSelectedService: ExpandServiceVO) => {
    const serviceMap =
      creationMode.value == 'public'
        ? getServiceMap(servicesOfInfra.value)
        : getServiceMap(servicesOfExcludeInfra.value)

    const result: ExpandServiceVO[] = []
    const dependenciesSuccess = await processDependencies(preSelectedService, serviceMap, servicesOfInfra.value, result)
    if (dependenciesSuccess.success) {
      result.unshift(preSelectedService)
      return result
    }
    return []
  }

  const confirmServiceDependencies = async (preSelectedService: ExpandServiceVO) => {
    const { requiredServices } = preSelectedService
    if (!requiredServices) {
      return [preSelectedService]
    }
    if (creationMode.value === 'internal' && validServiceFromInfra(preSelectedService, requiredServices)) {
      return []
    } else {
      return await handlePreSelectedServiceDependencies(preSelectedService)
    }
  }

  const confirmRequiredServicesToInstall = (targetService: ExpandServiceVO, requiredService: ExpandServiceVO) => {
    return new Promise((resolve) => {
      Modal.confirm({
        content: t('service.dependencies_msg', [targetService.displayName, requiredService.displayName]),
        icon: createVNode(SvgIcon, { name: 'unknown' }),
        cancelText: t('common.no'),
        okText: t('common.yes'),
        onOk: () => resolve(true),
        onCancel: () => {
          Modal.destroyAll()
          return resolve(false)
        }
      })
    })
  }

  const createService = async () => {
    try {
      commandRequest.value.serviceCommands = transformServiceData(selectedServices.value)
      afterCreateRes.value = await execCommand(commandRequest.value)
      Object.assign(afterCreateRes.value, { clusterId })
      return true
    } catch (error) {
      console.log('error :>> ', error)
      return false
    }
  }

  const addComponentForService = async () => {
    try {
      commandRequest.value.commandLevel = 'component'
      commandRequest.value.componentCommands = []
      for (const [compName, comp] of allComps.value) {
        commandRequest.value.componentCommands?.push({
          componentName: compName!,
          hostnames: comp.hosts.map((v) => v.hostname!)
        })
      }
      afterCreateRes.value = await execCommand(commandRequest.value)
      Object.assign(afterCreateRes.value, { clusterId })
      return true
    } catch (error) {
      console.log('error :>> ', error)
      return false
    }
  }

  return {
    steps,
    clusterId,
    current,
    stepsLimit,
    selectedServices,
    selectedServicesMeta,
    servicesOfExcludeInfra,
    servicesOfInfra,
    installedStore,
    allComps,
    afterCreateRes,
    scope,
    creationMode,
    creationModeType,
    routeParams,
    allCompsMeta,
    setDataByCurrent,
    addComponentForService,
    updateHostsForComponent,
    confirmServiceDependencies,
    createService,
    previousStep,
    nextStep
  }
}

export default useCreateService
