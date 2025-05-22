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
import { createVNode } from 'vue'
import { useI18n } from 'vue-i18n'
import { useServiceStore } from '@/store/service'
import SvgIcon from '@/components/common/svg-icon/index.vue'
import type { ExpandServiceVO } from '@/store/stack'

export function useValidations() {
  const { t } = useI18n()
  const serviceStore = useServiceStore()

  // Validate services from infra
  function validServiceFromInfra(
    targetService: ExpandServiceVO,
    requiredServices: string[],
    infraServiceNames: string[]
  ) {
    const installedInfra = serviceStore.getInstalledNamesOrIdsOfServiceByKey('0', 'names')
    const set = new Set(installedInfra)
    const missServices = requiredServices.reduce((acc, name) => {
      !set.has(name) && infraServiceNames.includes(name) && acc.push(name)
      return acc
    }, [] as string[])

    if (missServices.length === 0) return false
    message.error(t('service.dependencies_conflict_msg', [targetService.displayName!, missServices.join(',')]))
    return true
  }

  function confirmDependencyAddition(
    type: 'add' | 'remove',
    targetService: ExpandServiceVO,
    requiredService: ExpandServiceVO
  ) {
    const content = type === 'add' ? 'dependencies_add_msg' : 'dependencies_remove_msg'
    return new Promise((resolve) => {
      Modal.confirm({
        content: t(`service.${content}`, [targetService.displayName, requiredService.displayName]),
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

  function validCardinality(cardinality: string, count: number, displayName: string): boolean {
    if (/^\d+$/.test(cardinality)) {
      const expected = parseInt(cardinality, 10)
      if (count != expected) {
        message.error(t('service.exact', [displayName, expected]))
        return false
      }
    }

    if (/^\d+-\d+$/.test(cardinality)) {
      const [minStr, maxStr] = cardinality.split('-')
      const min = parseInt(minStr, 10)
      const max = parseInt(maxStr, 10)
      if (count < min || count > max) {
        message.error(t('service.range', [displayName, min, max]))
        return false
      }
    }

    if (/^\d+\+$/.test(cardinality)) {
      const min = parseInt(cardinality.slice(0, -1), 10)
      if (count < min) {
        message.error(t('service.minOnly', [displayName, min]))
        return false
      }
    }

    return true
  }

  return {
    confirmDependencyAddition,
    validCardinality,
    validServiceFromInfra
  }
}
