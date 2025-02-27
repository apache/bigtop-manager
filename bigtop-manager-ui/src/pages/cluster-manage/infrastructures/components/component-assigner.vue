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
  import { HostVO } from '@/api/hosts/types'
  import { TableColumnType, TableProps } from 'ant-design-vue'
  import { FilterConfirmProps, FilterResetProps, TableRowSelection } from 'ant-design-vue/es/table/interface'
  import { computed, onActivated, reactive, ref } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { getHosts } from '@/api/hosts'
  import { useRoute } from 'vue-router'
  import Sidebar from './sidebar.vue'
  import useBaseTable from '@/composables/use-base-table'

  type Key = string | number

  interface TableState {
    selectedRowKeys: Key[]
    searchText: string
    searchedColumn: keyof HostVO
  }

  defineProps<{ stepData: any }>()
  defineEmits(['update'])

  const { t } = useI18n()
  const route = useRoute()
  const searchInputRef = ref()
  const state = reactive<TableState>({
    searchText: '',
    searchedColumn: '',
    selectedRowKeys: []
  })
  const columns = computed((): TableColumnType<HostVO>[] => [
    {
      title: t('host.hostname'),
      dataIndex: 'hostname',
      key: 'hostname',
      ellipsis: true,
      customFilterDropdown: true,
      onFilterDropdownOpenChange: (visible) => onFilterDropdownOpenChange(visible)
    },
    {
      title: t('host.ip_address'),
      dataIndex: 'ipv4',
      key: 'ipv4',
      ellipsis: true,
      customFilterDropdown: true,
      onFilterDropdownOpenChange: (visible) => onFilterDropdownOpenChange(visible)
    },
    {
      title: t('common.desc'),
      dataIndex: 'desc',
      ellipsis: true
    }
  ])

  const onFilterDropdownOpenChange = (visible: boolean) => {
    if (visible) {
      setTimeout(() => {
        searchInputRef.value.focus()
      }, 100)
    }
  }

  const onSelectChange: TableRowSelection['onChange'] = (selectedRowKeys, selectedRows) => {
    console.log('selectedRows :>> ', selectedRows)
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

  const { loading, dataSource, filtersParams, paginationProps, onChange } = useBaseTable<HostVO>({
    columns: columns.value,
    rows: []
  })

  const tableChange: TableProps['onChange'] = (pagination, filters, ...args) => {
    onChange(pagination, filters, ...args)
    getHostList()
  }

  const getHostList = async () => {
    loading.value = true
    const clusterId = route.query.id as unknown as number
    if (clusterId || !paginationProps.value) {
      loading.value = false
      return
    }
    try {
      const res = await getHosts({ ...filtersParams.value, clusterId })
      dataSource.value = res.content
      paginationProps.value.total = res.total
      loading.value = false
    } catch (error) {
      console.log('error :>> ', error)
    } finally {
      loading.value = false
    }
  }

  onActivated(() => {
    getHostList()
  })
</script>

<template>
  <div class="component-assigner">
    <section>
      <div class="list-title">
        <div>{{ $t('service.service_list') }}</div>
      </div>
      <sidebar
        :data="$props.stepData[0].map((v: any) => ({ ...v, selectable: false }))"
        :field-names="{
          children: 'components',
          title: 'name',
          key: 'name'
        }"
      />
    </section>
    <a-divider type="vertical" class="divider" />
    <section>
      <div class="list-title">
        <div>{{ $t('service.select_host') }}</div>
      </div>
      <a-table
        row-key="id"
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
      </a-table>
    </section>
    <a-divider type="vertical" class="divider" />
    <section>
      <div class="list-title">
        <div>{{ $t('service.host_preview') }}</div>
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
