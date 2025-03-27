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
  import { computed, onActivated, reactive, ref, useAttrs } from 'vue'
  import { storeToRefs } from 'pinia'
  import { useI18n } from 'vue-i18n'
  import { useRoute } from 'vue-router'
  import { getComponents } from '@/api/component'
  import { useStackStore } from '@/store/stack'
  import useBaseTable from '@/composables/use-base-table'
  import type { GroupItem } from '@/components/common/button-group/types'
  import type { TableColumnType, TableProps } from 'ant-design-vue'
  import type { ComponentVO } from '@/api/component/types'
  import type { FilterConfirmProps, FilterResetProps } from 'ant-design-vue/es/table/interface'

  type Key = string | number
  interface TableState {
    selectedRowKeys: Key[]
    searchText: string
    searchedColumn: keyof ComponentVO
  }

  interface ServieceInfo {
    cluster: string
    id: number
    service: string
    serviceId: number
  }

  const { t } = useI18n()
  const stackStore = useStackStore()
  const route = useRoute()
  const attrs = useAttrs()
  const { stacks } = storeToRefs(stackStore)
  const searchInputRef = ref()
  const componentStatus = ref(['INSTALLING', 'SUCCESS', 'FAILED', 'UNKNOWN'])
  const state = reactive<TableState>({
    searchText: '',
    searchedColumn: '',
    selectedRowKeys: []
  })

  const currServiceInfo = computed(() => route.params as unknown as ServieceInfo)
  const componentsFromStack = computed(
    () =>
      new Map(
        stacks.value.flatMap(
          (stack) =>
            stack.services?.flatMap((service) =>
              service.name && service.components ? [[service.name, service.components]] : []
            ) ?? []
        )
      )
  )

  const columns = computed((): TableColumnType<ComponentVO>[] => [
    {
      title: t('common.componentname'),
      dataIndex: 'displayName',
      key: 'name',
      ellipsis: true,
      filterMultiple: false,
      filters: [...(componentsFromStack.value.get(currServiceInfo.value.service)?.values() || [])]?.map((v) => ({
        text: v?.displayName || '',
        value: v?.name || ''
      }))
    },
    {
      title: t('host.hostname'),
      dataIndex: 'hostname',
      key: 'hostname',
      ellipsis: true,
      customFilterDropdown: true
    },
    {
      title: t('common.status'),
      dataIndex: 'status',
      key: 'status',
      width: '160px',
      ellipsis: true,
      filterMultiple: false,
      filters: [
        {
          text: t('common.success'),
          value: 1
        },
        {
          text: t('common.failed'),
          value: 2
        },
        {
          text: t('common.unknown'),
          value: 3
        }
      ]
    },
    {
      title: t('common.quick_link'),
      key: 'quickLink',
      dataIndex: 'quickLink',
      ellipsis: true
    },
    {
      title: t('common.action'),
      width: 180,
      key: 'operation',
      fixed: 'right'
    }
  ])

  const { loading, dataSource, paginationProps, filtersParams, onChange, resetState } = useBaseTable<ComponentVO>({
    columns: columns.value,
    rows: []
  })

  const operations = computed((): GroupItem[] => [
    {
      text: 'start',
      clickEvent: (_item, args) => handleStart(args)
    },
    {
      text: 'stop',
      clickEvent: (_item, args) => handleStop(args)
    },
    {
      text: 'restart',
      clickEvent: (_item, args) => handleRestart(args)
    },
    {
      text: 'remove',
      danger: true,
      clickEvent: (_item, args) => handleDelete(args)
    }
  ])

  const batchOperations = computed((): GroupItem[] => [
    {
      text: t('common.batch_operation'),
      type: 'primary',
      dropdownMenu: [
        {
          action: 'Start',
          text: t('common.start', [t('common.all')])
        },
        {
          action: 'Restart',
          text: t('common.restart', [t('common.all')])
        },
        {
          action: 'Stop',
          text: t('common.stop', [t('common.all')])
        }
      ],
      dropdownMenuClickEvent: (info) => dropdownMenuClick && dropdownMenuClick(info)
    }
  ])

  const getTableOperations = (status: number): GroupItem[] => {
    if (status === 1) {
      return operations.value.filter((v) => ['stop', 'restart', 'remove'].includes(v.text!))
    } else if (status === 2) {
      return operations.value.filter((v) => ['start', 'remove'].includes(v.text!))
    } else {
      return operations.value.filter((v) => v.text === 'remove')
    }
  }

  const dropdownMenuClick: GroupItem['dropdownMenuClickEvent'] = async ({ key }) => {
    console.log('key :>> ', key)
  }

  const onSelectChange = (selectedRowKeys: Key[]) => {
    state.selectedRowKeys = selectedRowKeys
  }

  const handleSearch = (selectedKeys: Key[], confirm: (param?: FilterConfirmProps) => void, dataIndex: string) => {
    confirm()
    state.searchText = selectedKeys[0] as string
    state.searchedColumn = dataIndex
    resetState()
  }

  const handleReset = (clearFilters: (param?: FilterResetProps) => void) => {
    clearFilters({ confirm: true })
    state.searchText = ''
    resetState()
  }

  const handleStart = (row?: any) => {
    if (!row) {
      console.log('selectedRowKeys :>> ', state.selectedRowKeys)
    } else {
      console.log('row :>> ', row)
    }
  }

  const handleStop = (row?: any) => {
    if (!row) {
      console.log('selectedRowKeys :>> ', state.selectedRowKeys)
    } else {
      console.log('row :>> ', row)
    }
  }

  const handleRestart = (row?: any) => {
    if (!row) {
      console.log('selectedRowKeys :>> ', state.selectedRowKeys)
    } else {
      console.log('row :>> ', row)
    }
  }

  const handleDelete = (row?: any) => {
    if (!row) {
      console.log('selectedRowKeys :>> ', state.selectedRowKeys)
    } else {
      console.log('row :>> ', row)
    }
  }

  const getComponentList = async (isReset = false) => {
    const { id: clusterId, serviceId } = currServiceInfo.value
    loading.value = true
    if (attrs.id == undefined || !paginationProps.value) {
      loading.value = false
      return
    }
    if (isReset) {
      paginationProps.value.current = 1
    }
    try {
      const res = await getComponents({ ...filtersParams.value, clusterId, serviceId })
      dataSource.value = res.content
      paginationProps.value.total = res.total
      loading.value = false
    } catch (error) {
      console.log('error :>> ', error)
    } finally {
      loading.value = false
    }
  }

  const tableChange: TableProps['onChange'] = (pagination, filters, ...args) => {
    onChange(pagination, filters, ...args)
    getComponentList()
  }

  onActivated(() => {
    getComponentList(true)
  })
</script>

<template>
  <div class="component">
    <header>
      <div class="header-title">{{ $t('common.component') }}</div>
      <div class="list-operation">
        <a-button type="primary">{{ $t('common.add', [`${$t('common.component')}`]) }}</a-button>
        <button-group :space="24" :groups="batchOperations" group-shape="default" />
      </div>
    </header>
    <a-table
      :loading="loading"
      :data-source="dataSource"
      :columns="columns"
      :pagination="paginationProps"
      :row-selection="{ selectedRowKeys: state.selectedRowKeys, onChange: onSelectChange }"
      @change="tableChange"
    >
      <template #customFilterDropdown="{ setSelectedKeys, selectedKeys, confirm, clearFilters, column }">
        <div class="search">
          <a-input
            ref="searchInputRef"
            :placeholder="$t('common.enter_error', [column.title])"
            :value="selectedKeys[0]"
            @change="(e: any) => setSelectedKeys(e.target?.value ? [e.target?.value] : [])"
            @press-enter="handleSearch(selectedKeys, confirm, column.dataIndex)"
          />
          <div class="search-option">
            <a-button size="small" @click="handleReset(clearFilters)">
              {{ $t('common.reset') }}
            </a-button>
            <a-button type="primary" size="small" @click="handleSearch(selectedKeys, confirm, column.dataIndex)">
              {{ $t('common.search') }}
            </a-button>
          </div>
        </div>
      </template>
      <template #customFilterIcon="{ filtered, column }">
        <svg-icon v-if="!['name', 'status'].includes(column.key)" :name="filtered ? 'search_activated' : 'search'" />
        <svg-icon v-else :name="filtered ? 'filter_activated' : 'filter'" />
      </template>
      <template #bodyCell="{ record, column }">
        <template v-if="['status'].includes(column.key)">
          <svg-icon style="margin-left: 0" :name="componentStatus[record.status].toLowerCase()" />
          <span>{{ $t(`common.${componentStatus[record.status].toLowerCase()}`) }}</span>
        </template>
        <template v-if="column.key === 'quickLink'">
          <span v-if="!record.quickLink">{{ $t('common.no_link') }}</span>
          <a-typography-link v-else :href="record.quickLink.url" target="_blank">
            {{ record.quickLink.displayName }}
          </a-typography-link>
        </template>
        <template v-if="column.key === 'operation'">
          <button-group
            i18n="common"
            :text-compact="true"
            :space="24"
            :groups="getTableOperations(record.status)"
            :args="record"
            group-shape="default"
            group-type="link"
          />
        </template>
      </template>
    </a-table>
  </div>
</template>

<style lang="scss" scoped>
  .component {
    .list-operation {
      display: flex;
      justify-content: space-between;
    }
  }
  header {
    margin-bottom: $space-md;
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
