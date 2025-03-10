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

import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useStackStore } from '@/store/stack'
import { execCommand } from '@/api/command'
import useSteps from '@/composables/use-steps'
import type { HostVO } from '@/api/hosts/types'
import type { ServiceVO } from '@/api/service/types'
import type { CommandVO, CommandRequest, ServiceCommandReq } from '@/api/command/types'

type DataItem = ServiceVO & { order: number }

const filteredServices = computed(() => useStackStore().getServicesByExclude(['infra']))
const selectedServices = ref<DataItem[]>([])
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

  const setDataByCurrent = (val: DataItem[]) => {
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

  const transformServiceData = (services: DataItem[]) => {
    return services.map((service) => ({
      serviceName: service.name,
      componentHosts: service.components!.map((component) => ({
        componentName: component.name,
        hostnames: component.hosts.map((host: HostVO) => host.hostname)
      })),
      configs: service.configs
    })) as ServiceCommandReq[]
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
    filteredServices,
    allComps,
    afterCreateRes,
    setDataByCurrent,
    updateHostsForComponent,
    createService,
    previousStep,
    nextStep
  }
}

export default useCreateService
