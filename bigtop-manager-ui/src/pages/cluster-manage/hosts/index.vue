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
  import { message, TableColumnType, TableProps } from 'ant-design-vue'

  import { useClusterStore } from '@/store/cluster'
  import * as hostApi from '@/api/host'

  import useBaseTable from '@/composables/use-base-table'
  import HostCreate from '@/features/create-host/index.vue'
  import InstallDependencies from '@/features/create-host/install-dependencies.vue'

  import type { FilterConfirmProps, FilterResetProps, TableRowSelection } from 'ant-design-vue/es/table/interface'
  import type { HostReq } from '@/api/command/types'
  import type { GroupItem } from '@/components/common/button-group/types'
  import type { HostVO } from '@/api/host/types'

  const POLLING_INTERVAL = 30000
  type Key = string | number

  interface TableState {
    selectedRowKeys: Key[]
    searchText: string
    searchedColumn: string
  }

  const { t } = useI18n()
  const { confirmModal } = useModal()

  const router = useRouter()
  const clusterStore = useClusterStore()
  const searchInputRef = ref()
  const pollingIntervalId = ref<any>(null)
  const hostCreateRef = ref<InstanceType<typeof HostCreate> | null>(null)
  const installRef = ref<InstanceType<typeof InstallDependencies> | null>(null)
  const hostStatus = shallowRef(['INSTALLING', 'SUCCESS', 'FAILED', 'UNKNOWN'])
  const state = reactive<TableState>({
    searchText: '',
    searchedColumn: '',
    selectedRowKeys: []
  })
  const filtersOfClusterDisplayName = computed(() =>
    Object.values(clusterStore.clusterMap).map((v) => ({
      text: v.displayName || v.name,
      value: v.id!
    }))
  )
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
      key: 'clusterDisplayName',
      dataIndex: 'clusterDisplayName',
      ellipsis: true,
      filterMultiple: false,
      filters: filtersOfClusterDisplayName.value
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
      key: 'status',
      width: '260px',
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
      title: t('common.operation'),
      key: 'operation',
      width: '160px',
      fixed: 'right'
    }
  ])

  const operations = computed((): GroupItem[] => [
    { text: 'edit', clickEvent: (_item, args) => editHost(args) },
    { text: 'remove', danger: true, clickEvent: (_item, args) => deleteHost([args.id]) }
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
    getHostList(true)
  }

  const handleReset = (clearFilters: (param?: FilterResetProps) => void) => {
    clearFilters({ confirm: true })
    state.searchText = ''
    getHostList(true)
  }

  const getHostList = async (isFirstCall = false) => {
    if (!paginationProps.value) {
      loading.value = false
      return
    }
    try {
      if (isFirstCall) {
        loading.value = true
        paginationProps.value.current = 1
      }
      const res = await hostApi.getHosts({ ...filtersParams.value, clusterId: 0 })
      dataSource.value = res.content
      paginationProps.value.total = res.total
      loading.value = false
    } catch (error) {
      console.log('error :>> ', error)
      stopPolling()
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

  const bulkRemove = () => {
    if (state.selectedRowKeys.length === 0) {
      message.error(t('common.delete_empty'))
      return
    }
    deleteHost(state.selectedRowKeys as number[])
  }

  const editHost = (row: HostVO) => {
    const cluster = Object.values(clusterStore.clusterMap).find((v) => v.name === row.clusterName)
    hostCreateRef.value?.handleOpen('EDIT', {
      ...row,
      displayName: row.clusterDisplayName,
      clusterId: cluster?.id
    })
  }

  const deleteHost = (ids: number[]) => {
    confirmModal({
      tipText: ids.length > 1 ? t('common.delete_msgs') : t('common.delete_msg'),
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

  const afterSetupHostConfig = async (type: 'ADD' | 'EDIT', item: HostReq) => {
    if (type === 'ADD') {
      installRef.value?.handleOpen(item)
    } else {
      await getHostList(true)
    }
  }

  onMounted(() => {
    startPolling()
  })

  const startPolling = () => {
    getHostList(true)
    pollingIntervalId.value = setInterval(() => {
      getHostList()
    }, POLLING_INTERVAL)
  }

  const stopPolling = () => {
    if (pollingIntervalId.value) {
      clearInterval(pollingIntervalId.value)
      pollingIntervalId.value = null
    }
  }

  const tableChange: TableProps['onChange'] = (pagination, filters, ...args) => {
    onChange(pagination, filters, ...args)
    getHostList(true)
  }

  const viewHostDetail = (row: HostVO) => {
    const { clusterDisplayName, id: hostId } = row
    const index = filtersOfClusterDisplayName.value.findIndex((v) => v.text === clusterDisplayName)
    if (index != -1) {
      const clusterId = filtersOfClusterDisplayName.value[index].value
      router.push({ name: 'HostDetail', query: { hostId, clusterId } })
    }
  }

  onUnmounted(() => {
    stopPolling()
  })
</script>

<template>
  <div class="host-list">
    <div class="title">{{ t('host.host_list') }}</div>
    <a-space :size="16">
      <a-button type="primary" @click="addHost">{{ t('cluster.add_host') }}</a-button>
      <a-button type="primary" danger @click="bulkRemove">{{ t('common.bulk_remove') }}</a-button>
    </a-space>
    <a-table
      :loading="loading"
      row-key="id"
      :data-source="dataSource"
      :columns="columns"
      :pagination="paginationProps"
      :scroll="{ x: 1300, y: 1000 }"
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
        <svg-icon v-if="!['status', 'clusterDisplayName'].includes(column.key)" name="search" :highlight="filtered" />
        <svg-icon v-else name="filter" :highlight="filtered" />
      </template>
      <template #bodyCell="{ record, column }">
        <template v-if="column.key === 'hostname'">
          <a-typography-link underlin>
            <span :title="record.hostname" @click="viewHostDetail(record)">{{ record.hostname }}</span>
          </a-typography-link>
        </template>
        <template v-if="column.key === 'componentNum'">
          <span :title="record.componentNum">
            {{ `${record.componentNum} ${t('host.component_count_quantifier')}` }}
          </span>
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
          ></button-group>
        </template>
      </template>
    </a-table>
    <host-create
      ref="hostCreateRef"
      :current-hosts="dataSource"
      :api-edit-caller="true"
      :is-public="true"
      @on-ok="afterSetupHostConfig"
    />
    <install-dependencies ref="installRef" @on-install-success="getHostList(true)" />
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

  .highlight {
    background-color: rgb(255, 192, 105);
    padding: 0px;
  }
</style>
