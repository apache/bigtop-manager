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
  import { message, Modal, TableColumnType, TableProps } from 'ant-design-vue'
  import { getHosts } from '@/api/hosts'
  import * as hostApi from '@/api/hosts'

  import HostCreate from '@/features/create-host/index.vue'
  import InstallDependencies from '@/features/create-host/install-dependencies.vue'
  import SvgIcon from '@/components/base/svg-icon/index.vue'

  import type { FilterConfirmProps, FilterResetProps } from 'ant-design-vue/es/table/interface'
  import type { GroupItem } from '@/components/common/button-group/types'
  import type { HostVO } from '@/api/hosts/types'
  import type { ClusterVO } from '@/api/cluster/types'
  import type { HostReq } from '@/api/command/types'

  type Key = string | number

  interface TableState {
    selectedRowKeys: Key[]
    searchText: string
    searchedColumn: keyof HostVO
  }

  const { t } = useI18n()
  const router = useRouter()
  const attrs = useAttrs() as ClusterVO

  const searchInputRef = ref()
  const hostCreateRef = ref<InstanceType<typeof HostCreate> | null>(null)
  const installRef = ref<InstanceType<typeof InstallDependencies> | null>(null)
  const hostStatus = ref(['INSTALLING', 'SUCCESS', 'FAILED', 'UNKNOWN'])

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
        { text: t('common.success'), value: 1 },
        { text: t('common.failed'), value: 2 },
        { text: t('common.unknown'), value: 3 }
      ]
    },
    {
      title: t('common.operation'),
      key: 'operation',
      width: '160px',
      fixed: 'right'
    }
  ])

  const { loading, dataSource, filtersParams, paginationProps, onChange } = useBaseTable<HostVO>({
    columns: columns.value,
    rows: []
  })

  const operations = computed((): GroupItem[] => [
    { text: 'edit', clickEvent: (_item, args) => handleEdit(args) },
    { text: 'remove', danger: true, clickEvent: (_item, args) => deleteHost([args.id]) }
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

  const handleEdit = (row: HostVO) => {
    const formatHost = { ...row, displayName: row.clusterDisplayName, clusterId: attrs?.id }
    hostCreateRef.value?.handleOpen('EDIT', formatHost)
  }

  const bulkRemove = () => {
    if (state.selectedRowKeys.length === 0) {
      message.error(t('common.delete_empty'))
      return
    }
    deleteHost(state.selectedRowKeys as number[])
  }

  const deleteHost = (ids: number[]) => {
    Modal.confirm({
      title: () =>
        h('div', { style: { display: 'flex' } }, [
          h(SvgIcon, { name: 'unknown', style: { width: '24px', height: '24px' } }),
          h('p', ids.length > 1 ? t('common.delete_msgs') : t('common.delete_msg'))
        ]),
      style: { top: '30vh' },
      icon: null,
      async onOk() {
        try {
          const data = await hostApi.removeHost({ ids })
          if (data) {
            message.success(t('common.delete_success'))
            await getHostList(true)
          }
        } catch (error) {
          console.log('error :>> ', error)
        }
      }
    })
  }

  const viewHostDetail = (row: HostVO) => {
    const { id: hostId } = row
    router.push({ name: 'HostDetail', query: { hostId, clusterId: attrs.id } })
  }

  const addHost = () => {
    hostCreateRef.value?.handleOpen('ADD', { clusterId: attrs.id })
  }

  const afterSetupHostConfig = async (type: 'ADD' | 'EDIT', item: HostReq) => {
    if (type === 'ADD') {
      installRef.value?.handleOpen(item)
    } else {
      await getHostList(true)
    }
  }

  const getHostList = async (isReset = false) => {
    loading.value = true
    if (attrs.id == undefined || !paginationProps.value) {
      loading.value = false
      return
    }
    if (isReset) {
      paginationProps.value.current = 1
    }
    try {
      const res = await getHosts({ ...filtersParams.value, clusterId: attrs.id })
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
    getHostList()
  }

  onActivated(() => {
    getHostList()
  })
</script>

<template>
  <div class="host">
    <header>
      <div class="header-title">{{ t('host.host_list') }}</div>
      <a-space :size="16">
        <a-button type="primary" danger @click="bulkRemove">{{ t('common.bulk_remove') }}</a-button>
        <a-button type="primary" @click="addHost">{{ t('cluster.add_host') }}</a-button>
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
      <template #customFilterIcon="{ filtered, column }">
        <svg-icon v-if="column.key != 'status'" name="search" :highlight="filtered" />
        <svg-icon v-else name="filter" :highlight="filtered" />
      </template>
      <template #bodyCell="{ record, column }">
        <template v-if="column.key === 'hostname'">
          <a-typography-link underline @click="viewHostDetail(record)"> {{ record.hostname }} </a-typography-link>
        </template>
        <template v-if="column.key === 'status'">
          <svg-icon style="margin-left: 0" :name="hostStatus[record.status].toLowerCase()" />
          <span>{{ t(`common.${hostStatus[record.status].toLowerCase()}`) }}</span>
        </template>
        <template v-if="column.key === 'operation'">
          <button-group
            i18n="common"
            :text-compact="true"
            :space="24"
            :groups="operations"
            :payload="record"
            group-shape="default"
            group-type="link"
          />
        </template>
      </template>
    </a-table>
    <host-create
      ref="hostCreateRef"
      :current-hosts="dataSource"
      :api-edit-caller="true"
      @on-ok="afterSetupHostConfig"
    />
    <install-dependencies ref="installRef" @on-install-success="getHostList(true)" />
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
