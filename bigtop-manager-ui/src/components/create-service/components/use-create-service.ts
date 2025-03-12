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

import { computed, createVNode, ref, watch } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import { ExpandServiceVO, useStackStore } from '@/store/stack'
import { execCommand } from '@/api/command'
import useSteps from '@/composables/use-steps'
import SvgIcon from '@/components/common/svg-icon/index.vue'
import type { HostVO } from '@/api/hosts/types'
import type { CommandVO, CommandRequest, ServiceCommandReq } from '@/api/command/types'

interface ProcessResult {
  success: boolean
  conflictService?: ExpandServiceVO
}

const selectedServices = ref<ExpandServiceVO[]>([])
const afterCreateRes = ref<CommandVO>({ id: undefined })

const servicesOfInfra = computed(() => useStackStore().getServicesByExclude(['bigtop', 'extra']) as ExpandServiceVO[])
const servicesOfExcludeInfra = computed(() => useStackStore().getServicesByExclude(['infra']))
const steps = computed(() => [
  'service.select_service',
  'service.assign_component',
  'service.configure_service',
  'service.service_overview',
  'service.install_component'
])
const { current, stepsLimit, previousStep, nextStep } = useSteps(steps.value)

const useCreateService = () => {
  const route = useRoute()
  const { t } = useI18n()
  const processedServices = ref(new Set())

  watch(
    () => selectedServices.value,
    () => {
      processedServices.value = new Set(selectedServices.value.map((v) => v.name))
    },
    {
      deep: true
    }
  )

  const commandRequest = ref<CommandRequest>({
    command: 'Add',
    commandLevel: 'service',
    clusterId: parseInt(route.query.clusterId as string)
  })

  const allComps = computed(() => {
    return new Map(selectedServices.value.flatMap((s) => s.components!.map((comp) => [comp.name, comp])))
  })

  const setDataByCurrent = (val: ExpandServiceVO[]) => {
    selectedServices.value = val
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
      componentHosts: service.components!.map((component) => ({
        componentName: component.name,
        hostnames: component.hosts.map((host: HostVO) => host.hostname)
      })),
      configs: service.configs
    })) as ServiceCommandReq[]
  }

  // Validate services from infra
  const validServiceFromInfra = (targetService: ExpandServiceVO, requiredServices: string[]) => {
    const filterServiceNames = servicesOfInfra.value
      .filter((service) => requiredServices?.includes(service.name!))
      .map((service) => service.displayName)

    if (!filterServiceNames.length) return false
    message.error(t('service.dependencies_conflict_msg', [targetService.displayName!, filterServiceNames.join(',')]))
    return true
  }

  const processDependencies = async (
    targetService: ExpandServiceVO,
    serviceMap: Map<string, ExpandServiceVO>,
    servicesOfInfra: ExpandServiceVO[],
    collected: ExpandServiceVO[]
  ): Promise<ProcessResult> => {
    const dependencies = targetService.requiredServices || []

    if (validServiceFromInfra(targetService, dependencies)) {
      return {
        success: false,
        conflictService: targetService
      }
    }

    for (const serviceName of dependencies) {
      const dependency = serviceMap.get(serviceName)
      if (!dependency || processedServices.value.has(dependency.name!)) continue

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

  const handlePreSelectedServiceDependencies = async (preSelectedService: ExpandServiceVO) => {
    const serviceMap = new Map(servicesOfExcludeInfra.value.map((s) => [s.name as string, s as ExpandServiceVO]))
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
    if (validServiceFromInfra(preSelectedService, requiredServices)) {
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
      const formatData = transformServiceData(selectedServices.value)
      commandRequest.value.serviceCommands = formatData
      afterCreateRes.value = await execCommand(commandRequest.value)
      return true
    } catch (error) {
      console.log('error :>> ', error)
      return false
    }
  }

  return {
    steps,
    current,
    stepsLimit,
    selectedServices,
    servicesOfExcludeInfra,
    allComps,
    afterCreateRes,
    setDataByCurrent,
    updateHostsForComponent,
    confirmServiceDependencies,
    createService,
    previousStep,
    nextStep
  }
}

export default useCreateService
