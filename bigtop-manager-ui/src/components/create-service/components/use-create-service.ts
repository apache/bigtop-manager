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

import { computed, createVNode, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ExpandServiceVO, useStackStore } from '@/store/stack'
import { execCommand } from '@/api/command'
import useSteps from '@/composables/use-steps'
import type { HostVO } from '@/api/hosts/types'
import type { CommandVO, CommandRequest, ServiceCommandReq } from '@/api/command/types'
import { message, Modal } from 'ant-design-vue'
import { ExclamationCircleOutlined } from '@ant-design/icons-vue'

const filteredServices = computed(() => useStackStore().getServicesByExclude(['infra']))
const servicesOfInfra = computed(() => useStackStore().getServicesByExclude(['bigtop', 'extra']))
const selectedServices = ref<ExpandServiceVO[]>([])
const afterCreateRes = ref<CommandVO>({ id: undefined })

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
  const validServiceFromInfra = (requiredServices: string[]) => {
    return servicesOfInfra.value
      .filter((service) => requiredServices?.includes(service.name!))
      .map((v) => v.displayName)
  }

  interface ProcessResult {
    success: boolean
    conflictService?: ExpandServiceVO
  }

  async function processDependencies(
    node: ExpandServiceVO,
    serviceMap: Map<string, ExpandServiceVO>,
    servicesOfInfra: ExpandServiceVO[],
    collected: ExpandServiceVO[],
    processed: Set<string>
  ): Promise<ProcessResult> {
    const dependencies = node.requiredServices || []
    const infraConflict = servicesOfInfra.some((s) => s.name === node.name)

    if (infraConflict) {
      return {
        success: false,
        conflictService: node
      }
    }

    for (const serviceName of dependencies) {
      const dependency = serviceMap.get(serviceName)
      if (!dependency || processed.has(dependency.name!)) continue

      const isInInfra = servicesOfInfra.some((s) => s.name === dependency.name)
      if (isInInfra) {
        return {
          success: false,
          conflictService: dependency
        }
      }

      const shouldAdd = await confirmRequiredServicesToInstall(dependency)
      if (!shouldAdd) return { success: false }

      collected.push(dependency)
      processed.add(dependency.name!)
      const result = await processDependencies(dependency, serviceMap, servicesOfInfra, collected, processed)

      if (!result.success) {
        collected.splice(collected.indexOf(dependency), 1)
        processed.delete(dependency.name!)
        return result
      }
    }
    return { success: true }
  }

  const handleServiceSelection = async (selectedNode: ExpandServiceVO) => {
    const serviceMap = new Map(filteredServices.value.map((s) => [s.name as string, s as ExpandServiceVO]))
    const processed = new Set(selectedServices.value.map((v) => v.name!))
    const result: ExpandServiceVO[] = []
    const dependenciesSuccess = await processDependencies(
      selectedNode,
      serviceMap,
      servicesOfInfra.value as ExpandServiceVO[],
      result,
      processed
    )
    if (dependenciesSuccess) {
      result.unshift(selectedNode)
      return result
    }
    return []
  }

  const resolveRequiredServices = async (item: ExpandServiceVO) => {
    const { requiredServices } = item
    if (!requiredServices) {
      return [item]
    }
    const filterServiceNames = validServiceFromInfra(requiredServices)
    if (filterServiceNames.length > 0) {
      message.error(`${filterServiceNames.join(',')}属于基础服务，请先前往安装`)
      return []
    } else {
      return await handleServiceSelection(item)
    }
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

  const confirmRequiredServicesToInstall = (requiredService: ExpandServiceVO) => {
    return new Promise((resolve) => {
      Modal.confirm({
        content: `dependency ${requiredService.displayName}`,
        icon: createVNode(ExclamationCircleOutlined),
        cancelText: '否',
        okText: '是',
        onOk() {
          return resolve(true)
        },
        onCancel() {
          Modal.destroyAll()
          return resolve(false)
        }
      })
    })
  }

  return {
    steps,
    current,
    stepsLimit,
    selectedServices,
    filteredServices,
    allComps,
    afterCreateRes,
    setDataByCurrent,
    updateHostsForComponent,
    resolveRequiredServices,
    createService,
    previousStep,
    nextStep
  }
}

export default useCreateService
