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
  import { computed, ComputedRef, onActivated, onDeactivated, reactive, ref, shallowRef, useAttrs } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { TableColumnType, TableProps } from 'ant-design-vue'
  import { getJobList, getStageList, getTaskList } from '@/api/job'
  import useBaseTable from '@/composables/use-base-table'
  import LogsView, { type LogViewProps } from '@/components/log-view/index.vue'
  import CustomProgress from './custom-progress.vue'
  import type { JobVO, StageVO, StateType, TaskListParams, TaskVO } from '@/api/job/types'
  import type { ClusterVO } from '@/api/cluster/types'

  interface BreadcrumbItem {
    id: string
    name: ComputedRef<string> | string
  }
  const POLLING_INTERVAL = 3000
  const { t } = useI18n()
  const clusterInfo = useAttrs() as ClusterVO
  const pollingIntervalId = ref<any>(null)
  const breadcrumbs = ref<BreadcrumbItem[]>([
    {
      name: computed(() => t('job.job_list')),
      id: `clusterId-${clusterInfo.id}`
    }
  ])
  const status = shallowRef<Record<StateType, string>>({
    Pending: 'installing',
    Processing: 'processing',
    Failed: 'failed',
    Canceled: 'canceled',
    Successful: 'success'
  })
  const apiMap = shallowRef([
    {
      key: 'clusterId',
      api: getJobList
    },
    {
      key: 'jobId',
      api: getStageList
    },
    {
      key: 'stageId',
      api: getTaskList
    }
  ])
  const logsViewState = reactive<LogViewProps>({
    open: false
  })
  const breadcrumbLen = computed(() => breadcrumbs.value.length)
  const currBreadcrumb = computed(() => breadcrumbs.value.at(-1))
  const columns = computed((): TableColumnType[] => [
    {
      title: '#',
      width: '48px',
      key: 'index',
      customRender: ({ index }) => `${index + 1}`
    },
    {
      title: t('common.name'),
      key: 'name',
      dataIndex: 'name',
      ellipsis: true
    },
    {
      title: t('common.status'),
      key: 'state',
      dataIndex: 'state'
    },
    {
      title: t('common.create_time'),
      dataIndex: 'createTime',
      ellipsis: true
    },
    {
      title: t('common.update_time'),
      dataIndex: 'updateTime',
      ellipsis: true
    }
  ])
  const apiParams = computed(
    (): TaskListParams =>
      breadcrumbs.value.reduce((pre, val, index) => {
        return Object.assign(pre, {
          [`${apiMap.value[index].key}`]: val.id.replace(`${apiMap.value[index].key}-`, '')
        })
      }, {} as TaskListParams)
  )

  const { dataSource, loading, filtersParams, paginationProps, onChange } = useBaseTable<JobVO | StageVO | TaskVO>({
    columns: columns.value
  })

  const clickBreadcrumb = (breadcrumb: BreadcrumbItem) => {
    if (breadcrumb.id !== currBreadcrumb.value?.id) {
      const index = breadcrumbs.value.findIndex((v) => v.id === breadcrumb.id)
      breadcrumbs.value.splice(index + 1, breadcrumbLen.value - index - 1)
      stopPolling()
      startPolling()
    }
  }

  const updateBreadcrumbs = (data: BreadcrumbItem) => {
    if (breadcrumbLen.value + 1 > apiMap.value.length) {
      // task log open
      logsViewState.open = true
      logsViewState.subTitle = data.name as string
      logsViewState.payLoad = {
        ...apiParams.value,
        taskId: parseInt(data.id)
      }
      return
    }
    const currId = `${apiMap.value[breadcrumbLen.value].key}-${data.id}`
    const index = breadcrumbs.value.findIndex((v) => v.id === currId)
    if (index === -1) {
      breadcrumbs.value.push({
        id: currId,
        name: data.name as string
      })
      stopPolling()
      startPolling()
    }
  }

  const getListData = async (isFirstCall = false) => {
    if (!paginationProps.value) {
      loading.value = false
      return
    }
    try {
      if (isFirstCall) {
        loading.value = true
      }
      const { api } = apiMap.value[breadcrumbs.value.length - 1]
      const res = await api(apiParams.value, { ...filtersParams.value, sort: 'desc' })
      dataSource.value = res.content
      paginationProps.value.total = res.total
    } catch (error) {
      console.log('error :>> ', error)
      stopPolling()
    } finally {
      loading.value = false
    }
  }

  const tableChange: TableProps['onChange'] = (...args) => {
    onChange(...args)
    stopPolling()
    startPolling()
  }

  const startPolling = () => {
    getListData(true)
    pollingIntervalId.value = setInterval(() => {
      getListData()
    }, POLLING_INTERVAL)
  }

  const stopPolling = () => {
    if (pollingIntervalId.value) {
      clearInterval(pollingIntervalId.value)
      pollingIntervalId.value = null
    }
  }

  onActivated(() => {
    startPolling()
  })

  onDeactivated(() => {
    stopPolling()
  })
</script>

<template>
  <div class="job">
    <header>
      <a-breadcrumb>
        <a-breadcrumb-item
          v-for="breadcrumb in breadcrumbs"
          :key="breadcrumb.id"
          class="header-title"
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
          <custom-progress v-if="breadcrumbLen === 1" :key="record.id" :state="text" :progress-data="record.progress" />
          <svg-icon v-else :name="status[record.state as StateType]"></svg-icon>
        </template>
      </template>
    </a-table>
    <logs-view
      v-model:open="logsViewState.open"
      :pay-load="logsViewState.payLoad"
      :sub-title="logsViewState.subTitle"
    />
  </div>
</template>

<style lang="scss" scoped></style>
