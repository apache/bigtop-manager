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
  import { useI18n } from 'vue-i18n'
  import { getJob } from '@/api/job'
  import { JOB_SCHEDULE_INTERVAL } from '@/utils/constant.ts'
  import { useIntervalFn } from '@vueuse/core'
  import { onBeforeMount, onBeforeUnmount, reactive, ref } from 'vue'
  import { useClusterStore } from '@/store/cluster'
  import { storeToRefs } from 'pinia'
  import CustomProgress from '@/components/job-info/custom-progress.vue'
  import Job from '@/components/job-info/job.vue'
  import { JobVO, StageVO } from '@/api/job/types'

  const clusterInfo = defineModel<any>('clusterInfo')
  const disableButton = defineModel<boolean>('disableButton')

  const clusterStore = useClusterStore()
  const { clusterId } = storeToRefs(clusterStore)

  const { t } = useI18n()
  const loading = ref(true)
  const jobWindowOpened = ref(false)
  const jobState = ref<string>('')
  const jobs = ref<JobVO[]>([])
  const currStage = ref<StageVO>()
  const installData = reactive([])

  const installColumns = [
    {
      title: t('common.stage'),
      dataIndex: 'name',
      align: 'center'
    },
    {
      title: t('common.progress'),
      dataIndex: 'state',
      align: 'center'
    }
  ]

  const initData = async () => {
    const res = await getJob(clusterInfo.value.jobId, clusterId.value)
    // const res: any = await getFetch()
    jobs.value = [res] as any
    // console.log(res)
    jobState.value = res.state

    if (!['Pending', 'Processing'].includes(jobState.value)) {
      disableButton.value = false
      clusterInfo.value.success = jobState.value === 'Successful'
    }
    return res.stages.sort((a: StageVO, b: StageVO) => a.order - b.order)
  }

  const clickStage = (record: StageVO) => {
    jobWindowOpened.value = true
    currStage.value = record
  }

  onBeforeMount(async () => {
    disableButton.value = true
    const { pause } = useIntervalFn(
      async () => {
        Object.assign(installData, await initData())
        loading.value = false
        if (!['Pending', 'Processing'].includes(jobState.value)) {
          pause()
        }
      },
      JOB_SCHEDULE_INTERVAL,
      { immediateCallback: true }
    )
  })

  onBeforeUnmount(() => {
    disableButton.value = false
  })

  const onNextStep = async () => {
    return Promise.resolve(true)
  }

  defineExpose({
    onNextStep
  })
</script>

<template>
  <div class="container">
    <div class="title">{{ $t('common.install') }}</div>
    <a-table
      :pagination="false"
      :scroll="{ y: 400 }"
      :columns="installColumns"
      :data-source="installData"
      :loading="loading"
    >
      <template #bodyCell="{ column, text, record }">
        <template v-if="column.dataIndex === 'name'">
          <a @click="clickStage(record)">
            {{ text }}
          </a>
        </template>
        <template v-if="column.dataIndex === 'state'">
          <custom-progress
            :key="record.id"
            :state="text"
            :progress-data="record.tasks"
          />
        </template>
      </template>
    </a-table>
    <job
      v-model:visible="jobWindowOpened"
      :outer-data="{ meta: jobs, currItem: currStage }"
    />
  </div>
</template>

<style scoped lang="less">
  .container {
    .flexbox-mixin(column,null,start,center);
    align-content: center;
    height: 100%;

    .title {
      font-size: 1.5rem;
      line-height: 2rem;
      margin-bottom: 1rem;

      .progress {
        width: 80%;
      }
    }
  }
</style>
