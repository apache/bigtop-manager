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

import { ServiceVO } from '@/api/service/types'
import { getStacks } from '@/api/stack'
import { StackVO } from '@/api/stack/types'
import { defineStore } from 'pinia'
import { shallowRef } from 'vue'

export const useStackStore = defineStore(
  'stack',
  () => {
    const stacks = shallowRef<StackVO[]>([])

    const loadStacks = async () => {
      try {
        const data = await getStacks()
        stacks.value = data
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    const getServicesByExclude = (exclude?: string[], isOrder = true): ServiceVO[] => {
      const filterData = stacks.value.flatMap((stack) => (exclude?.includes(stack.stackName) ? [] : stack.services))
      return isOrder ? filterData.map((service, index) => ({ ...service, order: index })) : filterData
    }

    return {
      stacks,
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
