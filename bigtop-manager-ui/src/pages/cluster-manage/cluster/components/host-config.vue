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
  import { computed, onMounted, reactive, ref } from 'vue'
  import useBaseTable from '@/composables/use-base-table'
  import type { FilterConfirmProps, FilterResetProps } from 'ant-design-vue/es/table/interface'
  import type { GroupItem } from '@/components/common/button-group/types'
  import { getHosts } from '@/api/hosts'
  import { HostVO } from '@/api/hosts/types'
  import HostCreate from '@/pages/cluster-manage/hosts/create.vue'

  type Key = string | number
  interface TableState {
    selectedRowKeys: Key[]
    searchText: string
    searchedColumn: string
  }

  const searchInputRef = ref()
  // const checkAllHostIsSuccessfull = ref(false)
  const data = ref<HostVO[]>()
  const hostCreateRef = ref<InstanceType<typeof HostCreate> | null>(null)
  const state = reactive<TableState>({
    searchText: '',
    searchedColumn: '',
    selectedRowKeys: []
  })

  const columns = computed((): TableColumnType[] => [
    {
      title: '主机名',
      dataIndex: 'name',
      key: 'name',
      ellipsis: true,
      customFilterDropdown: true,
      onFilter: (value, record) => isContain(record.name, value as string),
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
      title: '备注',
      dataIndex: 'remark',
      width: '20%',
      ellipsis: true
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: '260px',
      ellipsis: true
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
      clickEvent: (_item, args) => deleteHost(args)
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

  // const handleInstallDependencies = () => {
  //   console.log('selectedRowKeys :>> ', state.selectedRowKeys)
  // }

  const handleSearch = (selectedKeys: Key[], confirm: (param?: FilterConfirmProps) => void, dataIndex: string) => {
    confirm()
    state.searchText = selectedKeys[0] as string
    state.searchedColumn = dataIndex as string
  }

  const handleReset = (clearFilters: (param?: FilterResetProps) => void) => {
    clearFilters({ confirm: true })
    state.searchText = ''
  }

  const addHost = () => {
    hostCreateRef.value?.handleOpen()
  }

  const handleEdit = (row: any) => {
    console.log('row :>> ', row)
  }

  const deleteHost = (row?: any) => {
    if (!row) {
      console.log('selectedRowKeys :>> ', state.selectedRowKeys)
    } else {
      console.log('row :>> ', row)
    }
  }

  const getHostList = async () => {
    try {
      loading.value = true
      const { content } = await getHosts()
      data.value = content
    } catch (error) {
      console.log('error :>> ', error)
    } finally {
      loading.value = false
    }
  }

  onMounted(() => {
    getHostList()
  })
</script>

<template>
  <div class="host-config">
    <header>
      <a-space :size="16">
        <a-button type="primary" @click="addHost">{{ $t('cluster.add_host') }}</a-button>
        <a-button type="primary" danger @click="deleteHost">{{ $t('common.bulk_remove') }}</a-button>
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
      <template #customFilterIcon="{ filtered }">
        <svg-icon :name="filtered ? 'search_active' : 'search'" />
      </template>
      <template #bodyCell="{ record, column }">
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
    <host-create ref="hostCreateRef" />
  </div>
</template>

<style lang="scss" scoped>
  .host-config {
    header {
      @include flexbox($justify: space-between);
      margin-bottom: $space-md;
    }
  }
  .search {
    display: grid;
    gap: $space-sm;
    padding: $space-sm;
  }

  .highlight {
    background-color: rgb(255, 192, 105);
    padding: 0px;
  }
</style>
