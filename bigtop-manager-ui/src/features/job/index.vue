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
  import { Modal, TableColumnType, TableProps } from 'ant-design-vue'
  import { getJobList, getStageList, getTaskList, retryJob } from '@/api/job'

  import SvgIcon from '@/components/base/svg-icon/index.vue'
  import LogsView, { type LogViewProps } from '@/features/log-view/index.vue'

  import type { JobVO, StageVO, StateType, TaskListParams, TaskVO } from '@/api/job/types'
  import type { ClusterVO } from '@/api/cluster/types'
  import type { ListParams } from '@/api/types'

  interface BreadcrumbItem {
    id: string
    name: ComputedRef<string> | string
    pagination: ListParams
  }

  const POLLING_INTERVAL = 3000
  const { t } = useI18n()
  const clusterInfo = useAttrs() as ClusterVO
  const pollingIntervalId = ref<any>(null)
  const breadcrumbs = ref<BreadcrumbItem[]>([
    {
      name: computed(() => t('job.job_list')),
      id: `clusterId-${clusterInfo.id}`,
      pagination: {
        pageNum: 1,
        pageSize: 10,
        sort: 'desc'
      }
    }
  ])
  const apiMap = ref([
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
  const status = shallowRef<Record<StateType, string>>({
    Pending: 'installing',
    Processing: 'processing',
    Failed: 'failed',
    Canceled: 'canceled',
    Successful: 'success'
  })
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
        const apiMapKey = apiMap.value[index].key
        return Object.assign(pre, {
          [`${apiMapKey}`]: val.id.replace(`${apiMapKey}-`, '')
        })
      }, {} as TaskListParams)
  )

  const { dataSource, loading, paginationProps, filtersParams, onChange } = useBaseTable<JobVO | StageVO | TaskVO>({
    columns: columns.value
  })

  const isFirstPageNum = computed(() => filtersParams.value.pageNum === 1)

  const clickBreadcrumb = (breadcrumb: BreadcrumbItem) => {
    if (breadcrumb.id !== currBreadcrumb.value?.id) {
      const index = breadcrumbs.value.findIndex((v) => v.id === breadcrumb.id)
      breadcrumbs.value.splice(index + 1, breadcrumbLen.value - index - 1)
      updataPaginationsInBaseTable(currBreadcrumb.value?.pagination)
      stopPolling()
      startPolling()
    }
  }

  const updateBreadcrumbs = (data: BreadcrumbItem) => {
    // task log open
    if (breadcrumbLen.value + 1 > apiMap.value.length) {
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
      const newBreadcrumb = createBreadcrumbItem({ id: currId, name: data.name as string })
      breadcrumbs.value.push(newBreadcrumb)

      stopPolling()
      updataPaginationsInBaseTable()
      startPolling()
    }
  }

  const createBreadcrumbItem = (item: { id: string; name: string }) => {
    return {
      id: item.id,
      name: item.name,
      pagination: {
        pageNum: 1,
        pageSize: 10
      }
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
      const res = await api(apiParams.value, currBreadcrumb.value!.pagination)
      dataSource.value = res.content
      paginationProps.value.total = res.total
    } catch (error) {
      console.log('error :>> ', error)
      stopPolling()
    } finally {
      loading.value = false
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

  const onRetry = (row: JobVO | StageVO | TaskVO) => {
    Modal.confirm({
      title: () =>
        h('div', { style: { display: 'flex' } }, [
          h(SvgIcon, { name: 'unknown', style: { width: '24px', height: '24px' } }),
          h('p', t('job.retry'))
        ]),
      style: { top: '30vh' },
      icon: null,
      async onOk() {
        try {
          const state = await retryJob({ jobId: row.id!, clusterId: clusterInfo.id! })
          row.state = state.state
        } catch (error) {
          console.log('error :>> ', error)
        }
      }
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
      <template #bodyCell="{ record, text, column, index }">
        <template v-if="column.key === 'name'">
          <a-typography-link underline @click="updateBreadcrumbs(record as BreadcrumbItem)">
            <span :title="record.name">{{ record.name }}</span>
          </a-typography-link>
        </template>
        <template v-if="column.key === 'state'">
          <job-progress v-if="breadcrumbLen === 1" :key="record.id" :state="text" :progress-data="record.progress">
            <template #icon>
              <a-button
                v-if="text === 'Failed' && index === 0 && isFirstPageNum"
                :title="t('common.retry')"
                size="small"
                type="text"
                shape="circle"
                @click="onRetry(record)"
              >
                <template #icon>
                  <svg-icon color="var(--color-error)" style="margin: 0" name="retry" />
                </template>
              </a-button>
              <div v-else class="svg-icon-box">
                <svg-icon style="margin: 0" :name="status[record.state as StateType]" />
              </div>
            </template>
          </job-progress>
          <svg-icon v-else :name="status[record.state as StateType]" />
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

<style lang="scss" scoped>
  .svg-icon-box {
    border: 1px solid transparent;
    display: flex;
    width: 22px;
    align-items: center;
    min-width: 24px;
    min-height: 24px;
    justify-content: center;
  }
</style>
