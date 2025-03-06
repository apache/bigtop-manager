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
import { useStackStore } from '@/store/stack'
import useSteps from '@/composables/use-steps'
import type { HostVO } from '@/api/hosts/types'
import type { ServiceVO } from '@/api/service/types'
import type { DataNode } from 'ant-design-vue/es/tree'
import { TreeProps } from 'ant-design-vue'

type DataItem = ServiceVO & { order: number }

const filteredServices = computed(() => useStackStore().getServicesByExclude(['infra']))
const serviceCommands = ref<DataItem[]>([])
const steps = computed(() => [
  'service.select_service',
  'service.assign_component',
  'service.configure_service',
  'service.service_overview',
  'service.install_component'
])
const { current, stepsLimit, previousStep, nextStep } = useSteps(steps.value)

const useCreateService = () => {
  const allComps = computed(() => {
    return new Map(serviceCommands.value.flatMap((s) => s.components!.map((comp) => [comp.name, comp])))
  })

  const setDataByCurrent = (val: DataItem[]) => {
    serviceCommands.value = val
  }

  const updateComponentHosts = (compName: string, hosts: HostVO[]) => {
    const [serviceName, componentName] = compName.split('/')
    const service = serviceCommands.value.find((svc) => svc.name === serviceName)
    if (!service) return false
    const component = service.components?.find((comp) => comp.name === componentName)
    if (!component) return false
    component.hosts = hosts
  }

  const findLastChildOfFirstNode = (
    tree: any,
    fieldNames: TreeProps['fieldNames']
  ): { currNode: DataNode; pNode: DataNode | undefined } | undefined => {
    if (!tree || tree.length === 0) return undefined
    const childrenKey = fieldNames?.children || 'children'
    const queue: Array<{ node: DataNode; pNode: DataNode | undefined }> = tree.map((node: any) => ({
      node,
      pNode: undefined
    }))
    while (queue.length > 0) {
      const { node: current, pNode } = queue.shift()!
      const children = (current[childrenKey] as DataNode[]) || []
      if (children.length === 0) {
        return { currNode: current, pNode }
      }
      children.forEach((child) => {
        queue.push({ node: child, pNode: current })
      })
    }
    return undefined
  }

  return {
    steps,
    current,
    stepsLimit,
    serviceCommands,
    filteredServices,
    allComps,
    setDataByCurrent,
    findLastChildOfFirstNode,
    updateComponentHosts,
    previousStep,
    nextStep
  }
}

export default useCreateService
