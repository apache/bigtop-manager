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
import { useValidations } from './validation'
import { cloneDeep } from 'lodash-es'
import { execCommand } from '@/api/command'
import { createKeyedItem } from '@/utils/tools'

import { type ExpandServiceVO, useStackStore } from '@/store/stack'

import type { Property, ServiceConfig, ServiceVO } from '@/api/service/types'
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
            s.configs && (p[s.name] = s.configs)
          }

          return p
        },
        {} as Record<string, any>
      )
    )

    function generateProperty(): Property {
      return createKeyedItem({
        name: '',
        displayName: undefined,
        value: '',
        isManual: true,
        action: 'add'
      })
    }

    function getServiceMap(services: ServiceVO[]) {
      return new Map(services.map((s) => [s.name as string, s as ExpandServiceVO]))
    }

    function injectKeysToConfigs(configs: ServiceConfig[]): ServiceConfig[] {
      return configs.map((c) => {
        if (!c.properties) return c
        return {
          ...c,
          properties: c.properties.map((p) => createKeyedItem(p))
        }
      })
    }

    function injectPropertyKeys(data: ExpandServiceVO[]) {
      return data.map((s) => {
        if (!s.configs) return s

        return {
          ...s,
          configs: injectKeysToConfigs(s.configs)
        }
      })
    }

    function updateSelectedService(data: ExpandServiceVO[], updateSnapshot = false) {
      selectedServices.value = injectPropertyKeys(data)

      if (updateSnapshot) {
        snapshotSelectedServices.value = cloneDeep(toRaw(data))
      }
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

        const propMap: Record<string, Property> = {}

        for (const prop of properties ?? []) {
          if (prop.isManual) continue
          const key = prop.name
          propMap[key] = {
            name: prop.name,
            value: prop.value,
            __key: prop.__key,
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

    const getDiffConfigs = (configs: ServiceConfig[], snapshotConfigs: ServiceConfig[]) => {
      const configMap = generateConfigsMap(snapshotConfigs)
      const filterConfig = [] as ServiceConfig[]

      for (const c of configs) {
        const { name: configName, id } = c
        if (!configName || !configMap[configName]) continue
        const oldPropsMap = configMap[configName]

        const diffProps = (c.properties ?? []).filter((prop) => {
          if (prop.name === '') return false

          if (prop.action === 'delete' || prop.action === 'add') return true

          const oldProp = oldPropsMap[prop.name]
          return oldProp && oldProp.value !== prop.value
        })

        if (diffProps.length > 0) {
          filterConfig.push({
            id,
            name: configName,
            properties: diffProps.map(({ name, value, action }) => ({ name, value, action: action ?? 'update' }))
          })
        }
      }

      return filterConfig
    }

    function extractComponentHosts(components: ComponentVO[]) {
      return components.map((component) => ({
        componentName: component.name,
        hostnames: (component.hosts ?? []).map((host) => host.hostname)
      })) as ComponentHostReq[]
    }

    /**
     * Get modified configuration diffs for uninstalled services.
     *
     * @param services - Uninstalled services to be checked for config changes
     * @returns A list of services with config differences to be sent to backend
     */
    function getUninstalledConfigDiffs(services: ExpandServiceVO[]) {
      const diffs: ServiceCommandReq[] = []

      for (const service of services) {
        const { name, isInstalled, configs, components } = service
        if (isInstalled || !name || !configs) continue

        const snapshot = snapshotSelectedServiceMap.value[name]
        if (!snapshot) continue

        const configDiffs = getDiffConfigs(configs, snapshot)

        diffs.push({
          serviceName: name,
          installed: false,
          componentHosts: extractComponentHosts(components ?? []),
          configs: configDiffs as ServiceConfigReq[]
        })
      }

      return diffs
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
        commandRequest.value.serviceCommands = getUninstalledConfigDiffs(toRaw(selectedServices.value))
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
      confirmServiceDependencyAction,
      setComponentHosts,
      createService,
      createdPayload,
      generateProperty,
      getDiffConfigs,
      injectKeysToConfigs,
      attachComponentToService,
      $reset,
      validCardinality: validations.validCardinality
    }
  },
  {
    persist: false
  }
)
