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
  import { PaginationProps, TableColumnType } from 'ant-design-vue'
  import { generateRandomId } from '@/utils/tools'

  import HostCreate from '@/features/create-host/index.vue'

  import type { FilterConfirmProps, FilterResetProps } from 'ant-design-vue/es/table/interface'
  import type { GroupItem } from '@/components/common/button-group/types'
  import type { HostReq } from '@/api/command/types'

  type Key = string | number
  type conflictItem = HostReq & { strategy: 'override' | 'keep' }

  interface TableState {
    selectedRowKeys: Key[]
    searchText: string
    searchedColumn: string
  }

  const { t } = useI18n()
  const props = defineProps<{ stepData: HostReq[] }>()
  const emits = defineEmits(['updateData'])

  const searchInputRef = ref()
  const hostCreateRef = ref<InstanceType<typeof HostCreate> | null>(null)

  const state = reactive<TableState>({
    searchText: '',
    searchedColumn: '',
    selectedRowKeys: []
  })

  const columns = computed((): TableColumnType[] => [
    {
      title: t('host.hostname'),
      dataIndex: 'hostname',
      key: 'hostname',
      ellipsis: true,
      customFilterDropdown: true,
      onFilter: (value, record) => isContain(record.hostname, value as string),
      onFilterDropdownOpenChange: (visible) => onFilterDropdownOpenChange(visible)
    },
    {
      title: t('host.ssh_user'),
      dataIndex: 'sshUser',
      width: '260px',
      key: 'sshUser',
      ellipsis: true,
      customFilterDropdown: true,
      onFilter: (value, record) => isContain(record.sshUser, value as string),
      onFilterDropdownOpenChange: (visible) => onFilterDropdownOpenChange(visible)
    },
    {
      title: t('common.desc'),
      dataIndex: 'desc',
      width: '20%',
      ellipsis: true
    },
    {
      key: 'status',
      title: t('common.status'),
      dataIndex: 'status',
      ellipsis: true
    },
    {
      title: t('common.operation'),
      key: 'operation',
      width: '160px',
      fixed: 'right'
    }
  ])

  const { loading, dataSource, paginationProps, onChange } = useBaseTable<HostReq>({ columns: columns.value, rows: [] })

  const operations = computed((): GroupItem[] => [
    { text: 'edit', clickEvent: (_item, args) => updateHost('EDIT', args) },
    { text: 'remove', danger: true, clickEvent: (_item, args) => deleteHost(args) }
  ])

  const isContain = (source: string, target: string): boolean => {
    return source.toString().toLowerCase().includes(target.toLowerCase())
  }

  const onFilterDropdownOpenChange = (visible: boolean) => {
    visible && setTimeout(searchInputRef.value.focus(), 100)
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

  const updateHost = (type: 'ADD' | 'EDIT', row?: HostReq) => {
    hostCreateRef.value?.handleOpen(type, row)
  }

  const deleteHost = (row?: HostReq) => {
    if (row?.hostnames) {
      dataSource.value = dataSource.value?.filter((v) => row!.key !== v.key)
    } else {
      dataSource.value = dataSource.value?.filter((v) => !state.selectedRowKeys.includes(v.key)) || []
    }
    updateStepData()
  }

  const updateStepData = () => {
    Object.assign(paginationProps.value as PaginationProps, { total: dataSource.value.length })
    const res = dataSource.value.map((v) => ({ ...v, hostnames: [v.hostname] }))
    emits('updateData', res)
  }

  /**
   * Merges duplicate hostnames based on strategy.
   * @param list
   * @param duplicateHosts
   * @param config host config
   */
  const mergeByStrategy = (list: HostReq[], duplicateHosts: conflictItem[], config: HostReq): HostReq[] => {
    const strategyMap = new Map<string, conflictItem>()
    duplicateHosts.forEach((s) => strategyMap.set(s.hostname, s))

    const existingHostnames = new Set<string>()
    const mainPart: HostReq[] = []
    const prependPart: HostReq[] = []

    for (const item of list) {
      existingHostnames.add(item.hostname)
      const strategy = strategyMap.get(item.hostname)

      if (!strategy) {
        mainPart.push(item)
      } else if (strategy.strategy === 'override') {
        mainPart.push(generateNewHostItem(item.hostname, config))
      } else {
        mainPart.push(item)
      }
    }

    for (const s of duplicateHosts) {
      if (!existingHostnames.has(s.hostname)) {
        prependPart.push(generateNewHostItem(s.hostname, config))
      }
    }

    return [...prependPart, ...mainPart]
  }

  const generateNewHostItem = (hostname: string, config: HostReq): HostReq => ({
    ...config,
    key: generateRandomId(),
    hostname: hostname,
    status: 'UNKNOWN'
  })

  /**
   * Handles successful addition or editing of hosts.
   */
  const addHostSuccess = (type: 'ADD' | 'EDIT', item: HostReq, duplicateHosts: any) => {
    if (type === 'ADD') {
      if (duplicateHosts.length > 0) {
        dataSource.value = mergeByStrategy(dataSource.value, duplicateHosts, item)
      } else {
        const items = item.hostnames?.map((v) => generateNewHostItem(v, item)) as HostReq[]
        dataSource.value?.unshift(...items)
      }
    }

    if (type === 'EDIT') {
      const index = dataSource.value.findIndex((data) => data.key === item.key)

      if (index !== -1) {
        dataSource.value[index] = item
      }
    }

    updateStepData()
  }

  watch(
    () => props.stepData,
    (val) => {
      dataSource.value = JSON.parse(JSON.stringify(val))
    },
    {
      immediate: true
    }
  )
</script>

<template>
  <div class="host-config">
    <header>
      <a-space :size="16">
        <a-button type="primary" @click="updateHost('ADD')">{{ t('cluster.add_host') }}</a-button>
        <a-button type="primary" danger @click="deleteHost">{{ t('common.bulk_remove') }}</a-button>
      </a-space>
    </header>
    <a-table
      :loading="loading"
      :data-source="dataSource"
      :columns="columns"
      :pagination="paginationProps"
      :row-selection="{ selectedRowKeys: state.selectedRowKeys, onChange: onSelectChange }"
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
            <a-button size="small" @click="handleReset(clearFilters)"> {{ t('common.reset') }} </a-button>
          </a-space>
        </div>
      </template>
      <template #customFilterIcon="{ filtered }">
        <svg-icon name="search" :highlight="filtered" />
      </template>
      <template #bodyCell="{ record, column }">
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
    <host-create ref="hostCreateRef" :current-hosts="dataSource" @on-ok="addHostSuccess" />
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
