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
  import { ref, watch, computed, toRefs, nextTick, watchEffect } from 'vue'
  import { useClusterStore } from '@/store/cluster/index.ts'
  import { storeToRefs } from 'pinia'
  import { JobVO, StageVO, TaskVO, OuterData } from '@/api/job/types.ts'
  import { getJobs } from '@/api/job/index.ts'
  import { Pausable, useIntervalFn } from '@vueuse/core'
  import { MONITOR_SCHEDULE_INTERVAL } from '@/utils/constant.ts'
  import CustomProgress from './custom-progress.vue'
  import Stage from './stage.vue'
  import Task from './task.vue'
  import TaskLog from './task-log.vue'
  import useBaseTable from '@/composables/use-base-table'
  import type { TableColumnType, TablePaginationConfig } from 'ant-design-vue'

  const columns: TableColumnType[] = [
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

  const showLogAwaitMsg = ref(false)
  const isComplete = ref(false)
  const stages = ref<StageVO[]>([])
  const tasks = ref<TaskVO[]>([])
  const breadcrumbs = ref<any[]>([{ name: 'Job Info' }])
  const currTaskInfo = ref<TaskVO>()
  const intervalId = ref<Pausable | undefined>()
  const logRef = ref<InstanceType<typeof TaskLog> | null>(null)
  const currPage = ref<string[]>([
    'isJobTable',
    'isStageTable',
    'isTaskTable',
    'isTaskLogs'
  ])

  const {
    loading,
    columnsProp,
    dataSource: jobs,
    paginationProps,
    onChange,
    resetState
  } = useBaseTable<JobVO>({ columns })

  const getCurrPage = computed(() => {
    return currPage.value[breadcrumbs.value.length - 1]
  })

  watch(visible, (val) => {
    if (!val) return
    resetState()
    loading.value = true
    checkDataOrigin(outerData.value ? true : false)
  })

  watchEffect(() => {
    if (!outerData.value) return
    jobs.value = outerData.value.meta as any
  })

  watchEffect(() => {
    if (!jobs.value) return
    const id = breadcrumbs.value[breadcrumbs.value.length - 1]?.id
    id &&
      (stages.value = jobs.value.find((v: JobVO) => v.id == id)?.stages || [])
  })

  const checkDataOrigin = (isOuter = false) => {
    if (isOuter) {
      const { meta, currItem } = outerData.value as OuterData
      jobs.value = meta
      clickJob(meta[0])
      clickStage(currItem as StageVO)
      loading.value = false
      return
    }
    initInterval()
  }

  const initInterval = () => {
    if (intervalId.value) {
      intervalId.value?.resume()
      return
    }
    intervalId.value = useIntervalFn(getJobsList, MONITOR_SCHEDULE_INTERVAL, {
      immediate: true,
      immediateCallback: true
    })
  }

  const getJobsList = async () => {
    try {
      const { current, pageSize } = paginationProps.value
      const { content, total } = await getJobs(clusterId.value, {
        pageNum: current as number,
        pageSize: pageSize as number,
        sort: 'desc'
      })
      jobs.value = content.map((v) => ({ ...v }))
      paginationProps.value.total = total
      loading.value = false
    } catch (error) {
      console.log('error :>> ', error)
    } finally {
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

  const handleClose = () => {
    intervalId.value?.pause()
    breadcrumbs.value = [{ name: 'Job Info' }]
    resetState()
    emits('update:visible', false)
  }

  const setShowLogAwaitMsg = (status: boolean) => {
    showLogAwaitMsg.value = status
  }

  const onLogComplete = (status: boolean) => {
    isComplete.value = status
  }

  const onTableChange = (pagination: TablePaginationConfig) => {
    onChange(pagination)
    loading.value = true
    getJobsList()
  }
</script>

<template>
  <a-modal :open="props.visible" width="95%" @cancel="handleClose">
    <template #footer>
      <div :class="{ 'footer-btns': showLogAwaitMsg }">
        <div
          v-if="showLogAwaitMsg"
          class="logs-wait-msg"
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
      :scroll="{ y: 400 }"
      :loading="loading"
      :data-source="jobs"
      :columns="columnsProp"
      :pagination="paginationProps"
      destroy-on-close
      @change="onTableChange"
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
        @on-log-complete="onLogComplete"
      />
    </template>
  </a-modal>
</template>

<style lang="scss" scoped>
  .footer-btns {
    @include flexbox($wrap: wrap, $justify: space-between, $align: center);
  }

  .logs-wait-msg {
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
      content: '. .';
    }
    48% {
      content: '. . .';
    }
    64% {
      content: '. . . .';
    }
    80% {
      content: '. . . . .';
    }
    96% {
      content: '. . . . . .';
    }
  }
  .breadcrumb {
    :deep(.ant-breadcrumb) {
      margin-bottom: 16px !important;
    }
  }
</style>
