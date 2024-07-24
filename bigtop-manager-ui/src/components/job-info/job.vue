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
  import { ref, watch, computed, reactive, toRaw, toRefs, nextTick } from 'vue'
  import { useClusterStore } from '@/store/cluster'
  import { PaginationConfig } from 'ant-design-vue/es/pagination/Pagination'
  import { storeToRefs } from 'pinia'
  import {
    JobVO,
    StageVO,
    TaskVO,
    OuterData,
    Pagination
  } from '@/api/job/types.ts'
  import { getJobs } from '@/api/job'
  import { Pausable, useIntervalFn } from '@vueuse/core'
  import { MONITOR_SCHEDULE_INTERVAL } from '@/utils/constant.ts'
  import CustomProgress from './custom-progress.vue'
  import Stage from './stage.vue'
  import Task from './task.vue'
  import TaskLog from './task-log.vue'

  const columns = [
    {
      title: 'common.name',
      dataIndex: 'name',
      align: 'center'
    },
    {
      title: 'common.status',
      dataIndex: 'state',
      align: 'center'
    },
    {
      title: 'common.create_time',
      dataIndex: 'createTime',
      align: 'center'
    },
    {
      title: 'common.update_time',
      dataIndex: 'updateTime',
      align: 'center'
    }
  ]

  const clusterStore = useClusterStore()
  const { clusterId } = storeToRefs(clusterStore)

  interface Props {
    visible: boolean
    outerData?: OuterData
  }

  const props = withDefaults(defineProps<Props>(), {
    visible: false,
    outerData: undefined
  })

  const { visible, outerData } = toRefs(props)

  const emits = defineEmits(['update:visible', 'closed'])

  const loading = ref(false)
  const showLogAwaitMsg = ref(false)
  const isComplete = ref(false)
  const stages = ref<StageVO[]>([])
  const tasks = ref<TaskVO[]>([])
  const breadcrumbs = ref<any[]>([{ name: 'Job Info' }])
  const currTaskInfo = ref<TaskVO>()
  const jobs = ref<JobVO[]>([])
  const intervalId = ref<Pausable | undefined>()
  const logRef = ref<InstanceType<typeof TaskLog> | null>()
  const currPage = ref<string[]>([
    'isJobTable',
    'isStageTable',
    'isTaskTable',
    'isTaskLogs'
  ])

  const paginationProps = reactive<PaginationConfig>({})
  const jobsPageState = reactive<Pagination>({
    pageNum: 1,
    pageSize: 10,
    sort: 'desc'
  })

  const getCurrPage = computed(() => {
    return currPage.value[breadcrumbs.value.length - 1]
  })

  watch(visible, (val) => {
    if (val) {
      loading.value = true
      Object.assign(paginationProps, initPagedProps())
      checkMetaOrigin(outerData.value ? true : false)
      loading.value = false
    }
  })

  watch(outerData, (val) => {
    if (!val) return
    jobs.value = val.meta as any
  })

  watch(jobs, (val) => {
    if (val) {
      const len = breadcrumbs.value.length
      const idxId = breadcrumbs.value[len - 1]?.id
      if (idxId) {
        stages.value = val.find((v: JobVO) => v.id == idxId)?.stages || []
      }
    }
  })

  const checkMetaOrigin = (isOuter = false) => {
    if (isOuter) {
      const { meta, currItem } = outerData.value as OuterData
      jobs.value = meta
      clickJob(meta[0])
      clickStage(currItem as StageVO)
      return
    }
    getJobsList()
    initInterval()
  }

  const initInterval = () => {
    if (intervalId.value) {
      intervalId.value?.resume()
      return
    }
    intervalId.value = useIntervalFn(
      async () => {
        await getJobsList()
      },
      MONITOR_SCHEDULE_INTERVAL,
      { immediate: false, immediateCallback: true }
    )
  }

  const getJobsList = async () => {
    try {
      const params = { ...toRaw(jobsPageState) }
      const { content, total } = await getJobs(clusterId.value, params)
      jobs.value = content.map((v) => {
        return {
          ...v
        }
      })
      paginationProps.total = total
      loading.value = false
    } catch (error) {
      loading.value = false
    }
  }

  const clickTask = async (record: TaskVO) => {
    breadcrumbs.value.push(record)
    currTaskInfo.value = record
    await nextTick()
    logRef.value?.getLogInfo(record.id)
  }

  const clickJob = (record: JobVO) => {
    stages.value = record.stages
    breadcrumbs.value.push(record)
  }

  const clickStage = (record: StageVO) => {
    tasks.value = record.tasks
    breadcrumbs.value.push(record)
  }

  const clickBreadCrumbs = (idx: number) => {
    const len = breadcrumbs.value.length
    breadcrumbs.value.splice(idx + 1, len)
  }

  const handlePageChange = (page: number) => {
    paginationProps.current = page
    jobsPageState.pageNum = page
    loading.value = true
    getJobsList()
  }

  const handlePageSizeChange = (_current: number, size: number) => {
    paginationProps.pageSize = size
    jobsPageState.pageSize = size
    loading.value = true
    getJobsList()
  }

  const initPagedProps = () => {
    return {
      current: 1,
      pageSize: 10,
      size: 'small',
      showSizeChanger: true,
      pageSizeOptions: ['10', '20', '30', '40', '50'],
      total: 0,
      onChange: handlePageChange,
      onShowSizeChange: handlePageSizeChange
    }
  }

  const handleClose = () => {
    intervalId.value?.pause()
    breadcrumbs.value = [{ name: 'Job Info' }]
    jobs.value = []
    Object.assign(jobsPageState, {
      pageNum: 1,
      pageSize: 10,
      sort: 'desc'
    })
    emits('update:visible', false)
  }

  const setShowLogAwaitMsg = (status: boolean) => {
    showLogAwaitMsg.value = status
  }

  const onLogCompalete = (status: boolean) => {
    isComplete.value = status
  }
</script>

<template>
  <a-modal :open="props.visible" width="95%" @cancel="handleClose">
    <template #footer>
      <div :class="{ 'footer-btns': showLogAwaitMsg }">
        <div
          v-if="showLogAwaitMsg"
          class="logs_wait_msg"
          :class="{ 'loading-dot': !isComplete }"
        >
          {{
            isComplete
              ? $t('job.log_complete_message')
              : $t('job.log_await_message')
          }}
        </div>
        <a-button key="back" type="primary" @click="handleClose">
          {{ $t('common.confirm') }}
        </a-button>
      </div>
    </template>

    <div class="breadcrumb">
      <a-breadcrumb>
        <a-breadcrumb-item
          v-for="(item, idx) in breadcrumbs"
          :key="idx"
          @click="clickBreadCrumbs(idx)"
        >
          <a href="#">{{ item.name }}</a>
        </a-breadcrumb-item>
      </a-breadcrumb>
    </div>

    <a-table
      v-show="getCurrPage == 'isJobTable'"
      :scroll="{ y: 500 }"
      :pagination="paginationProps"
      :loading="loading"
      :data-source="jobs"
      :columns="columns"
      destroy-on-close
    >
      <template #headerCell="{ column }">
        <span>{{ $t(column.title) }}</span>
      </template>
      <template #bodyCell="{ column, text, record }">
        <template v-if="column.dataIndex === 'name'">
          <a @click="clickJob(record)">
            {{ text }}
          </a>
        </template>
        <template v-if="column.dataIndex === 'state'">
          <custom-progress
            :key="record.id"
            :state="text"
            :progress-data="record.stages"
          />
        </template>
      </template>
    </a-table>
    <template v-if="getCurrPage == 'isStageTable'">
      <stage :columns="columns" :stages="stages" @click-stage="clickStage" />
    </template>
    <template v-if="getCurrPage == 'isTaskTable'">
      <task :columns="columns" :tasks="tasks" @click-task="clickTask" />
    </template>
    <template v-if="getCurrPage == 'isTaskLogs'">
      <task-log
        ref="logRef"
        @vue:before-unmount="setShowLogAwaitMsg"
        @on-log-receive="setShowLogAwaitMsg"
        @on-log-compalete="onLogCompalete"
      />
    </template>
  </a-modal>
</template>

<style lang="scss" scoped>
  .footer-btns {
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-wrap: wrap;
  }

  .logs_wait_msg {
    margin: 0;
    padding: 0;
  }

  .loading-dot {
    &::after {
      content: '';
      animation: wait 5s 0s infinite;
    }
  }

  @keyframes wait {
    16% {
      content: '.';
    }
    32% {
      content: '..';
    }
    48% {
      content: '...';
    }
    64% {
      content: '....';
    }
    80% {
      content: '.....';
    }
    96% {
      content: '......';
    }
  }
  .breadcrumb {
    :deep(.ant-breadcrumb) {
      margin-bottom: 16px !important;
    }
  }
</style>
