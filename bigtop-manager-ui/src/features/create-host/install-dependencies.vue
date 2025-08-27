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
  import { message, TableColumnType } from 'ant-design-vue'
  import { getInstalledStatus, installDependencies } from '@/api/hosts'
  import { useClusterStore } from '@/store/cluster'
  import { generateRandomId } from '@/utils/tools'
  import { execCommand } from '@/api/command'

  import HostCreate from './index.vue'

  import { type InstalledStatusVO, Status } from '@/api/hosts/types'
  import type { FilterConfirmProps, FilterResetProps } from 'ant-design-vue/es/table/interface'
  import type { GroupItem } from '@/components/common/button-group/types'
  import type { CommandRequest, HostReq } from '@/api/command/types'

  type Key = string | number

  interface TableState {
    selectedRowKeys: Key[]
    searchText: string
    searchedColumn: string
  }

  interface Emits {
    (event: 'onInstallSuccess'): void
  }

  const { t } = useI18n()
  const emits = defineEmits<Emits>()
  const clusterStore = useClusterStore()
  const { clusterMap } = storeToRefs(clusterStore)

  const installing = ref(false)
  const open = ref(false)
  const searchInputRef = ref()
  const hostCreateRef = ref<InstanceType<typeof HostCreate> | null>(null)

  const installStatus = shallowRef<InstalledStatusVO[]>([])
  const state = reactive<TableState>({
    searchText: '',
    searchedColumn: '',
    selectedRowKeys: []
  })
  const allInstallSuccess = computed(() => dataSource.value.every((v) => v.status === Status.Success))
  const columns = computed((): TableColumnType<HostReq & { status: string }>[] => [
    {
      title: t('host.hostname'),
      dataIndex: 'hostname',
      key: 'hostname',
      ellipsis: true,
      customFilterDropdown: true,
      onFilter: (value, record) => isContain(record.hostname, value as string)
    },
    {
      title: t('common.cluster'),
      key: 'clusterId',
      dataIndex: 'clusterId',
      ellipsis: true
    },
    {
      title: t('common.status'),
      dataIndex: 'status',
      key: 'status',
      ellipsis: true,
      filterMultiple: false,
      onFilter: (value, record: HostReq & { status: string }) => record.status.includes(value as string),
      filters: [
        {
          text: t('common.unknown'),
          value: Status.Unknown
        },
        {
          text: t('common.success'),
          value: Status.Success
        },
        {
          text: t('common.failed'),
          value: Status.Failed
        }
      ]
    },
    {
      title: t('common.operation'),
      width: '180px',
      key: 'operation',
      fixed: 'right'
    }
  ])

  const operations = computed((): GroupItem[] => [
    {
      text: 'edit',
      disabled: installing.value,
      clickEvent: (_item, args) => editPreAddedHost(args)
    },
    {
      text: 'remove',
      danger: true,
      disabled: installing.value,
      clickEvent: (_item, args) => deletePreAddedHost(args)
    }
  ])

  const { loading, dataSource, paginationProps, onChange, resetState } = useBaseTable<HostReq>({
    columns: columns.value,
    rows: []
  })

  const isContain = (source: string, target: string): boolean => {
    return source.toString().toLowerCase().includes(target.toLowerCase())
  }

  const splitHostFormToHostList = (hostForm: HostReq) => {
    dataSource.value = hostForm.hostnames?.map((v) => ({
      ...hostForm,
      hostname: v,
      hostnames: [v],
      status: 'UNKNOWN',
      key: generateRandomId()
    })) as HostReq[]
  }

  const setHostStatusToInstalling = () => {
    return dataSource.value.map((item: HostReq) => ({
      ...item,
      status: Status.Installing
    }))
  }

  const startInstallDependencies = async () => {
    if (dataSource.value.length == 0) {
      message.error(t('host.uninstallable'))
      return
    }
    try {
      installing.value = true
      const data = await installDependencies((dataSource.value = setHostStatusToInstalling()))
      if (data) {
        pollUntilInstalled()
      }
    } catch (error) {
      installing.value = false
      console.log('error :>> ', error)
    }
  }

  const pollUntilInstalled = (interval: number = 1000): void => {
    let isInitialized = false
    const intervalId = setInterval(async () => {
      if (!isInitialized) {
        dataSource.value = setHostStatusToInstalling()
        isInitialized = true
      }
      const result = await recordInstalledStatus()
      if (result) {
        installing.value = false
        clearInterval(intervalId)
      }
    }, interval)
  }

  const recordInstalledStatus = async () => {
    try {
      const data = await getInstalledStatus()
      installStatus.value = data
      dataSource.value = mergeByHostname(dataSource.value, data)
      return data.every((item) => item.status != Status.Installing)
    } catch (error) {
      console.log('error :>> ', error)
    }
  }

  const mergeByHostname = (arr1: any[], arr2: any[]): any[] => {
    const mergedMap = new Map<string, any>()
    for (const item of arr1) {
      mergedMap.set(item.hostname, { ...item })
    }
    for (const item of arr2) {
      if (mergedMap.has(item.hostname)) {
        mergedMap.set(item.hostname, { ...mergedMap.get(item.hostname), ...item })
      } else {
        mergedMap.set(item.hostname, { ...item })
      }
    }
    return Array.from(mergedMap.values())
  }

  const startAddHosts = async () => {
    const data = {
      command: 'Add',
      commandLevel: 'host',
      hostCommands: dataSource.value
    } as CommandRequest
    installing.value = true
    try {
      await execCommand(data)
      emits('onInstallSuccess')
      handleCancel()
    } catch (error) {
      console.log('error :>> ', error)
    } finally {
      installing.value = false
    }
  }

  const editPreAddedHost = (row: HostReq) => {
    hostCreateRef.value?.handleOpen('EDIT', row)
  }

  const deletePreAddedHost = (row: HostReq) => {
    dataSource.value = dataSource.value?.filter((v) => row.key !== v.key)
  }

  const handleInstalled = () => {
    if (allInstallSuccess.value) {
      startAddHosts()
    } else {
      startInstallDependencies()
    }
  }

  const handleOpen = (payLoad: HostReq) => {
    open.value = true
    splitHostFormToHostList(payLoad)
  }

  const handleCancel = () => {
    open.value = false
    resetState()
  }

  const handleSearch = (selectedKeys: Key[], confirm: (param?: FilterConfirmProps) => void, dataIndex: string) => {
    confirm()
    Object.assign(state, {
      searchText: selectedKeys[0] as string,
      searchedColumn: dataIndex
    })
  }

  const handleReset = (clearFilters: (param?: FilterResetProps) => void) => {
    clearFilters({ confirm: true })
    state.searchText = ''
  }

  const handleOk = (_: string, item: HostReq) => {
    const index = dataSource.value.findIndex((data) => data.key === item.key)
    if (index !== -1) {
      dataSource.value[index] = item
    }
  }

  defineExpose({
    handleOpen
  })
</script>

<template>
  <div class="install-dependencies">
    <a-modal
      :open="open"
      width="60%"
      :title="t('cluster.install_dependencies')"
      :mask-closable="false"
      :centered="true"
      :confirm-loading="installing"
      :ok-text="allInstallSuccess ? t('common.confirm') : installing ? t('common.installing') : t('common.install')"
      :destroy-on-close="true"
      @ok="handleInstalled"
      @cancel="handleCancel"
    >
      <a-table
        :loading="loading"
        row-key="id"
        :data-source="dataSource"
        :columns="columns"
        :pagination="paginationProps"
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
            <a-space :size="16">
              <a-button type="primary" size="small" @click="handleSearch(selectedKeys, confirm, column.dataIndex)">
                {{ t('common.search') }}
              </a-button>
              <a-button size="small" @click="handleReset(clearFilters)"> {{ t('common.reset') }}</a-button>
            </a-space>
          </div>
        </template>
        <template #customFilterIcon="{ filtered, column }">
          <svg-icon v-if="column.key === 'hostname'" name="search" :highlight="filtered" />
          <svg-icon v-else name="filter" :highlight="filtered" />
        </template>
        <template #bodyCell="{ record, column }">
          <template v-if="column.key === 'clusterId'">
            <span :title="`${clusterMap[record.clusterId]?.displayName}`">
              {{ clusterMap[record.clusterId]?.displayName }}
            </span>
          </template>
          <template v-if="column.key === 'status'">
            <svg-icon :name="record.status.toLowerCase()" />
            <span :title="`${record.message ? record.message : ''}`">
              {{ `${t(`common.${record.status.toLowerCase()}`)}` }}
              {{ record.message && record.status.toLowerCase() === 'failed' ? `:  ${record.message}` : '' }}
            </span>
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
    </a-modal>
    <host-create ref="hostCreateRef" :current-hosts="dataSource" :is-public="true" @on-ok="handleOk" />
  </div>
</template>

<style lang="scss" scoped>
  .search {
    display: grid;
    gap: $space-sm;
    padding: $space-sm;
  }

  .highlight {
    background-color: rgb(255, 192, 105);
    padding: 0;
  }
</style>
