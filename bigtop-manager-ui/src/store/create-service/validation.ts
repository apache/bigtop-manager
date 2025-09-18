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

import { message, Modal } from 'ant-design-vue'
import { useServiceStore } from '@/store/service'
import { useStackStore, type ExpandServiceVO } from '@/store/stack'

export function useValidations() {
  const { t } = useI18n()
  const { confirmModal } = useModal()

  const serviceStore = useServiceStore()
  const stackStore = useStackStore()

  const serviceMap = computed(() => stackStore.stackRelationMap?.services)

  /**
   * Validate services from infrastructure
   * @param targetService - The service being validated
   * @param requireds - List of required services
   * @param infraNames - List of available infrastructure services
   * @returns Whether there are missing services
   */
  function validServiceFromInfra(
    targetService: ExpandServiceVO,
    requireds: string[] | undefined,
    infraNames: string[]
  ) {
    const installedInfra = new Set(serviceStore.getInstalledNamesOrIdsOfServiceByKey('0', 'names'))
    const missingServiceNames = (requireds ?? []).filter(
      (name) => !installedInfra.has(name) && infraNames.includes(name)
    )

    if (missingServiceNames.length === 0) return false

    // Get service's displayName
    const missingDisplayNames = missingServiceNames.map((name) => serviceMap.value?.[name]?.displayName ?? name)

    if (!infraNames.includes(targetService.name!)) {
      message.error(t('service.dependencies_conflict_msg', [targetService.displayName!, missingDisplayNames.join(',')]))
    }
    return true
  }

  /**
   * Confirm dependency addition or removal
   * @param type - The action type ('add' or 'remove')
   * @param targetService - The target service
   * @param requireds - The required service
   * @returns A promise resolving to the user's decision
   */
  function confirmDependencyAddition(
    type: 'add' | 'remove',
    targetService: ExpandServiceVO,
    requireds: ExpandServiceVO
  ) {
    const content = type === 'add' ? 'dependencies_add_msg' : 'dependencies_remove_msg'

    return new Promise((resolve) => {
      confirmModal({
        tipText: t(`service.${content}`, [targetService.displayName, requireds.displayName]),
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

  /**
   * Parse cardinality string into a structured object
   * @param cardinality - The cardinality string
   * @returns Parsed cardinality object
   */
  function parseCardinality(cardinality: string): { min?: number; max?: number } {
    if (/^\d+$/.test(cardinality)) {
      const value = parseInt(cardinality, 10)
      return { min: value, max: value }
    }

    if (/^\d+-\d+$/.test(cardinality)) {
      const [min, max] = cardinality.split('-').map((v) => parseInt(v, 10))
      return { min, max }
    }

    if (/^\d+\+$/.test(cardinality)) {
      const min = parseInt(cardinality.slice(0, -1), 10)
      return { min }
    }

    return {}
  }

  /**
   * Validate cardinality constraints
   * @param cardinality - The cardinality string
   * @param count - The current count
   * @param displayName - The display name of the comp
   * @returns Whether the cardinality is valid
   */
  function validCardinality(cardinality: string, count: number, displayName: string): boolean {
    const { min, max } = parseCardinality(cardinality)

    if (min !== undefined && max !== undefined && (count < min || count > max)) {
      message.error(t('service.range', [displayName, min, max]))
      return false
    }

    if (min !== undefined && max === undefined && count < min) {
      message.error(t('service.minOnly', [displayName, min]))
      return false
    }

    if (min === max && count !== min) {
      message.error(t('service.exact', [displayName, min]))
      return false
    }

    return true
  }

  return {
    confirmDependencyAddition,
    validCardinality,
    validServiceFromInfra
  }
}
