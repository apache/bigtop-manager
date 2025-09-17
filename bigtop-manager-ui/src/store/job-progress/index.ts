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
import { useClusterStore } from '@/store/cluster'
import { execCommand } from '@/api/command'
import { getJobDetails } from '@/api/job'

import SvgIcon from '@/components/base/svg-icon/index.vue'
import JobModal from '@/features/job-modal/index.vue'

import { type CommandRequest } from '@/api/command/types'
import type { JobParams, JobVO } from '@/api/job/types'

export type StatusType = 'processing' | 'success' | 'failed' | 'pending'
export type ExecFunc = (params: JobParams) => Promise<JobVO | undefined>
export type CommandRes = { name: string } & JobParams
export interface JobStageProgressItem extends Partial<CommandRequest> {
  percent: number
  status: 'success' | 'normal' | 'active' | 'exception'
  icon: string
  desc: string
  payLoad?: JobVO
}

type PayLoad = { displayName?: string | string[] } & Record<string, any>

type JobStageProgress = Record<StatusType, () => JobStageProgressItem>

export const useJobProgress = defineStore('job-progress', () => {
  const { t } = useI18n()
  const instance = getCurrentInstance()
  const clusterStore = useClusterStore()
  const progressMap = reactive<Map<number, JobStageProgressItem>>(new Map())
  const jobStageProgress = shallowRef<JobStageProgress>({
    pending: () => ({
      percent: 0,
      status: 'normal',
      icon: 'processing',
      desc: 'job.pending'
    }),
    processing: () => ({
      percent: 1,
      status: 'active',
      icon: 'processing',
      desc: 'job.processing'
    }),
    success: () => ({
      percent: 100,
      status: 'success',
      icon: 'success',
      desc: 'job.success'
    }),
    failed: () => ({
      percent: 100,
      status: 'exception',
      icon: 'failed',
      desc: 'job.failed'
    })
  })

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
    const targetCluster = clusterStore.clusterMap[clusterId]
    return targetCluster ? targetCluster.displayName : 'Global'
  }

  const createStateIcon = (execRes: CommandRes) => {
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

  const createStateProgress = (execRes: CommandRes, nextAction?: (...args: any) => void) => {
    const progressData = progressMap.get(execRes.jobId)
    return h('div', [
      h(Progress, {
        percent: progressData?.percent,
        status: progressData?.status,
        showInfo: false,
        size: [300, 8]
      }),
      createStateDesc(execRes, nextAction)
    ])
  }

  const createStateDesc = (execRes: CommandRes, nextAction?: (...args: any) => void) => {
    const progressData = progressMap.get(execRes.jobId)
    let retryJobOperation = h('span', `${progressData?.percent}%`)
    if (progressData?.status === 'exception') {
      retryJobOperation = h(
        Button,
        {
          size: 'small',
          type: 'link',
          style: jobProgressStyle.value.retryBtn,
          onClick: (e: Event) => retryJob(e, execRes, nextAction)
        },
        {
          icon: () =>
            h(SvgIcon, {
              name: 'retry',
              style: { margin: 0 }
            }),
          default: () => h('span', {}, t('common.retry'))
        }
      )
    }
    return h('div', { style: jobProgressStyle.value.desc }, [
      h('span', { class: { 'text-loading': progressData?.status === 'active' } }, `${t(`${progressData?.desc}`)}`),
      retryJobOperation
    ])
  }

  const retryJob = (e: Event, execRes: CommandRes, nextAction?: (...args: any) => void) => {
    e.stopPropagation()
    Object.assign(progressMap.get(execRes.jobId)!, jobStageProgress.value.processing())
    pollJobDetails(execRes, getJobInstanceDetails, 1000, nextAction)
  }

  const getJobInstanceDetails = async (params: JobParams) => {
    try {
      const data = await getJobDetails(params)
      return data
    } catch (error) {
      console.log('error :>> ', error)
    }
  }

  const pollJobDetails = (
    params: JobParams,
    execFunc: ExecFunc,
    interval: number = 1000,
    nextAction?: (...args: any) => void
  ) => {
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
          progressMap.set(params.jobId, Object.assign(params, jobStageProgress.value.failed()))
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
        const mergeProgressPercent = { ...targetProgress, percent: oldPercent + 2 }
        progressEntry!.payLoad = result

        if (state === 'Successful' || state === 'Failed') {
          Object.assign(progressEntry!, targetProgress)
          if (state === 'Successful') {
            if (nextAction) {
              nextAction()
            }
            closeNotification(params.jobId)
          }
          cleanup()
        } else if (state === 'Processing' && oldPercent < 90) {
          Object.assign(progressEntry, mergeProgressPercent)
        }
      } catch (error) {
        console.log('error :>> ', error)
      }
    }
    intervalId = setInterval(checkStatus, interval)
  }

  const openNotification = (execRes: CommandRes, nextAction?: (...args: any) => void) => {
    pollJobDetails(execRes, getJobInstanceDetails, 1000, nextAction)
    notification.open({
      key: `${execRes.jobId}`,
      message: `${getClusterDisplayName(execRes.clusterId)} - ${execRes.name}`,
      duration: null,
      placement: 'bottomRight',
      style: { padding: '16px' },
      class: 'job-progress-notification',
      icon: () => createStateIcon(execRes),
      description: () => createStateProgress(execRes, nextAction),
      onClick: () => onClick(execRes)
    })
  }

  const closeNotification = (key: string | number, delay = 10000) => {
    setTimeout(() => {
      notification.close(typeof key === 'number' ? `${key}` : key)
    }, delay)
  }

  /**
   * Processes a command request by showing a confirmation modal.
   * @param params command request parameters
   * @param nextAction callback function to execute after the command is processed
   * @param payLoad additional payload for the command
   * @returns void
   */
  const processCommand = (params: CommandRequest, nextAction?: (...args: any[]) => void, payLoad?: PayLoad) => {
    const { displayName = '', tips } = payLoad as PayLoad
    const action = t(`common.${params.command.toLowerCase()}`).toLowerCase()

    let title = tips ?? 'common.confirm_action'
    let target = typeof displayName === 'string' ? displayName : ''

    if (Array.isArray(displayName)) {
      if (displayName.length > 1) {
        title = 'common.confirm_comp_action'
      } else {
        target = displayName[0]
      }
    }

    Modal.confirm({
      title: () =>
        h('div', { style: { display: 'flex' } }, [
          h(SvgIcon, { name: 'unknown', style: { width: '24px', height: '24px' } }),
          h('p', t(`${title}`, target === '' ? { action } : { action, target }))
        ]),
      style: { top: '30vh' },
      icon: null,
      async onOk() {
        try {
          const { id: jobId, name } = await execCommand(params)
          if (jobId && name) {
            progressMap.set(jobId, Object.assign(params, jobStageProgress.value.processing()))
            openNotification({ jobId, clusterId: params.clusterId!, name }, nextAction)
          }
        } catch (error) {
          console.log('error :>> ', error)
        }
      }
    })
  }

  const onClick = (execRes: CommandRes) => {
    Modal.destroyAll()
    Modal.confirm({
      icon: null,
      width: '980px',
      zIndex: 9999,
      mask: false,
      closable: true,
      appContext: instance?.appContext,
      content: () => h(JobModal, { execRes, jobInfo: progressMap })
    })
  }

  return {
    processCommand
  }
})
