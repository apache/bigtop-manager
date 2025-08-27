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
  import { Empty } from 'ant-design-vue'
  import { getJobDetails, retryJob } from '@/api/job'
  import { CommandVO } from '@/api/command/types'
  import { useCreateServiceStore } from '@/store/create-service'

  import LogsView, { type LogViewProps } from '@/features/log-view/index.vue'
  import type { JobVO, StageVO, StateType, TaskVO } from '@/api/job/types'

  const props = defineProps<{ stepData: CommandVO }>()
  const { stepData } = toRefs(props)

  const { t } = useI18n()
  const createStore = useCreateServiceStore()
  const activeKey = ref<number[]>([])
  const jobDetail = ref<JobVO>({})
  const spinning = ref(false)

  const logsViewState = reactive<LogViewProps>({
    open: false
  })
  const status = shallowRef<Record<StateType, string>>({
    Pending: 'installing',
    Processing: 'processing',
    Failed: 'failed',
    Canceled: 'canceled',
    Successful: 'success'
  })

  const stages = computed(() => {
    if (jobDetail.value.stages) {
      return [...jobDetail.value.stages].sort((a, b) => a.order! - b.order!)
    }
    return []
  })

  const getJobInstanceDetails = async () => {
    const { id: jobId, clusterId } = stepData.value
    if (jobId === undefined) {
      return true
    }
    try {
      const data = await getJobDetails({ jobId, clusterId })
      jobDetail.value = data
      createStore.updateInstalledStatus(data.state as StateType)
      return ['Successful', 'Failed'].includes(data.state as StateType)
    } catch (error) {
      console.log('error :>> ', error)
      return true
    }
  }

  const handleRetryJob = async () => {
    const { id: jobId, clusterId } = stepData.value
    if (jobId === undefined) {
      return true
    }
    try {
      await retryJob({ jobId, clusterId })
      pollJobDetails(getJobInstanceDetails)
    } catch (error) {
      console.log('error :>> ', error)
    }
  }

  // eslint-disable-next-line @typescript-eslint/no-unsafe-function-type
  const pollJobDetails = (execFunc: Function, interval: number = 1000): void => {
    let firstPoll = true
    let firstPollCompleted = false
    let intervalId: NodeJS.Timeout

    const poll = async () => {
      if (firstPoll) {
        spinning.value = true
        firstPoll = false
      }
      const result = await execFunc()
      if (!firstPollCompleted) {
        spinning.value = false
        firstPollCompleted = true
      }
      if (result) {
        clearInterval(intervalId)
      }
    }

    intervalId = setInterval(poll, interval)
    poll()
  }

  const viewLogs = (stage: StageVO, task: TaskVO) => {
    const { id: jobId, clusterId } = stepData.value
    const { id: stageId } = stage
    const { id: taskId } = task
    if (jobId === undefined || stageId === undefined || taskId === undefined) {
      return
    }
    logsViewState.payLoad = {
      clusterId,
      jobId,
      stageId,
      taskId
    }
    logsViewState.open = true
    logsViewState.subTitle = task.name
  }

  onMounted(() => {
    pollJobDetails(getJobInstanceDetails)
  })
</script>

<template>
  <a-spin :spinning="spinning">
    <a-empty v-if="stages.length == 0" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
    <div v-else class="component-installer">
      <div class="retry">
        <a-button v-if="jobDetail.state === 'Failed'" type="link" @click="handleRetryJob">
          {{ t('common.retry') }}
        </a-button>
      </div>
      <a-collapse v-model:active-key="activeKey" :bordered="false" :ghost="true">
        <a-collapse-panel v-for="stage in stages" :key="stage.id">
          <template #header>
            <div class="stage-item">
              <span>{{ stage.name }}</span>
              <div style="min-width: 100px">
                <svg-icon :name="stage.state && status[stage.state]"></svg-icon>
              </div>
            </div>
          </template>
          <div v-for="task in stage.tasks" :key="task.id" class="task-item">
            <span>{{ task.name }}</span>
            <a-space :size="16">
              <svg-icon :name="task.state && status[task.state]"></svg-icon>
              <div style="min-width: 62px">
                <a-button
                  v-if="task.state && !['Canceled', 'Pending'].includes(task.state)"
                  type="link"
                  @click="viewLogs(stage, task)"
                >
                  {{ t('cluster.view_log') }}
                </a-button>
              </div>
            </a-space>
          </div>
        </a-collapse-panel>
      </a-collapse>
      <logs-view
        v-model:open="logsViewState.open"
        :pay-load="logsViewState.payLoad"
        :sub-title="logsViewState.subTitle"
      />
    </div>
  </a-spin>
</template>

<style lang="scss" scoped>
  .component-installer {
    button {
      padding: 0;
    }
    :deep(.ant-collapse-header) {
      background-color: $color-fill-quaternary;
    }
    :deep(.ant-collapse-content-box) {
      padding: 0 !important;
    }
    .retry {
      text-align: end;
      line-height: 14px;
      margin-bottom: $space-sm;
      button {
        margin: 0;
        padding: 0;
        line-height: inherit;
        height: 0;
      }
    }
  }
  .stage-item {
    @include flexbox($justify: space-between, $align: center);
    .svg-icon {
      width: 16px;
      height: 16px;
    }
  }
  .task-item {
    height: 45px;
    padding: 10px;
    box-sizing: border-box;
    padding-left: $space-lg;
    border-top: 1px solid $color-border-secondary;
    @include flexbox($justify: space-between, $align: center);
    &:last-child {
      border-bottom: 1px solid $color-border-secondary;
    }
    .svg-icon {
      width: 16px;
      height: 16px;
    }
  }
</style>
