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
  import { TableColumnType, TableProps } from 'ant-design-vue'
  import LogsView, { type LogViewProps } from '@/features/log-view/index.vue'

  import type { JobVO, StageVO, StateType, TaskListParams, TaskVO } from '@/api/job/types'
  import type { CommandRes, JobStageProgressItem } from '@/store/job-progress'
  import type { Reactive } from 'vue'
  import type { ListParams } from '@/api/types'

  interface BreadcrumbItem {
    id: string
    name: string
    tableData: (JobVO | StageVO | TaskVO)[]
    pagination: ListParams
  }

  interface Props {
    execRes: CommandRes
    jobInfo: Reactive<Map<number, JobStageProgressItem>>
  }

  const props = defineProps<Props>()
  const { execRes, jobInfo } = toRefs(props)

  const { t } = useI18n()
  const breadcrumbs = ref<BreadcrumbItem[]>([])
  const status = shallowRef<Record<StateType, string>>({
    Pending: 'installing',
    Processing: 'processing',
    Failed: 'failed',
    Canceled: 'canceled',
    Successful: 'success'
  })
  const apiMap = shallowRef([{ key: 'jobId' }, { key: 'stageId' }])

  const logsViewState = reactive<LogViewProps>({ open: false })
  const breadcrumbLen = computed(() => breadcrumbs.value.length)
  const currBreadcrumb = computed(() => breadcrumbs.value.at(-1))

  const columns = computed((): TableColumnType[] => [
    { title: '#', width: '48px', key: 'index', customRender: ({ index }) => `${index + 1}` },
    { title: t('common.name'), key: 'name', dataIndex: 'name', ellipsis: true },
    { title: t('common.status'), key: 'state', dataIndex: 'state' },
    { title: t('common.create_time'), dataIndex: 'createTime', ellipsis: true },
    { title: t('common.update_time'), dataIndex: 'updateTime', ellipsis: true }
  ])

  const apiParams = computed(() =>
    breadcrumbs.value.reduce((pre, val, index) => {
      const key = apiMap.value[index].key
      return Object.assign(pre, {
        [`${key}`]: val.id.replace(`${key}-`, '')
      })
    }, {} as TaskListParams)
  )

  const { dataSource, loading, paginationProps, onChange } = useBaseTable<JobVO | StageVO | TaskVO>({
    columns: columns.value,
    rows: []
  })

  const currJobInfo = computed(() => jobInfo.value.get(execRes.value.jobId))

  watch(
    () => currJobInfo.value?.payLoad,
    (val) => {
      if (!val) return

      breadcrumbs.value[0] = {
        name: val.name!,
        id: `jobId-${val.id}`,
        tableData: val.stages || [],
        pagination: { pageNum: 1, pageSize: 10 }
      }

      if (breadcrumbLen.value === 1) {
        dataSource.value = val.stages || []
      }

      if (breadcrumbLen.value === 2) {
        const stageId = breadcrumbs.value[1].id.replace('stageId-', '')
        const stage = val.stages?.find((s) => String(s.id) === stageId)
        if (stage) {
          breadcrumbs.value[1].tableData = stage.tasks || []
          dataSource.value = stage.tasks || []
        }
      }
    },
    {
      immediate: true,
      deep: true
    }
  )

  const clickBreadcrumb = (breadcrumb: BreadcrumbItem) => {
    if (breadcrumb.id !== currBreadcrumb.value?.id) {
      const index = breadcrumbs.value.findIndex((v) => v.id === breadcrumb.id)
      breadcrumbs.value.splice(index + 1, breadcrumbLen.value - index - 1)
    }
    dataSource.value = breadcrumb.tableData
    updataPaginationsInBaseTable(currBreadcrumb.value?.pagination)
  }

  const updateBreadcrumbs = (data: JobVO | StageVO | TaskVO) => {
    if (breadcrumbLen.value + 1 > apiMap.value.length) {
      // task log open
      logsViewState.open = true
      logsViewState.subTitle = data.name as string
      logsViewState.payLoad = {
        ...apiParams.value,
        taskId: Number(data.id),
        clusterId: currJobInfo.value?.clusterId ?? 0
      }
      return
    }
    const breadcrumbKey = apiMap.value[breadcrumbLen.value].key
    const currId = `${breadcrumbKey}-${data.id}`
    const index = breadcrumbs.value.findIndex((v) => v.id === currId)

    const newBreadcrumbItem: BreadcrumbItem = {
      id: currId,
      name: data.name!,
      tableData: [],
      pagination: { pageNum: 1, pageSize: 10 }
    }

    if (index === -1) {
      if (breadcrumbKey === 'jobId') {
        newBreadcrumbItem.tableData = data.stages
        breadcrumbs.value.push(newBreadcrumbItem)
        dataSource.value = data.stages
      } else if (breadcrumbKey === 'stageId') {
        newBreadcrumbItem.tableData = data.tasks
        breadcrumbs.value.push(newBreadcrumbItem)
        dataSource.value = data.tasks
      }
      updataPaginationsInBaseTable()
    } else {
      dataSource.value = breadcrumbs.value[index].tableData
    }
  }

  const updataPaginationsInBaseTable = (pagination: ListParams = { pageNum: 1, pageSize: 10 }) => {
    const { pageNum: current, pageSize } = pagination
    paginationProps.value = { ...paginationProps.value, current, pageSize }
  }

  const updataPaginationsInBreadcrumbs = (pagination: ListParams) => {
    const { pageNum, pageSize } = pagination
    const index = breadcrumbs.value.findIndex((v) => v.id === currBreadcrumb.value?.id)
    const targetPagination = breadcrumbs.value[index].pagination

    if (index != -1) {
      breadcrumbs.value[index].pagination = { ...targetPagination, pageNum, pageSize }
    }
  }

  const tableChange: TableProps['onChange'] = (...args) => {
    const { current: pageNum, pageSize } = args[0]
    updataPaginationsInBreadcrumbs({ pageNum, pageSize })
    onChange(...args)
  }
</script>

<template>
  <div class="job">
    <header>
      <a-breadcrumb>
        <a-breadcrumb-item
          v-for="breadcrumb in breadcrumbs"
          :key="breadcrumb.id"
          class="header-title"
          style="font-size: 16px"
          @click="clickBreadcrumb(breadcrumb)"
        >
          <a v-if="currBreadcrumb?.id != breadcrumb.id">{{ breadcrumb.name }}</a>
          <span v-else>{{ breadcrumb.name }}</span>
        </a-breadcrumb-item>
      </a-breadcrumb>
    </header>
    <a-table
      :loading="loading"
      :data-source="dataSource"
      :columns="columns"
      :pagination="paginationProps"
      @change="tableChange"
    >
      <template #bodyCell="{ record, text, column }">
        <template v-if="column.key === 'name'">
          <a-typography-link underline @click="updateBreadcrumbs(record)">
            <span :title="record.name">{{ record.name }}</span>
          </a-typography-link>
        </template>
        <template v-if="column.key === 'state'">
          <job-progress v-if="breadcrumbLen === 1" :key="record.id" :state="text" :progress-data="record.progress" />
          <svg-icon v-else :name="status[record.state as StateType]"></svg-icon>
        </template>
      </template>
    </a-table>
    <logs-view
      v-model:open="logsViewState.open"
      width="46%"
      :pay-load="logsViewState.payLoad"
      :sub-title="logsViewState.subTitle"
    />
  </div>
</template>

<style lang="scss" scoped></style>
