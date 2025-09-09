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
  import { message, type TableColumnType, type TableProps } from 'ant-design-vue'
  import { deleteComponent, getComponents } from '@/api/component'
  import { useStackStore } from '@/store/stack'
  import { useJobProgress } from '@/store/job-progress'

  import type { GroupItem } from '@/components/common/button-group/types'
  import type { ComponentVO } from '@/api/component/types'
  import type { FilterConfirmProps, FilterResetProps } from 'ant-design-vue/es/table/interface'
  import type { Command, CommandRequest } from '@/api/command/types'
  import type { ServiceVO } from '@/api/service/types'

  type Key = string | number
  interface TableState {
    selectedRowKeys: Key[]
    searchText: string
    searchedColumn: keyof ComponentVO
    selectedRows: ComponentVO[]
  }

  const POLLING_INTERVAL = 3000

  const { t } = useI18n()
  const { confirmModal } = useModal()

  const jobProgressStore = useJobProgress()
  const stackStore = useStackStore()
  const route = useRoute()
  const router = useRouter()
  const attrs = useAttrs() as unknown as Required<ServiceVO> & { clusterId: number }

  const { stacks, stackRelationMap } = storeToRefs(stackStore)
  const searchInputRef = ref()
  const pollingIntervalId = ref<any>(null)
  const componentStatus = ref(['INSTALLING', 'SUCCESS', 'FAILED', 'UNKNOWN'])

  const commandRequest = shallowRef<CommandRequest>({
    command: 'Add',
    commandLevel: 'component',
    componentCommands: []
  })

  const state = reactive<TableState>({
    searchText: '',
    searchedColumn: '',
    selectedRowKeys: [],
    selectedRows: []
  })

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
      filters: [...(componentsFromStack.value.get(attrs.name)?.values() || [])]?.map((v) => ({
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
      width: 160,
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
      title: t('common.operation'),
      width: 280,
      key: 'operation',
      fixed: 'right'
    }
  ])

  const { loading, dataSource, paginationProps, filtersParams, onChange } = useBaseTable<ComponentVO>({
    columns: columns.value,
    rows: []
  })

  const operations = shallowRef<GroupItem<keyof typeof Command>[]>([
    {
      text: 'start',
      action: 'Start',
      hidden: (_, args) => stackRelationMap.value?.components[`${args.name}`].category === 'client',
      clickEvent: (item, args) => handleTableOperation(item!, args)
    },
    {
      text: 'stop',
      action: 'Stop',
      hidden: (_, args) => stackRelationMap.value?.components[`${args.name}`].category === 'client',
      clickEvent: (item, args) => handleTableOperation(item!, args)
    },
    {
      text: 'restart',
      action: 'Restart',
      hidden: (_, args) => stackRelationMap.value?.components[`${args.name}`].category === 'client',
      clickEvent: (item, args) => handleTableOperation(item!, args)
    },
    {
      text: 'remove',
      danger: true,
      clickEvent: (_, args) => handleDelete(args)
    }
  ])

  const batchOperations = computed((): GroupItem[] => [
    {
      text: t('common.batch_operation'),
      type: 'primary',
      dropdownMenu: [
        {
          action: 'Start',
          text: t('common.start', [t('common.selected')])
        },
        {
          action: 'Restart',
          text: t('common.restart', [t('common.selected')])
        },
        {
          action: 'Stop',
          text: t('common.stop', [t('common.selected')])
        }
      ],
      dropdownMenuClickEvent: (info) => dropdownMenuClick && dropdownMenuClick(info)
    }
  ])

  const onSelectChange = (selectedRowKeys: Key[], selectedRows: ComponentVO[]) => {
    state.selectedRowKeys = selectedRowKeys
    state.selectedRows = selectedRows
  }

  const handleSearch = (selectedKeys: Key[], confirm: (param?: FilterConfirmProps) => void, dataIndex: string) => {
    confirm()
    state.searchText = selectedKeys[0] as string
    state.searchedColumn = dataIndex
    stopPolling()
    startPolling(true, true)
  }

  const handleReset = (clearFilters: (param?: FilterResetProps) => void) => {
    clearFilters({ confirm: true })
    state.searchText = ''
    stopPolling()
    startPolling(true, true)
  }

  const dropdownMenuClick: GroupItem['dropdownMenuClickEvent'] = async ({ key }) => {
    if (state.selectedRows.length === 0) {
      message.error(t('common.select_error', [`${t('common.component')}`.toLowerCase()]))
      return
    }
    commandRequest.value.command = key as keyof typeof Command
    const map = state.selectedRows.reduce((map, v) => {
      if (!map.has(v.name)) {
        map.set(v.name, { componentName: v.name, hostnames: [v.hostname] })
      } else {
        map.get(v.name).hostnames.push(v.hostname)
      }
      return map
    }, new Map())
    commandRequest.value.componentCommands = [...map.values()]
    execOperation(state.selectedRows)
  }

  const handleTableOperation = async (item: GroupItem<keyof typeof Command>, row: ComponentVO) => {
    commandRequest.value.command = item.action!
    commandRequest.value.componentCommands?.push({
      componentName: row.name!,
      hostnames: [row.hostname!]
    })
    execOperation([row])
  }

  const execOperation = (rows?: ComponentVO[]) => {
    const displayNameOfRows: string[] = rows ? rows.map((v) => v.displayName ?? '').filter((v) => v) : []
    jobProgressStore.processCommand(
      { ...commandRequest.value, clusterId: attrs.clusterId },
      async () => {
        getComponentList(true, true)
        state.selectedRowKeys = []
        state.selectedRows = []
      },
      { displayName: displayNameOfRows }
    )
  }

  const handleDelete = async (row: ComponentVO) => {
    confirmModal({
      tipText: t('common.delete_msg'),
      async onOk() {
        try {
          const data = await deleteComponent({ clusterId: attrs.clusterId, id: row.id! })
          if (data) {
            message.success(t('common.delete_success'))
            getComponentList(true, true)
          }
        } catch (error) {
          console.log('error :>> ', error)
        }
      }
    })
  }

  const getComponentList = async (isReset = false, isFirstCall = false) => {
    const { clusterId, id: serviceId } = attrs
    if (!paginationProps.value) {
      loading.value = false
      return
    }
    if (isReset) {
      paginationProps.value.current = 1
    }
    try {
      if (isFirstCall) {
        loading.value = true
      }
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

  const tableChange: TableProps['onChange'] = (...args) => {
    onChange(...args)
    stopPolling()
    startPolling()
  }

  const startPolling = (isReset = false, isFirstCall = false) => {
    getComponentList(isReset, isFirstCall)
    pollingIntervalId.value = setInterval(() => {
      getComponentList()
    }, POLLING_INTERVAL)
  }

  const stopPolling = () => {
    if (pollingIntervalId.value) {
      clearInterval(pollingIntervalId.value)
      pollingIntervalId.value = null
    }
  }

  const addComponent = () => {
    const creationMode = Number(attrs.clusterId) === 0 ? 'public' : 'internal'
    const routerName = Number(attrs.clusterId) === 0 ? 'CreateInfraComponent' : 'CreateComponent'
    router.push({
      name: routerName,
      params: { ...route.params, creationMode, type: 'component' }
    })
  }

  onActivated(() => {
    startPolling()
  })

  onDeactivated(() => {
    stopPolling()
  })
</script>

<template>
  <div class="component">
    <header>
      <div class="header-title">{{ t('common.component') }}</div>
      <div class="list-operation">
        <a-button type="primary" @click="addComponent">{{ t('common.add', [`${t('common.component')}`]) }}</a-button>
        <button-group :groups="batchOperations" group-shape="default" />
      </div>
    </header>
    <a-table
      row-key="id"
      :loading="loading"
      :data-source="dataSource"
      :columns="columns"
      :pagination="paginationProps"
      :scroll="{ x: 900, y: 1000 }"
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
        <svg-icon v-if="!['name', 'status'].includes(column.key)" name="search" :highlight="filtered" />
        <svg-icon v-else name="filter" :highlight="filtered" />
      </template>
      <template #bodyCell="{ record, column }">
        <template v-if="['status'].includes(column.key as string)">
          <svg-icon style="margin-left: 0" :name="componentStatus[record.status].toLowerCase()" />
          <span>{{ t(`common.${componentStatus[record.status].toLowerCase()}`) }}</span>
        </template>
        <template v-if="column.key === 'quickLink'">
          <span v-if="!record.quickLink">{{ t('common.no_link') }}</span>
          <a-typography-link v-else :href="record.quickLink.url" target="_blank">
            {{ record.quickLink.displayName }}
          </a-typography-link>
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
