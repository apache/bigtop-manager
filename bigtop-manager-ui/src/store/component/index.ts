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

import { defineStore, storeToRefs } from 'pinia'
import { shallowRef, watch } from 'vue'
import { useClusterStore } from '@/store/cluster'
import { HostComponentVO } from '@/api/component/types.ts'
import { getHostComponents } from '@/api/component'

export const useComponentStore = defineStore(
  'component',
  () => {
    const clusterStore = useClusterStore()
    const { clusterId } = storeToRefs(clusterStore)
    const hostComponents = shallowRef<HostComponentVO[]>([])

    watch(clusterId, async () => {
      await loadHostComponents()
    })

    const loadHostComponents = async () => {
      hostComponents.value = await getHostComponents(clusterId.value)
    }

    return { hostComponents, loadHostComponents }
  },
  { persist: false }
)
