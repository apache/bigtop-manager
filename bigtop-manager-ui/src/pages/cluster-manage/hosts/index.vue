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
  import { message, Modal, TableColumnType } from 'ant-design-vue'
  import { computed, onMounted, reactive, ref } from 'vue'
  import { useI18n } from 'vue-i18n'
  import * as hostApi from '@/api/hosts'
  import useBaseTable from '@/composables/use-base-table'
  import HostCreate from '@/pages/cluster-manage/hosts/create.vue'
  import InstallDependencies from './install-dependencies.vue'
  import { HostReq } from '@/api/command/types'
  import { useClusterStore } from '@/store/cluster'
  import type { FilterConfirmProps, FilterResetProps, TableRowSelection } from 'ant-design-vue/es/table/interface'
  import type { GroupItem } from '@/components/common/button-group/types'
  import type { HostVO } from '@/api/hosts/types'

  type Key = string | number
  interface TableState {
    selectedRowKeys: Key[]
    searchText: string
    searchedColumn: string
  }
  const { t } = useI18n()
  const searchInputRef = ref()
  const clusterStore = useClusterStore()
  const hostCreateRef = ref<InstanceType<typeof HostCreate> | null>(null)
  const installRef = ref<InstanceType<typeof InstallDependencies> | null>(null)
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
      title: t('common.cluster'),
      key: 'clusterName',
      dataIndex: 'clusterName',
      ellipsis: true
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
      key: 'os',
      title: t('common.os'),
      dataIndex: 'os',
      ellipsis: true
    },
    {
      title: t('common.arch'),
      dataIndex: 'arch',
      ellipsis: true
    },
    {
      title: t('host.component_count'),
      dataIndex: 'componentNum',
      key: 'componentNum',
      width: '180px',
      ellipsis: true
    },
    {
      title: t('common.status'),
      dataIndex: 'status',
      width: '260px',
      ellipsis: true
    },
    {
      title: t('common.action'),
      key: 'operation',
      width: '160px',
      fixed: 'right'
    }
  ])

  const operations = computed((): GroupItem[] => [
    {
      text: 'edit',
      clickEvent: (_item, args) => editHost(args)
    },
    {
      text: 'remove',
      danger: true,
      clickEvent: (_item, args) => deleteHost(args)
    }
  ])

  const { loading, dataSource, filtersParams, paginationProps, onChange } = useBaseTable<HostVO>({
    columns: columns.value,
    rows: []
  })
  const handleSearch = (selectedKeys: Key[], confirm: (param?: FilterConfirmProps) => void, dataIndex: string) => {
    confirm()
    Object.assign(state, {
      searchText: selectedKeys[0] as string,
      searchedColumn: dataIndex
    })
    getHostList()
  }

  const handleReset = (clearFilters: (param?: FilterResetProps) => void) => {
    clearFilters({ confirm: true })
    state.searchText = ''
    getHostList()
  }

  const getHostList = async () => {
    loading.value = true
    if (!paginationProps.value) {
      loading.value = false
      return
    }
    try {
      const res = await hostApi.getHosts({ ...filtersParams.value, clusterId: 0 })
      dataSource.value = res.content.map((v) => ({ ...v, name: v.id, displayName: v.hostname }))
      paginationProps.value.total = res.total
      loading.value = false
    } catch (error) {
      console.log('error :>> ', error)
    } finally {
      loading.value = false
    }
  }

  const onSelectChange: TableRowSelection['onChange'] = (selectedRowKeys) => {
    state.selectedRowKeys = selectedRowKeys
  }

  const onFilterDropdownOpenChange = (visible: boolean) => {
    if (visible) {
      setTimeout(() => {
        searchInputRef.value.focus()
      }, 100)
    }
  }

  const addHost = () => {
    hostCreateRef.value?.handleOpen('ADD')
  }

  const editHost = (row: HostVO) => {
    const cluster = clusterStore.clusters.find((v) => v.name === row.clusterName)
    const formatHost = { ...row, diplayName: row.clusterDisplayName, clusterId: cluster?.id }
    hostCreateRef.value?.handleOpen('EDIT', formatHost)
  }

  const deleteHost = (row?: HostVO) => {
    console.log('row :>> ', row)
    Modal.confirm({
      title: t('common.delete_msg'),
      async onOk() {
        message.success(t('common.delete_success'))
      }
    })
  }

  const updateHost = (item: HostReq) => {
    console.log('item :>> ', item)
  }

  const afterSetupHostConfig = async (type: 'ADD' | 'EDIT', item: HostReq) => {
    type === 'ADD' ? installRef.value?.handleOpen(item) : updateHost(item)
  }

  onMounted(() => {
    getHostList()
  })
</script>

<template>
  <div class="host-list">
    <div class="title">{{ $t('host.host_list') }}</div>
    <a-space :size="16">
      <a-button type="primary" @click="addHost">{{ $t('cluster.add_host') }}</a-button>
      <a-button type="primary" danger @click="deleteHost">{{ $t('common.bulk_remove') }}</a-button>
    </a-space>
    <a-table
      :loading="loading"
      row-key="id"
      :data-source="dataSource"
      :columns="columns"
      :pagination="paginationProps"
      :scroll="{ x: 1300, y: 1000 }"
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
        <svg-icon :name="filtered ? 'search_activated' : 'search'" />
      </template>
      <template #bodyCell="{ record, column }">
        <template v-if="column.key === 'hostname'">
          <a-typography-link underline>
            <span :title="record.hostname">{{ record.hostname }}</span>
          </a-typography-link>
        </template>
        <template v-if="column.key === 'componentNum'">
          <span :title="record.componentNum">
            {{ `${record.componentNum} ${$t('host.component_count_quantifier')}` }}
          </span>
        </template>
        <template v-if="column.key === 'status'">
          <svg-icon :name="record.status.toLowerCase()" />
          <span :title="`${record.message ? record.message : ''}`">
            {{ `${$t(`common.${record.status.toLowerCase()}`)}` }}
            {{ record.message && record.status.toLowerCase() === 'failed' ? `:  ${record.message}` : '' }}
          </span>
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
    <host-create ref="hostCreateRef" :is-public="true" @on-ok="afterSetupHostConfig" />
    <install-dependencies ref="installRef" />
  </div>
</template>

<style scoped lang="scss">
  .host-list {
    width: 100%;
    height: 100vh;
    overflow: auto;
    background-color: #fff;
    padding: 16px;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    gap: 16px;

    .title {
      font-size: 16px;
      font-weight: 500;
      line-height: 24px;
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
