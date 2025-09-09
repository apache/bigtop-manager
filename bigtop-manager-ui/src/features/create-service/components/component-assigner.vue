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
  import { TableColumnType, Empty } from 'ant-design-vue'
  import { getHosts } from '@/api/host'

  import { useCreateServiceStore } from '@/store/create-service'
  import { useServiceStore } from '@/store/service'

  import TreeSelector from './tree-selector.vue'

  import type { HostVO } from '@/api/host/types'
  import type { FilterConfirmProps, FilterResetProps, TableRowSelection } from 'ant-design-vue/es/table/interface'
  import type { Key } from 'ant-design-vue/es/_util/type'

  interface TableState {
    selectedRowKeys: Key[]
    searchText: string
    searchedColumn: keyof HostVO
  }

  const { t } = useI18n()
  const createStore = useCreateServiceStore()
  const serviceStore = useServiceStore()
  const { stepContext, selectedServices, allComps, componentSnapshot } = storeToRefs(createStore)

  const searchInputRef = ref()
  const currComp = ref<string>('')
  const fieldNames = shallowRef({
    children: 'components',
    title: 'displayName',
    key: 'name'
  })
  const state = reactive<TableState>({
    searchText: '',
    searchedColumn: '',
    selectedRowKeys: []
  })

  const serviceList = computed(() =>
    selectedServices.value.map((v) => ({
      ...v,
      selectable: false
    }))
  )

  const currCompInfo = computed(() => allComps.value.get(currComp.value.split('/')[1]))
  const currCompSnapshot = computed(() => componentSnapshot.value.get(currComp.value.split('/')[1]))
  const installedServices = computed(() =>
    serviceStore.getInstalledNamesOrIdsOfServiceByKey(`${stepContext.value.clusterId}`)
  )
  const hostsOfCurrComp = computed((): HostVO[] => {
    const temp = currComp.value.split('/').at(-1)
    return allComps.value.get(temp!)?.hosts ?? []
  })

  const columns = computed((): TableColumnType<HostVO>[] => [
    {
      title: t('host.hostname'),
      dataIndex: 'hostname',
      key: 'hostname',
      ellipsis: true,
      customFilterDropdown: true
    },
    {
      title: t('host.ip_address'),
      dataIndex: 'ipv4',
      key: 'ipv4',
      ellipsis: true,
      customFilterDropdown: true
    },
    {
      title: t('common.desc'),
      dataIndex: 'desc',
      ellipsis: true
    }
  ])

  const handleSearch = (selectedKeys: Key[], confirm: (param?: FilterConfirmProps) => void, dataIndex: string) => {
    confirm()
    state.searchText = selectedKeys[0] as string
    state.searchedColumn = dataIndex
  }

  const handleReset = (clearFilters: (param?: FilterResetProps) => void) => {
    clearFilters({ confirm: true })
    state.searchText = ''
  }

  const getHostList = async () => {
    loading.value = true
    if (!paginationProps.value) {
      loading.value = false
      return
    }
    try {
      const { clusterId } = stepContext.value
      const res = await getHosts({ ...filtersParams.value, clusterId })
      dataSource.value = res.content.map((v) => ({ ...v, name: v.id, displayName: v.hostname }))
      paginationProps.value.total = res.total
      loading.value = false
    } catch (error) {
      console.error('Error fetching host list:', error)
    } finally {
      loading.value = false
    }
  }

  const { loading, dataSource, filtersParams, paginationProps, onChange } = useBaseTable<HostVO>({
    columns: columns.value,
    rows: [],
    onChangeCallback: getHostList
  })

  const validateHostIsCheck = (host: HostVO) => {
    const { type } = stepContext.value
    const notAdd = (currCompSnapshot.value?.hosts ?? []).findIndex((v) => v.hostname === host.hostname) == -1
    if (type === 'component') {
      return !currCompInfo.value?.uninstall && !notAdd
    } else {
      return installedServices.value.includes(currComp.value.split('/')[0]) && !notAdd
    }
  }

  const getCheckboxProps: TableRowSelection['getCheckboxProps'] = (record) => ({
    disabled: validateHostIsCheck(record),
    name: record.hostname
  })

  const onSelectChange: TableRowSelection['onChange'] = (selectedRowKeys, selectedRows) => {
    const { cardinality, displayName } = currCompInfo.value || {}
    const newCount = selectedRowKeys.length
    const compKey = currComp.value.split('/').at(-1)!
    const isValid = !cardinality || createStore.validCardinality(cardinality, newCount, displayName || '')

    if (isValid || newCount < state.selectedRowKeys.length) {
      state.selectedRowKeys = selectedRowKeys
      if (allComps.value.has(compKey)) {
        createStore.setComponentHosts(currComp.value, selectedRows)
      }
    }
  }

  const resetSelectedRowKeys = (key: string) => {
    const hosts = allComps.value.get(key)?.hosts ?? []
    state.selectedRowKeys = hosts.map((v: HostVO) => v.hostname) as Key[]
  }

  const treeSelectedChange = (keyPath: string) => {
    currComp.value = keyPath ?? ''
    resetSelectedRowKeys(keyPath.split('/')[1])
  }

  onActivated(() => {
    getHostList()
  })
</script>

<template>
  <div class="component-assigner">
    <section>
      <div class="list-title">
        <div>{{ t('service.service_list') }}</div>
      </div>
      <tree-selector :tree="serviceList" :field-names="fieldNames" @change="treeSelectedChange" />
    </section>
    <a-divider type="vertical" class="divider" />
    <section>
      <div class="list-title">
        <div>{{ t('service.select_host') }}</div>
      </div>
      <a-table
        row-key="hostname"
        :loading="loading"
        :data-source="dataSource"
        :columns="columns"
        :pagination="paginationProps"
        :row-selection="{
          selectedRowKeys: state.selectedRowKeys,
          getCheckboxProps: getCheckboxProps,
          onChange: onSelectChange
        }"
        @change="onChange"
      >
        <template #customFilterDropdown="{ setSelectedKeys, selectedKeys, confirm, clearFilters, column }">
          <div class="search">
            <a-input
              ref="searchInputRef"
              :placeholder="t('common.enter_error', [column.title])"
              :value="selectedKeys[0]"
              @change="(e: any) => setSelectedKeys(e.target?.value ? [e.target?.value] : [])"
              @press-enter="handleSearch(selectedKeys, confirm, column.dataIndex)"
            />
            <div class="search-option">
              <a-button size="small" @click="handleReset(clearFilters)">
                {{ t('common.reset') }}
              </a-button>
              <a-button type="primary" size="small" @click="handleSearch(selectedKeys, confirm, column.dataIndex)">
                {{ t('common.search') }}
              </a-button>
            </div>
          </div>
        </template>
      </a-table>
    </section>
    <a-divider type="vertical" class="divider" />
    <section>
      <div class="list-title">
        <div>{{ t('service.host_preview') }}</div>
      </div>
      <div class="preview">
        <a-empty v-if="hostsOfCurrComp.length === 0" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
        <template v-else>
          <div v-for="host in hostsOfCurrComp" :key="host.id">
            {{ host.hostname }}
          </div>
        </template>
      </div>
    </section>
  </div>
</template>

<style lang="scss" scoped>
  .component-assigner {
    display: grid;
    grid-template-columns: 1fr auto 3fr auto 1fr;

    .list-title {
      display: flex;
      height: 32px;
      align-items: center;
      font-weight: 500;
      border-bottom: 1px solid $color-border;
      padding-bottom: 16px;
      margin-bottom: 16px;
    }

    .divider {
      height: 100%;
      margin-inline: 16px;
    }
  }

  .preview {
    display: grid;
    gap: 8px;
    padding-inline: 24px;
  }

  .search {
    display: grid;
    gap: $space-sm;
    padding: $space-sm;

    &-option {
      width: 100%;
      display: grid;
      gap: $space-sm;
      grid-template-columns: 1fr 1fr;

      button {
        width: 100%;
      }
    }
  }
</style>
