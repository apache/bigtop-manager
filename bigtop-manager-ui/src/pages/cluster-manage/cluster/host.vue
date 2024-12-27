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
  import { TableColumnType } from 'ant-design-vue'
  import { computed, reactive, ref } from 'vue'
  import { generateTableHostData } from './components/mock'
  import useBaseTable from '@/composables/use-base-table'
  import type { FilterConfirmProps, FilterResetProps } from 'ant-design-vue/es/table/interface'
  import type { GroupItem } from '@/components/common/button-group/types'

  type Key = string | number
  interface TableState {
    selectedRowKeys: Key[]
    searchText: string
    searchedColumn: string
  }

  const searchInputRef = ref()
  const data = ref<any[]>(generateTableHostData(50))
  const state = reactive<TableState>({
    searchText: '',
    searchedColumn: '',
    selectedRowKeys: []
  })

  const columns = computed((): TableColumnType[] => [
    {
      title: '节点',
      dataIndex: 'nodeName',
      key: 'nodeName',
      ellipsis: true,
      customFilterDropdown: true,
      onFilter: (value, record) => isContain(record.nodeName, value as string),
      onFilterDropdownOpenChange: (visible) => onFilterDropdownOpenChange(visible)
    },
    {
      title: 'IP地址',
      dataIndex: 'address',
      key: 'address',
      ellipsis: true,
      customFilterDropdown: true,
      onFilter: (value, record) => isContain(record.address, value as string),
      onFilterDropdownOpenChange: (visible) => onFilterDropdownOpenChange(visible)
    },
    {
      title: '系统',
      dataIndex: 'system',
      ellipsis: true
    },
    {
      title: '架构',
      dataIndex: 'architecture',
      ellipsis: true
    },
    {
      title: '组件数',
      dataIndex: 'componentCount',
      ellipsis: true
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: '160px',
      ellipsis: true,
      filters: [
        {
          text: '正常',
          value: 'success'
        },
        {
          text: '异常',
          value: 'error'
        },
        {
          text: '未知',
          value: 'unknow'
        }
      ],
      onFilter: (value, record) => record.status.indexOf(value) === 0
    },
    {
      title: '操作',
      key: 'operation',
      width: '160px',
      fixed: 'right'
    }
  ])

  const { loading, paginationProps, onChange } = useBaseTable({
    columns: columns.value,
    rows: data.value
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

  const isContain = (source: string, target: string) => {
    return source.toString().toLowerCase().includes(target.toLowerCase())
  }

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
    state.searchedColumn = dataIndex as string
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
</script>

<template>
  <div class="host">
    <header>
      <div class="host-title">{{ $t('host.host_list') }}</div>
      <a-space :size="16">
        <a-button type="primary" danger @click="handleDelete">{{ $t('common.bulk_remove') }}</a-button>
        <a-button type="primary">{{ $t('cluster.add_host') }}</a-button>
      </a-space>
    </header>
    <a-table
      :loading="loading"
      :data-source="data"
      :columns="columns"
      :pagination="paginationProps"
      :row-selection="{ selectedRowKeys: state.selectedRowKeys, onChange: onSelectChange }"
      @change="onChange"
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
          <a-space :size="16">
            <a-button type="primary" size="small" @click="handleSearch(selectedKeys, confirm, column.dataIndex)">
              {{ $t('common.search') }}
            </a-button>
            <a-button size="small" @click="handleReset(clearFilters)"> {{ $t('common.reset') }} </a-button>
          </a-space>
        </div>
      </template>
      <template #customFilterIcon="{ filtered, column }">
        <svg-icon v-if="column.key != 'status'" :name="filtered ? 'search_activated' : 'search'" />
        <svg-icon v-else :name="filtered ? 'filter_activated' : 'filter'" />
      </template>
      <template #bodyCell="{ record, column }">
        <template v-if="column.key === 'nodeName'">
          <a-typography-link underline> {{ record.nodeName }} </a-typography-link>
        </template>
        <template v-if="column.key === 'status'">
          <svg-icon :name="record.status" />
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
          ></button-group>
        </template>
      </template>
    </a-table>
  </div>
</template>

<style lang="scss" scoped>
  .host {
    header {
      margin-bottom: $space-md;
    }
    &-title {
      font-size: 14px;
      font-weight: 500;
      line-height: 22px;
      margin-bottom: $space-md;
    }
  }
  .search {
    display: grid;
    gap: $space-sm;
    padding: $space-sm;
  }
</style>
