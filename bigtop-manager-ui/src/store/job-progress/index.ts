/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import { notification, Progress, Avatar, Button, Modal } from 'ant-design-vue'
import { computed, h, reactive, shallowRef } from 'vue'
import { defineStore } from 'pinia'
import { useClusterStore } from '@/store/cluster'
import JobModal from '@/components/job-modal/index.vue'
import SvgIcon from '@/components/common/svg-icon/index.vue'
import { CommandRequest } from '@/api/command/types'
// import { getJobDetails } from '@/api/job'
import { JobParams, JobVO } from '@/api/job/types'

type StatusType = 'processing' | 'success' | 'failed'
type ExecFunc = (params: JobParams) => Promise<JobVO | undefined>

interface JobStageProgressItem extends Partial<CommandRequest> {
  percent: number
  status: 'success' | 'normal' | 'active' | 'exception'
  icon: string
  desc: string
  payLoad?: any
}

type JobStageProgress = Record<StatusType, () => JobStageProgressItem>

export const useJobProgress = defineStore('job-progress', () => {
  const clusterStore = useClusterStore()
  const progressMap = reactive<Map<number, JobStageProgressItem>>(new Map())
  const jobStageProgress = shallowRef<JobStageProgress>({
    processing: () => ({
      percent: 1,
      status: 'active',
      icon: 'processing',
      desc: '运行中......'
    }),
    success: () => ({
      percent: 100,
      status: 'success',
      icon: 'success',
      desc: '运行成功！'
    }),
    failed: () => ({
      percent: 100,
      status: 'exception',
      icon: 'failed',
      desc: '运行失败！'
    })
  })

  // // test
  function getRandomJobStage(): string {
    const keys = [
      'Processing',
      'Successful',
      'Failed',
      'Processing',
      'Processing',
      'Processing',
      'Processing',
      'Processing'
    ]
    const randomKey = keys[Math.floor(Math.random() * keys.length)]
    return randomKey
  }

  function generateRandomNumber(length) {
    let randomNumber = ''
    for (let i = 0; i < length; i++) {
      randomNumber += Math.floor(Math.random() * 10) // 生成0-9之间的随机数字
    }
    return Number(randomNumber)
  }

  const jobProgressStyle = computed(() => ({
    desc: { display: 'flex', fontSize: '12px', 'justify-content': 'space-between' },
    avatar: { backgroundColor: 'transparent', border: 'none' },
    icon: { padding: 0, margin: 0, height: '27px', width: '27px' },
    retryBtn: {
      display: 'flex',
      padding: 0,
      'align-items': 'center',
      gap: '4px',
      fontSize: '12px',
      height: 'auto',
      border: 'none'
    }
  }))

  const getClusterDisplayName = (clusterId: number) => {
    const clusters = clusterStore.clusters
    const index = clusters.findIndex((v) => v.id === clusterId)
    if (index != -1) {
      return clusters[index].displayName
    } else {
      return 'Global'
    }
  }

  const createStateIcon = (execRes: JobParams) => {
    return h(
      Avatar,
      { size: 27, shape: 'circle', style: jobProgressStyle.value.avatar },
      {
        icon: () =>
          h(SvgIcon, {
            name: progressMap.get(execRes.jobId)!.icon!,
            style: jobProgressStyle.value.icon
          })
      }
    )
  }

  const createStateProgress = (execRes: JobParams) => {
    const progressData = progressMap.get(execRes.jobId)
    return h('div', [
      h(Progress, {
        percent: progressData?.percent,
        status: progressData?.status,
        showInfo: false,
        size: [300, 8]
      }),
      createStateDesc(execRes)
    ])
  }

  const createStateDesc = (execRes: JobParams) => {
    const progressData = progressMap.get(execRes.jobId)
    let retryJobOperation = h('span', `${progressData?.percent}%`)
    if (progressData?.status === 'exception') {
      retryJobOperation = h(
        Button,
        {
          size: 'small',
          type: 'link',
          style: jobProgressStyle.value.retryBtn,
          onClick: (e: Event) => retryJob(e, execRes)
        },
        {
          default: () => [
            h('span', {}, '重试'),
            h(SvgIcon, {
              name: 'retry',
              style: { margin: 0 }
            })
          ]
        }
      )
    }
    return h('div', { style: jobProgressStyle.value.desc }, [h('span', `${progressData?.desc}`), retryJobOperation])
  }

  const retryJob = (e: Event, execRes: JobParams) => {
    e.stopPropagation()
    Object.assign(progressMap.get(execRes.jobId)!, jobStageProgress.value.processing())
    pollJobDetails(execRes, getJobInstanceDetails)
  }

  const getJobInstanceDetails = async (params: JobParams) => {
    try {
      console.log('params :>> ', params)
      // const data = await getJobDetails(params)
      const state = getRandomJobStage()
      return await Promise.resolve({ state } as any)
    } catch (error) {
      console.log('error :>> ', error)
    }
  }

  const pollJobDetails = (params: JobParams, execFunc: ExecFunc, interval: number = 1000) => {
    let intervalId: NodeJS.Timeout | null = null
    const cleanup = () => {
      if (intervalId) {
        clearInterval(intervalId)
        intervalId = null
      }
    }

    const checkStatus = async () => {
      try {
        const result = await execFunc(params)
        if (!result) {
          cleanup()
          return
        }

        const { state } = result
        const jobId = params.jobId
        const progressEntry = progressMap.get(jobId)
        if (!progressEntry) {
          cleanup()
          return
        }

        const currentState = state === 'Successful' ? 'success' : state!.toLowerCase()
        const targetProgress = jobStageProgress.value[`${currentState}`]() ?? progressEntry.percent
        const oldPercent = progressEntry?.percent ?? 0
        const mergeProgressPercent = { ...targetProgress, percent: oldPercent + 10 }
        progressEntry!.payLoad = result

        if (state === 'Successful' || state === 'Failed') {
          Object.assign(progressEntry!, targetProgress)
          state === 'Successful' && closeNotification(params.jobId)
          cleanup()
        } else if (state === 'Processing') {
          oldPercent < 90 && Object.assign(progressEntry, mergeProgressPercent)
        }
      } catch (error) {
        console.log('error :>> ', error)
      }
    }
    intervalId = setInterval(checkStatus, interval)
  }

  const openNotification = (execRes: JobParams, execParams: CommandRequest) => {
    console.log('execParams :>> ', execParams)
    pollJobDetails(execRes, getJobInstanceDetails)
    notification.open({
      key: `${execRes.jobId}`,
      message: `${getClusterDisplayName(execRes.clusterId)}`,
      duration: null,
      placement: 'bottomRight',
      style: {
        padding: '16px'
      },
      class: 'job-progress-notification',
      icon: () => createStateIcon(execRes),
      description: () => createStateProgress(execRes),
      onClick: () => onClick(execRes)
    })
  }

  const closeNotification = (key: string | number, delay = 10000) => {
    setTimeout(() => {
      notification.close(typeof key === 'number' ? `${key}` : key)
    }, delay)
  }

  const processCommand = async (params: CommandRequest) => {
    try {
      const { id: jobId } = await Promise.resolve({ id: generateRandomNumber(8), state: getRandomJobStage() })
      progressMap.set(jobId, Object.assign(params, jobStageProgress.value.processing()))
      openNotification({ jobId, clusterId: params.clusterId! }, params)
    } catch (error) {
      console.log('error :>> ', error)
    }
  }

  const onClick = (execRes: JobParams) => {
    console.log('execRes :>> ', execRes)

    Modal.confirm({
      title: 'Do you want to delete these items?',
      icon: null,
      content: () => h(JobModal, { jobInfo: { clusterId: execRes.clusterId } }),
      onOk() {
        return new Promise((resolve, reject) => {
          setTimeout(Math.random() > 0.5 ? resolve : reject, 1000)
        }).catch(() => console.log('Oops errors!'))
      }
    })
  }

  return {
    processCommand
  }
})
