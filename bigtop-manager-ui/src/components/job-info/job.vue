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
  import { ref, watch, computed, reactive, toRaw, toRefs } from 'vue'
  import { useClusterStore } from '@/store/cluster'
  import { PaginationConfig } from 'ant-design-vue/es/pagination/Pagination'
  import { CopyOutlined } from '@ant-design/icons-vue'
  import { storeToRefs } from 'pinia'
  import { message } from 'ant-design-vue'
  import {
    JobVO,
    StageVO,
    TaskVO,
    OuterData,
    Pagination
  } from '@/api/job/types.ts'
  import { getLogs } from '@/api/sse'
  import { getJobs } from '@/api/job'
  import { Pausable, useIntervalFn } from '@vueuse/core'
  import { AxiosProgressEvent } from 'axios'
  import { MONITOR_SCHEDULE_INTERVAL } from '@/utils/constant.ts'
  import CustomProgress from './custom-progress.vue'
  import Stage from './stage.vue'
  import Task from './task.vue'
  import { copyText } from '@/utils/tools'
  import { useI18n } from 'vue-i18n'
  const { t } = useI18n()

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

  const props = withDefaults(
    defineProps<{
      visible: boolean
      outerData?: OuterData
    }>(),
    {
      visible: false,
      outerData: undefined
    }
  )
  const { visible, outerData } = toRefs(props)

  const emits = defineEmits(['update:visible', 'closed'])

  const loading = ref(false)
  const stages = ref<StageVO[]>([])
  const tasks = ref<TaskVO[]>([])
  const breadcrumbs = ref<any[]>([{ name: 'Job Info' }])
  const currTaskInfo = ref<TaskVO>()
  const jobs = ref<JobVO[]>([])
  const intervalId = ref<Pausable | undefined>()
  const logTextOrigin = ref<string>('')
  const logsInfoRef = ref<HTMLElement | null>(null)
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

  const logText = computed(() => {
    return logTextOrigin.value
      .split('\n\n')
      .map((s) => {
        return s.substring(5)
      })
      .join('\n')
  })

  watch(visible, (val) => {
    if (val) {
      loading.value = true
      Object.assign(paginationProps, initPagedProps())
      if (outerData.value) {
        checkMetaOrigin(true)
      } else {
        checkMetaOrigin(false)
      }
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

  const getLogsInfo = (id: number) => {
    getLogs(clusterId.value, id, ({ event }: AxiosProgressEvent) => {
      logTextOrigin.value = event.target.responseText
      logsInfoRef.value = document.querySelector('.logsInfoRef')
      if (!logsInfoRef.value) {
        return
      }
      ;(function smoothscroll() {
        const { scrollTop, offsetHeight, scrollHeight } = logsInfoRef.value
        if (scrollHeight - 10 > scrollTop + offsetHeight) {
          window.requestAnimationFrame(smoothscroll)
          logsInfoRef.value.scrollTo(
            0,
            scrollTop + (scrollHeight - scrollTop - offsetHeight) / 2
          )
        }
      })()
    })
  }

  const copyLogTextContent = (text: string) => {
    copyText(text)
      .then(() => {
        message.success(`${t('common.copy_success')}`)
      })
      .catch((err: Error) => {
        message.error(`${t('common.copy_fail')}`)
        console.log('err :>> ', err)
      })
  }

  const clickTask = (record: TaskVO) => {
    breadcrumbs.value.push(record)
    currTaskInfo.value = record
    getLogsInfo(record.id)
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
</script>

<template>
  <a-modal :open="props.visible" width="95%" @cancel="handleClose">
    <template #footer>
      <a-button key="back" type="primary" @click="handleClose">
        {{ $t('common.confirm') }}
      </a-button>
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
      <div class="logs">
        <div class="logs_header">
          <span>Task Logs</span>
          <div class="logs_header-ops">
            <a-button
              type="link"
              size="small"
              @click="copyLogTextContent(logText)"
            >
              <template #icon>
                <copy-outlined />
              </template>
              <span class="copy-button">{{ $t('common.copy') }}</span>
            </a-button>
          </div>
        </div>
        <div ref="logsInfoRef" class="logs_info">
          <pre id="logs">{{ logText }}</pre>
        </div>
      </div>
    </template>
  </a-modal>
</template>

<style lang="scss" scoped>
  .breadcrumb {
    :deep(.ant-breadcrumb) {
      margin-bottom: 16px !important;
    }
  }
  .logs {
    height: 50vh;
    display: flex;
    flex-direction: column;
    &_header {
      font-size: 16px;
      font-weight: 600;
      margin: 0 0 10px 0;
      display: flex;
      justify-content: space-between;

      .copy-button {
        margin-left: 3px;
      }
    }
    &_info {
      height: 100%;
      overflow: auto;
      background-color: #f5f5f5;
      border-radius: 4px;
      position: relative;
      pre {
        height: 100%;
        margin: 0;
        padding: 16px 14px;
        box-sizing: border-box;
        color: #444;
        border-color: #eee;
        line-height: 16px;
      }
    }
  }
</style>
