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
  import { useI18n } from 'vue-i18n'
  import useBaseTable from '@/composables/use-base-table'
  import type { ServiceVO } from '@/api/service/types'
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

  const { t } = useI18n()
  const attrs = useAttrs()
  const searchInputRef = ref()
  const hostStatus = ref(['INSTALLING', 'SUCCESS', 'FAILED', 'UNKNOWN'])
  const state = reactive<TableState>({
    searchText: '',
    searchedColumn: '',
    selectedRowKeys: []
  })

  const columns = computed((): TableColumnType<ComponentVO>[] => [
    {
      title: t('common.componentname'),
      dataIndex: 'name',
      key: 'name',
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
      title: t('host.hostname'),
      dataIndex: 'hostname',
      key: 'hostname',
      ellipsis: true,
      customFilterDropdown: true,
      onFilterDropdownOpenChange: (visible) => onFilterDropdownOpenChange(visible)
    },
    {
      title: t('common.quick_link'),
      dataIndex: 'quickLink',
      ellipsis: true
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
      title: t('common.action'),
      key: 'operation',
      width: '160px',
      fixed: 'right'
    }
  ])

  const serviceDetail = computed(() => attrs as unknown as ServiceVO)
  const { loading, dataSource, paginationProps, filtersParams, onChange } = useBaseTable({
    columns: columns.value,
    rows: []
  })

  const operations = computed((): GroupItem[] => [
    {
      text: 'edit',
      clickEvent: (_item, args) => handleEdit(args)
    },
    {
      text: 'remove',
      danger: true,
      clickEvent: (_item, args) => handleDelete(args)
    }
  ])

  const onFilterDropdownOpenChange = (visible: boolean) => {
    if (visible) {
      setTimeout(() => {
        searchInputRef.value.focus()
      }, 100)
    }
  }

  const onSelectChange = (selectedRowKeys: Key[]) => {
    state.selectedRowKeys = selectedRowKeys
  }

  const handleSearch = (selectedKeys: Key[], confirm: (param?: FilterConfirmProps) => void, dataIndex: string) => {
    confirm()
    state.searchText = selectedKeys[0] as string
    state.searchedColumn = dataIndex
  }

  const handleReset = (clearFilters: (param?: FilterResetProps) => void) => {
    clearFilters({ confirm: true })
    state.searchText = ''
  }

  const handleEdit = (row: any) => {
    console.log('row :>> ', row)
  }

  const handleDelete = (row?: any) => {
    if (!row) {
      console.log('selectedRowKeys :>> ', state.selectedRowKeys)
    } else {
      console.log('row :>> ', row)
    }
  }

  const getHostList = async (isReset = false) => {
    console.log('isReset :>> ', isReset)
    console.log('filtersParams.value :>> ', filtersParams.value)
    // loading.value = true
    // if (attrs.id == undefined || !paginationProps.value) {
    //   loading.value = false
    //   return
    // }
    // if (isReset) {
    //   paginationProps.value.current = 1
    // }
    // try {
    //   const res = await getHosts({ ...filtersParams.value, clusterId: attrs.id })
    //   dataSource.value = res.content
    //   paginationProps.value.total = res.total
    //   loading.value = false
    // } catch (error) {
    //   console.log('error :>> ', error)
    // } finally {
    //   loading.value = false
    // }
  }

  const tableChange: TableProps['onChange'] = (pagination, filters, ...args) => {
    onChange(pagination, filters, ...args)
    getHostList()
  }

  onActivated(() => {
    console.log('serviceDetail.value :>> ', serviceDetail.value)
  })
</script>

<template>
  <div class="component">
    <header>
      <div class="header-title">{{ $t('common.component') }}</div>
      <a-space :size="16">
        <a-button type="primary">{{ $t('common.add', [`${$t('common.component')}`]) }}</a-button>
      </a-space>
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
        <svg-icon v-if="column.key != 'status'" :name="filtered ? 'search_activated' : 'search'" />
        <svg-icon v-else :name="filtered ? 'filter_activated' : 'filter'" />
      </template>
      <template #bodyCell="{ record, column }">
        <template v-if="['name', 'status'].includes(column.key)">
          <svg-icon style="margin-left: 0" :name="hostStatus[record.status].toLowerCase()" />
          <span>{{ $t(`common.${hostStatus[record.status].toLowerCase()}`) }}</span>
        </template>
        <template v-if="column.key === 'operation'">
          <button-group
            i18n="common"
            :text-compact="true"
            :space="24"
            :groups="operations"
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
