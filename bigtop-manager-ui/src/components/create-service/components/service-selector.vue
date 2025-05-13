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
  import useCreateService from './use-create-service'
  import type { ExpandServiceVO } from '@/store/stack'
  import type { ComponentVO } from '@/api/component/types.ts'
  import type { ServiceVO } from '@/api/service/types'

  interface State {
    isAddableData: ExpandServiceVO[]
    selectedData: ExpandServiceVO[]
  }

  const searchStr = ref('')
  const licenseOfConflictService = shallowRef(['AGPL-3.0', 'GPLv2'])
  const state = reactive<State>({
    isAddableData: [],
    selectedData: []
  })
  const {
    clusterId,
    routeParams,
    selectedServices,
    servicesOfInfra,
    servicesOfExcludeInfra,
    serviceStore,
    creationMode,
    creationModeType,
    confirmServiceDependencies,
    setDataByCurrent
  } = useCreateService()
  const { isAddableData } = toRefs(state)
  const checkSelectedServicesOnlyInstalled = computed(
    () => selectedServices.value.filter((v: ExpandServiceVO) => !v.isInstalled).length === 0
  )
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
    setDataByCurrent(state.selectedData)
  }

  const modifyInstallItems = async (type: 'add' | 'remove', item: ExpandServiceVO) => {
    const items = await confirmServiceDependencies(type, item)
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
            hosts: [{ hostname, name: hostname, displayName: hostname }]
          }
        } else {
          acc[name!].hosts.push({ hostname, name: hostname, displayName: hostname })
        }
        return acc
      }, {})
    )
  }

  const initInstalledServicesDetail = async () => {
    const detailRes = await serviceStore.getInstalledServicesDetailByKey(`${clusterId.value}`)
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

  const mergeInherentServiceAndInstalledService = (
    installedServiceMap: Map<string, ExpandServiceVO>,
    inherentServices: ServiceVO[]
  ) => {
    const inherentService = inherentServices.filter((v) => v.name === routeParams.value.service)[0]
    const installedService = { ...installedServiceMap.get(routeParams.value.service)! }
    installedService.license = inherentService.license
    const map = new Map(installedService.components!.map((item) => [item.name, item]))
    inherentService.components!.forEach((item) => {
      !map.has(item.name) && map.set(item.name, { ...item, hosts: [], uninstall: true })
    })
    installedService.components = [...map.values()]
    return installedService
  }

  const addInstalledSymbolForSelectedServices = async (onlyInstalled: boolean) => {
    if (onlyInstalled) {
      const installedServiceMap = await initInstalledServicesDetail()
      const installedServiceNames = serviceStore.getInstalledNamesOrIdsOfServiceByKey(`${clusterId.value}`)
      const inherentServices = creationMode.value === 'internal' ? servicesOfExcludeInfra.value : servicesOfInfra.value

      if (creationModeType.value === 'component' && installedServiceMap.has(routeParams.value.service)) {
        state.selectedData[0] = mergeInherentServiceAndInstalledService(installedServiceMap, inherentServices)
        state.selectedData[0].isInstalled = true
        state.isAddableData = []
      } else {
        for (const service of inherentServices) {
          if (installedServiceNames.includes(service.name || '')) {
            Object.assign(service, installedServiceMap.get(service.name!))
            service.isInstalled = true
            state.selectedData.push(service as ExpandServiceVO)
          } else {
            state.isAddableData.push(service as ExpandServiceVO)
          }
        }
      }
      setDataByCurrent(state.selectedData)
    } else {
      state.selectedData = [...selectedServices.value]
    }
  }

  onActivated(async () => {
    await addInstalledSymbolForSelectedServices(checkSelectedServicesOnlyInstalled.value)
  })

  defineExpose({
    modifyInstallItems
  })
</script>

<template>
  <div class="service-selector">
    <div>
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
    </div>
    <a-divider type="vertical" class="divider" />
    <div>
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
    </div>
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
