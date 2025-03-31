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

import { notification, Progress, Avatar } from 'ant-design-vue'
import { computed, h, reactive, shallowRef } from 'vue'
import { defineStore } from 'pinia'
import SvgIcon from '@/components/common/svg-icon/index.vue'
import { CommandRequest } from '@/api/command/types'
// import { getJobDetails } from '@/api/job'
import { JobParams, JobVO } from '@/api/job/types'

type StatusType = 'processing' | 'success' | 'failed'

interface JobStageProgressItem extends Partial<CommandRequest> {
  percent: number
  status: 'success' | 'normal' | 'active' | 'exception'
  icon: string
  duration: number | null
  desc: string
  payLoad?: any
}

type JobStageProgress = Record<StatusType, JobStageProgressItem>

export const useJobProgress = defineStore('job-progress', () => {
  const progressMap = reactive<Map<number, JobStageProgressItem>>(new Map())
  const jobStageProgress = shallowRef<JobStageProgress>({
    processing: {
      percent: 50,
      status: 'active',
      icon: 'processing',
      duration: 0,
      desc: '运行中......'
    },
    success: {
      percent: 100,
      status: 'success',
      icon: 'success',
      duration: 8,
      desc: '运行成功！'
    },
    failed: {
      percent: 100,
      status: 'exception',
      icon: 'failed',
      duration: 0,
      desc: '运行失败！'
    }
  })
  const defaultJobStageProgress = computed(() => jobStageProgress.value.processing)

  // // test
  function getRandomJobStage(): string {
    const keys = ['Processing', 'Successful', 'Failed']
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

  //

  const iconStyle = computed(() => {
    return {
      avatar: { backgroundColor: '#fff' },
      icon: { padding: 0, margin: 0, height: '27px', width: '27px' }
    }
  })

  const onClick = () => {
    console.log('current :>> ')
  }

  const createIcon = (execRes: JobParams) => {
    return h(
      Avatar,
      { size: 32, shape: 'circle', style: iconStyle.value.avatar },
      {
        icon: () =>
          h(SvgIcon, {
            name: progressMap.get(execRes.jobId)?.icon || defaultJobStageProgress.value.icon,
            style: iconStyle.value.icon
          })
      }
    )
  }

  const createDescription = (execRes: JobParams) => {
    const { jobId } = execRes
    const progressData = progressMap.get(jobId)
    return h('div', [
      h(Progress, {
        percent: progressMap.get(jobId)?.percent || defaultJobStageProgress.value.percent,
        status: progressMap.get(jobId)?.status || defaultJobStageProgress.value.status,
        showInfo: false,
        size: [300, 8]
      }),
      h('div', { style: { display: 'flex', fontSize: '12px', 'justify-content': 'space-between' } }, [
        h('span', `${progressMap.get(jobId)?.desc || defaultJobStageProgress.value.desc}`),
        h('span', `${progressMap.get(jobId)?.percent || defaultJobStageProgress.value.percent}%`)
      ])
    ])
  }

  const getJobInstanceDetails = async (params: JobParams) => {
    try {
      const state = getRandomJobStage()
      const data = await Promise.resolve({ state } as any)
      // const data = await getJobDetails(params)
      const initProgress = jobStageProgress.value[`${state === 'Successful' ? 'success' : state.toLowerCase()}`]
      Object.assign(progressMap.get(params.jobId)!, initProgress)
      progressMap.get(params.jobId)!.payLoad = data

      return data
    } catch (error) {
      console.log('error :>> ', error)
    }
  }

  const pollJobDetails = (
    params: JobParams,
    execFunc: (params: JobParams) => Promise<JobVO | undefined>,
    interval: number = 1000
  ): void => {
    const intervalId = setInterval(async () => {
      const result = await execFunc(params)
      if (result?.state === 'Successful' || result?.state === 'Failed') {
        clearInterval(intervalId)
      }
    }, interval)
  }

  const openNotification = (execRes: JobParams, execParams: CommandRequest) => {
    pollJobDetails(execRes, getJobInstanceDetails)
    notification.open({
      message: `${execParams.command} ${execParams.commandLevel}`,
      duration: null,
      placement: 'bottomRight',
      style: {
        padding: '16px'
      },
      class: 'notification-custom-class',
      icon: () => createIcon(execRes),
      description: () => createDescription(execRes),
      onClick: onClick
    })
  }

  const processCommand = async (params: CommandRequest) => {
    try {
      const { id: jobId } = await Promise.resolve({ id: generateRandomNumber(8), state: getRandomJobStage() })
      progressMap.set(jobId, params as JobStageProgressItem)
      openNotification({ jobId, clusterId: params.clusterId! }, params)
    } catch (error) {
      console.log('error :>> ', error)
    }
  }

  return {
    processCommand
  }
})
