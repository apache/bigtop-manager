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
import { computed, ref, shallowRef, toRaw } from 'vue'
import { defineStore } from 'pinia'
import useSteps from '@/composables/use-steps'
import { useValidations } from './validation'
import { cloneDeep } from 'lodash-es'
import { execCommand } from '@/api/command'

import { type ExpandServiceVO, useStackStore } from '@/store/stack'

import type { ServiceConfig, ServiceVO } from '@/api/service/types'
import type {
  CommandRequest,
  CommandVO,
  ComponentCommandReq,
  ComponentHostReq,
  ServiceCommandReq,
  ServiceConfigReq
} from '@/api/command/types'
import type { HostVO } from '@/api/hosts/types'
import type { ComponentVO } from '@/api/component/types'

const STEPS_TITLES = [
  'service.select_service',
  'service.assign_component',
  'service.configure_service',
  'service.service_overview',
  'service.install_component'
]

export interface ProcessResult {
  success: boolean
  conflictService?: ExpandServiceVO
}

export interface StepContext {
  clusterId: number
  serviceId: number
  creationMode: 'internal' | 'public'
  type?: 'component'
  [propName: string]: any
}

export interface CompItem extends ComponentVO {
  hosts: HostVO[]
}

export const useCreateServiceStore = defineStore(
  'service-create',
  () => {
    const validations = useValidations()
    const stackStore = useStackStore()

    const steps = shallowRef(STEPS_TITLES)
    const { current, stepsLimit, previousStep, nextStep } = useSteps(steps.value)

    const selectedServices = ref<ExpandServiceVO[]>([])
    const snapshotSelectedServices = ref<ExpandServiceVO[]>([])
    const createdPayload = ref<CommandVO>({})

    const commandRequest = ref<CommandRequest>({
      command: 'Add',
      commandLevel: 'service'
    })

    const stepContext = ref<StepContext>({
      clusterId: 0,
      serviceId: 0,
      creationMode: 'internal'
    })

    const creationMode = computed(() => stepContext.value.creationMode)
    const clusterId = computed(() => (creationMode.value === 'internal' ? stepContext.value.clusterId : 0))

    const infraServices = computed(() => stackStore.getServicesByExclude(['bigtop', 'extra']) as ExpandServiceVO[])
    const infraServiceNames = computed(() => infraServices.value.map((v) => v.name!))
    const excludeInfraServices = computed(() => stackStore.getServicesByExclude(['infra']))

    const processedServices = computed(() => new Set(selectedServices.value.map((v) => v.name)))

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

    const componentSnapshot = computed(() => {
      return new Map(
        snapshotSelectedServices.value.flatMap((s) =>
          s.components!.map((comp) => [
            comp.name,
            { serviceName: s.name, serviceDisplayName: s.displayName, serviceId: s.id, ...comp }
          ])
        )
      ) as Map<string, CompItem>
    })

    const snapshotSelectedServiceMap = computed(() =>
      snapshotSelectedServices.value.reduce(
        (p, s) => {
          if (!s.name) {
            return p
          }

          if (!p[s.name]) {
            s.configs && (p[s.name] = { configMap: generateConfigsMap(s.configs ?? []) })
          }

          return p
        },
        {} as Record<string, any>
      )
    )

    function getServiceMap(services: ServiceVO[]) {
      return new Map(services.map((s) => [s.name as string, s as ExpandServiceVO]))
    }

    function updateSelectedService(data: ExpandServiceVO[]) {
      selectedServices.value = data
    }

    function setTempData(data: ExpandServiceVO[]) {
      snapshotSelectedServices.value = cloneDeep(data)
    }

    function setStepContext(data: StepContext) {
      stepContext.value = data
    }

    function updateInstalledStatus(state: string) {
      createdPayload.value.state = state
    }

    function generateConfigsMap(arr: ServiceConfig[]): Record<string, Record<string, any>> {
      const treeMap: Record<string, Record<string, any>> = {}

      for (const { name, properties } of arr) {
        if (!name) continue

        const propMap: Record<string, any> = {}

        for (const prop of properties ?? []) {
          if (prop.isManual) continue
          const key = prop.name
          propMap[key] = {
            name: prop.name,
            value: prop.value,
            ...(prop.id !== undefined && { id: prop.id }),
            ...(prop.displayName !== undefined && { displayName: prop.displayName })
          }
        }

        treeMap[name] = propMap
      }

      return treeMap
    }

    async function confirmServiceDependencyAction(type: 'add' | 'remove', preSelectedService: ExpandServiceVO) {
      const { requiredServices } = preSelectedService
      if (!requiredServices && type === 'add') {
        return [preSelectedService]
      }
      const valid = validations.validServiceFromInfra(preSelectedService, requiredServices!, infraServiceNames.value)
      if (type === 'add' && creationMode.value === 'internal' && valid) {
        return []
      } else {
        return await handleServiceDependencyConfirm(type, preSelectedService)
      }
    }

    async function handleServiceDependencyConfirm(type: 'add' | 'remove', preSelectedService: ExpandServiceVO) {
      const result: ExpandServiceVO[] = []
      const target = creationMode.value === 'public' ? infraServices.value : excludeInfraServices.value
      const serviceMap = getServiceMap(target)

      const dependenciesSuccess =
        type === 'add'
          ? await processDependencies(preSelectedService, serviceMap, infraServices.value, result)
          : await notifyDependents(preSelectedService, result)

      if (dependenciesSuccess.success) {
        result.unshift(preSelectedService)
        return result
      }
      return []
    }

    async function notifyDependents(
      targetService: ExpandServiceVO,
      collected: ExpandServiceVO[]
    ): Promise<ProcessResult> {
      for (const service of selectedServices.value) {
        if (!service.requiredServices?.includes(targetService.name!)) continue

        const shouldRemove = await validations.confirmDependencyAddition('remove', service, targetService)
        if (!shouldRemove) return { success: false }
        collected.push(service)

        const result = await notifyDependents(service, collected)
        if (!result.success) {
          collected.splice(collected.indexOf(service), 1)
          processedServices.value.delete(service.name!)
          return result
        }
      }
      return { success: true }
    }

    async function processDependencies(
      targetService: ExpandServiceVO,
      serviceMap: Map<string, ExpandServiceVO>,
      servicesOfInfra: ExpandServiceVO[],
      collected: ExpandServiceVO[]
    ): Promise<ProcessResult> {
      const dependencies = targetService.requiredServices || []

      const valid = validations.validServiceFromInfra(targetService, dependencies, infraServiceNames.value)
      if (creationMode.value === 'internal' && valid) {
        return { success: false, conflictService: targetService }
      }

      for (const serviceName of dependencies) {
        const dependency = serviceMap.get(serviceName)
        if (!dependency || processedServices.value.has(dependency.name!)) continue

        if (dependency.isInstalled) continue

        const shouldAdd = await validations.confirmDependencyAddition('add', targetService, dependency)
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

    function getUninstalledDiffs(services: ExpandServiceVO[]) {
      const diffRes = [] as ServiceCommandReq[]
      const filterConfigMap = {} as Record<string, ServiceConfig[]>

      for (const s of services) {
        if (s.isInstalled || !s.name) continue

        const serviceName = s.name
        const snapshot = snapshotSelectedServiceMap.value[serviceName]
        if (!snapshot) continue

        const configMap = snapshot.configMap

        for (const c of s.configs ?? []) {
          const configName = c.name
          if (!configName || !configMap[configName]) continue
          const oldPropsMap = configMap[configName]

          const diffProps = (c.properties ?? []).filter((prop) => {
            if (prop.name === '') return false
            const oldProp = oldPropsMap[prop.name]
            if (!oldProp) return true
            return oldProp && oldProp.value !== prop.value
          })

          if (diffProps.length > 0) {
            filterConfigMap[serviceName] = []
            filterConfigMap[serviceName].push({
              name: configName,
              properties: diffProps.map(({ name, value }) => ({ name, value }))
            })
          }
        }

        diffRes.push({
          serviceName: s.name,
          installed: false,
          componentHosts: (s.components || []).map((component) => ({
            componentName: component.name,
            hostnames: (component.hosts || []).map((host: HostVO) => host.hostname)
          })) as ComponentHostReq[],
          configs: filterConfigMap[s.name] as ServiceConfigReq[]
        })
      }

      return diffRes
    }

    function formatComponentData(components: Map<string, CompItem>) {
      const componentCommands = [] as ComponentCommandReq[]
      for (const [compName, comp] of components) {
        componentCommands?.push({
          componentName: compName!,
          hostnames: comp.hosts.map((v) => v.hostname!)
        })
      }
      return componentCommands
    }

    function setComponentHosts(compName: string, hosts: HostVO[]) {
      const [serviceName, componentName] = compName.split('/')
      const service = selectedServices.value.find((svc) => svc.name === serviceName)
      if (!service) return false
      const component = service.components?.find((comp) => comp.name === componentName)
      if (!component) return false
      component.hosts = hosts
    }

    async function createService() {
      try {
        commandRequest.value.serviceCommands = getUninstalledDiffs(toRaw(selectedServices.value))
        createdPayload.value = await execCommand({ ...commandRequest.value, clusterId: clusterId.value })
        Object.assign(createdPayload.value, { clusterId: clusterId.value })
        return true
      } catch (error) {
        console.log('error :>> ', error)
        return false
      }
    }

    async function attachComponentToService() {
      try {
        commandRequest.value.commandLevel = 'component'
        commandRequest.value.componentCommands = formatComponentData(allComps.value)
        createdPayload.value = await execCommand({ ...commandRequest.value, clusterId: clusterId.value })
        Object.assign(createdPayload.value, { clusterId: clusterId.value })
        return true
      } catch (error) {
        console.log('error :>> ', error)
        return false
      }
    }

    function $reset() {
      current.value = 0
      selectedServices.value = []
      snapshotSelectedServices.value = []
      createdPayload.value = {}
      stepContext.value = {
        clusterId: 0,
        serviceId: 0,
        creationMode: 'internal'
      }
      commandRequest.value = {
        command: 'Add',
        commandLevel: 'service'
      }
    }

    return {
      steps,
      selectedServices,
      stepContext,
      infraServices,
      excludeInfraServices,
      current,
      stepsLimit,
      nextStep,
      previousStep,
      allComps,
      componentSnapshot,
      updateSelectedService,
      updateInstalledStatus,
      setStepContext,
      setTempData,
      confirmServiceDependencyAction,
      setComponentHosts,
      createService,
      createdPayload,
      attachComponentToService,
      $reset,
      validCardinality: validations.validCardinality
    }
  },
  {
    persist: false
  }
)
