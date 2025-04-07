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

import { getStacks } from '@/api/stack'
import { defineStore } from 'pinia'
import { shallowRef } from 'vue'
import type { ComponentVO } from '@/api/component/types'
import type { ServiceConfig, ServiceVO } from '@/api/service/types'
import type { StackVO } from '@/api/stack/types'

export type ExpandServiceVO = ServiceVO & { order: number }
interface StackRelationMap {
  services: {
    displayName: string
    stack: string
    components: string[]
    configs: ServiceConfig
  }
  components: {
    service: string
    stack: string
  } & ComponentVO
}

export const useStackStore = defineStore(
  'stack',
  () => {
    const stacks = shallowRef<StackVO[]>([])
    const stackRelationMap = shallowRef<StackRelationMap>()

    const loadStacks = async () => {
      try {
        stacks.value = await getStacks()
        stackRelationMap.value = setupStackRelationMap(stacks.value)
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    const setupStackRelationMap = (stacks: StackVO[]) => {
      const relationMap = { services: {}, components: {} } as StackRelationMap
      for (const { stackName, services } of stacks) {
        for (const service of services) {
          const { name, displayName, components, configs } = service
          relationMap.services[name!] = {
            displayName,
            stack: stackName,
            components: components!.map(({ name }) => name),
            configs
          }
          for (const component of components!) {
            relationMap.components[component.name!] = {
              ...component,
              service: name,
              stack: stackName
            }
          }
        }
      }
      return relationMap
    }

    const getServicesByExclude = (exclude?: string[], isOrder = true): ExpandServiceVO[] | ServiceVO[] => {
      const filterData = stacks.value.flatMap((stack) => (exclude?.includes(stack.stackName) ? [] : stack.services))
      return isOrder ? filterData.map((service, index) => ({ ...service, order: index })) : filterData
    }

    return {
      stacks,
      stackRelationMap,
      loadStacks,
      getServicesByExclude
    }
  },
  {
    persist: {
      storage: sessionStorage
    }
  }
)
