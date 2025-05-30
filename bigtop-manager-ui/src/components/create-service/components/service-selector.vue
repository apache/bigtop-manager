<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements.  See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership.  The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License.  You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
-->

<script setup lang="ts">
  import { computed, onActivated, reactive, ref, shallowRef, toRefs } from 'vue'
  import { usePngImage } from '@/utils/tools'
  import { useStackStore } from '@/store/stack'
  import { useServiceStore } from '@/store/service'
  import { useCreateServiceStore } from '@/store/create-service'
  import { storeToRefs } from 'pinia'
  import type { ExpandServiceVO } from '@/store/stack'
  import type { ComponentVO } from '@/api/component/types.ts'
  import type { ServiceVO } from '@/api/service/types'

  interface State {
    isAddableData: ExpandServiceVO[]
    selectedData: ExpandServiceVO[]
  }

  const stackStore = useStackStore()
  const createStore = useCreateServiceStore()
  const serviceStore = useServiceStore()
  const searchStr = ref('')
  const spinning = ref(false)
  const licenseOfConflictService = shallowRef(['AGPL-3.0', 'GPLv2'])
  const state = reactive<State>({
    isAddableData: [],
    selectedData: []
  })
  const { stepContext, selectedServices, infraServices, excludeInfraServices } = storeToRefs(createStore)
  const { isAddableData } = toRefs(state)
  const targetServiceName = computed(() => serviceStore.serviceFlatMap[stepContext.value.serviceId].name!)
  const checkIfInstalled = computed(() => selectedServices.value.filter((v) => !v.isInstalled).length === 0)
  const filterAddableData = computed(() =>
    isAddableData.value.filter(
      (v) =>
        v.displayName?.toString().toLowerCase().includes(searchStr.value.toString().toLowerCase()) ||
        v.desc?.toString().toLowerCase().includes(searchStr.value.toString().toLowerCase())
    )
  )

  const insertByOrder = <T extends { order: number }>(array: T[], item: T) => {
    const index = findInsertIndex(array, item.order)
    array.splice(index, 0, item)
  }

  const moveItem = <T extends { name?: string; order: number }>(from: T[], to: T[], item: T, key: keyof T = 'name') => {
    const index = from.findIndex((v) => v[key] === item[key])
    if (index !== -1) {
      const [removedItem] = from.splice(index, 1)
      insertByOrder(to, removedItem)
    }
  }

  //  Binary search
  const findInsertIndex = <T extends { order: number }>(array: T[], order: number) => {
    let low = 0
    let high = array.length

    while (low < high) {
      const mid = (low + high) >>> 1
      if (array[mid].order < order) {
        low = mid + 1
      } else {
        high = mid
      }
    }
    return low
  }

  const handleInstallItem = (item: ExpandServiceVO, from: ExpandServiceVO[], to: ExpandServiceVO[]) => {
    item.components = item.components?.map((v) => ({ ...v, hosts: [] }))
    moveItem(from, to, item)
    createStore.updateSelectedService(state.selectedData)
  }

  const modifyInstallItems = async (type: 'add' | 'remove', item: ExpandServiceVO) => {
    const items = await createStore.confirmServiceDependencyAction(type, item)
    if (items.length > 0) {
      items.forEach((i) => {
        if (type === 'add') {
          handleInstallItem(i, state.isAddableData, state.selectedData)
        } else {
          handleInstallItem(i, state.selectedData, state.isAddableData)
        }
      })
    }
  }

  const splitSearchStr = (splitStr: string) => {
    return splitStr.toString().split(new RegExp(`(?<=${searchStr.value})|(?=${searchStr.value})`, 'i'))
  }

  const mergeComponents = (components: ComponentVO[]) => {
    return Object.values(
      components.reduce((acc, item) => {
        const { name, hostname } = item
        if (!acc[name!]) {
          acc[name!] = {
            ...item,
            hosts: [{ hostname, name: hostname, displayName: hostname }],
            cardinality: stackStore.stackRelationMap?.components[item.name!].cardinality
          }
        } else {
          acc[name!].hosts.push({ hostname, name: hostname, displayName: hostname })
        }
        return acc
      }, {})
    )
  }

  const initInstalledServicesDetail = async () => {
    const clusterId = stepContext.value.clusterId
    const detailRes = await serviceStore.getInstalledServicesDetailByKey(`${clusterId}`)
    const detailMap = new Map<string, ExpandServiceVO>()
    if (!detailRes) {
      return detailMap
    } else {
      return detailRes.reduce(
        (pre, val) =>
          pre.set(val.name!, {
            ...val,
            components: mergeComponents(val.components || [])
          } as ExpandServiceVO),
        detailMap
      )
    }
  }

  const buildCompleteServiceInfo = (
    installedServiceMap: Map<string, ExpandServiceVO>,
    inherentServices: ServiceVO[]
  ) => {
    const inherent = inherentServices.find((v) => v.name === targetServiceName.value)!
    const installed = { ...installedServiceMap.get(targetServiceName.value)! }
    const mergedComponentsMap = new Map(
      (installed.components ?? []).map((component) => [component.name, { ...component }])
    )
    for (const component of inherent.components ?? []) {
      if (!mergedComponentsMap.has(component.name)) {
        mergedComponentsMap.set(component.name, { ...component, hosts: [], uninstall: true })
      }
    }
    installed.components = [...mergedComponentsMap.values()]
    installed.license = inherent.license

    return installed as ExpandServiceVO
  }

  const markSelectedAsInstalled = async (hasInstalled: boolean) => {
    const { type, creationMode, clusterId } = stepContext.value
    if (!hasInstalled) {
      state.selectedData = [...selectedServices.value]
      return
    }
    const installedServiceMap = await initInstalledServicesDetail()
    const installedServiceNames = serviceStore.getInstalledNamesOrIdsOfServiceByKey(`${clusterId}`)
    const rawServices = creationMode === 'internal' ? excludeInfraServices.value : infraServices.value
    // Clone to prevent mutation of original data
    const inherentServices = rawServices.map((s) => ({ ...s }))

    if (type === 'component' && installedServiceMap.has(targetServiceName.value!)) {
      const completeService = buildCompleteServiceInfo(installedServiceMap, inherentServices)
      state.selectedData[0] = { ...completeService, isInstalled: true }
      state.isAddableData = []
    } else {
      for (const service of inherentServices) {
        const name = service.name || ''
        if (installedServiceNames.includes(name)) {
          const installed = installedServiceMap.get(name)
          if (installed) {
            Object.assign(service, installed)
            service.isInstalled = true
          }
          state.selectedData.push(service as ExpandServiceVO)
        } else {
          state.isAddableData.push(service as ExpandServiceVO)
        }
      }
    }

    createStore.updateSelectedService(state.selectedData)
    createStore.setTempData(state.selectedData)
  }

  onActivated(async () => {
    spinning.value = true
    markSelectedAsInstalled(checkIfInstalled.value).finally(() => {
      spinning.value = false
    })
  })

  defineExpose({
    modifyInstallItems
  })
</script>

<template>
  <div class="service-selector">
    <a-spin :spinning="spinning">
      <div class="list-title">
        <div>{{ $t('service.select_service') }}</div>
        <a-input v-model:value="searchStr" :placeholder="$t('service.please_enter_search_keyword')" />
      </div>
      <a-list item-layout="horizontal" :data-source="filterAddableData">
        <template #renderItem="{ item }">
          <a-list-item>
            <template #actions>
              <a-button type="primary" @click="modifyInstallItems('add', item)">{{ $t('common.add') }}</a-button>
            </template>
            <a-list-item-meta>
              <template #title>
                <div class="item-name-wrp">
                  <div class="ellipsis item-name" :title="item.displayName">
                    <template v-for="(fragment, i) in splitSearchStr(item.displayName)">
                      <mark v-if="fragment.toLowerCase() === searchStr.toLowerCase()" :key="i" class="highlight">
                        {{ fragment }}
                      </mark>
                      <template v-else>{{ fragment }}</template>
                    </template>
                  </div>
                  <a-tag :color="licenseOfConflictService.includes(item.license) ? 'error' : 'success'">
                    <span class="item-tag">{{ item.license }}</span>
                  </a-tag>
                </div>
              </template>
              <template #description>
                <div class="ellipsis" :title="item.desc">
                  <template v-for="(fragment, i) in splitSearchStr(item.desc)">
                    <mark v-if="fragment.toLowerCase() === searchStr.toLowerCase()" :key="i" class="highlight">
                      {{ fragment }}
                    </mark>
                    <template v-else>{{ fragment }}</template>
                  </template>
                </div>
              </template>
              <template #avatar>
                <a-avatar
                  v-if="item.displayName"
                  :src="usePngImage(item.displayName.toLowerCase())"
                  :size="54"
                  class="header-icon"
                />
              </template>
            </a-list-item-meta>
          </a-list-item>
        </template>
      </a-list>
    </a-spin>
    <a-divider type="vertical" class="divider" />
    <a-spin :spinning="spinning">
      <div class="list-title">
        <div>{{ $t('service.pending_installation_services') }}</div>
      </div>
      <a-list item-layout="horizontal" :data-source="state.selectedData">
        <template #renderItem="{ item }">
          <a-list-item>
            <template #actions>
              <a-button danger :disabled="item.isInstalled" type="primary" @click="modifyInstallItems('remove', item)">
                {{ $t('common.remove') }}
              </a-button>
            </template>
            <a-list-item-meta>
              <template #title>
                <div class="item-name-wrp">
                  <div class="ellipsis item-name" :data-tooltip="item.displayName">
                    {{ item.displayName }}
                  </div>
                  <a-tag :color="licenseOfConflictService.includes(item.license) ? 'error' : 'success'">
                    <span class="item-tag">{{ item.license }}</span>
                  </a-tag>
                </div>
              </template>
              <template #description>
                <div class="ellipsis" :data-tooltip="item.desc">
                  {{ item.desc }}
                </div>
              </template>
              <template #avatar>
                <a-avatar
                  v-if="item.displayName"
                  :src="usePngImage(item.displayName.toLowerCase())"
                  :size="54"
                  class="header-icon"
                />
              </template>
            </a-list-item-meta>
          </a-list-item>
        </template>
      </a-list>
    </a-spin>
  </div>
</template>

<style lang="scss" scoped>
  .highlight {
    background-color: rgb(255, 192, 105);
    padding: 0px;
  }

  .item-name-wrp {
    display: flex;
    gap: 8px;
    justify-content: space-between;
    flex-wrap: wrap;
    .item-name {
      flex: 1;
      font-size: 16px;
    }
    .item-tag {
      font-weight: normal;
      line-height: 22px;
    }
  }

  .service-selector {
    display: grid;
    grid-template-columns: 1fr auto 1fr;
    grid-template-rows: auto;
    justify-content: space-between;

    .list-title {
      display: flex;
      height: 32px;
      align-items: center;
      justify-content: space-between;
      font-weight: 500;
      border-bottom: 1px solid $color-border;
      padding-bottom: 16px;

      .ant-input {
        flex: 0 1 160px;
      }
    }

    .ant-list {
      max-height: 500px;
      overflow: auto;
    }
  }

  .divider {
    height: 100%;
    margin-inline: 16px;
  }

  :deep(.ant-avatar) {
    border-radius: 4px;

    img {
      object-fit: contain !important;
    }
  }
</style>
