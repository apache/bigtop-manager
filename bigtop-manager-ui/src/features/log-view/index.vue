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
  import { copyText, scrollToBottom } from '@/utils/tools'
  import { message } from 'ant-design-vue'
  import { getTaskLog } from '@/api/job'
  import type { AxiosProgressEvent, Canceler } from 'axios'

  type EventListener = (val: any) => void

  interface Option {
    icon: 'copy' | 'download'
    text: string
    on: Record<string, EventListener>
  }

  interface PayLoad {
    clusterId: number
    jobId: number
    stageId: number
    taskId: number
  }

  export interface LogViewProps {
    open: boolean
    width?: string | number
    payLoad?: PayLoad
    subTitle?: string
  }

  const props = defineProps<LogViewProps>()
  const emits = defineEmits(['update:open'])

  const { t } = useI18n()
  const title = ref('task_log')
  const loading = ref(false)
  const viewRef = ref<HTMLElement | null>(null)
  const parsedLog = ref<string | null>(null)
  const logMessage = ref<string>('')
  const canceler = ref<Canceler>()
  const options = computed((): Option[] => {
    return [
      {
        icon: 'copy',
        text: 'common.copy',
        on: {
          click: () => copyLogs()
        }
      },
      {
        icon: 'download',
        text: 'common.download',
        on: {
          click: () => downloadLogs()
        }
      }
    ]
  })

  watch(logMessage, (val) => {
    parsedLog.value = val
      .split('\n\n')
      .map((s) => {
        return s.substring(5)
      })
      .join('\n')
  })

  watch(
    () => props.open,
    (val) => {
      if (val) {
        getLogMessage()
      } else {
        cancelSseConnect()
        logMessage.value = ''
      }
    }
  )

  const getLogMessage = async () => {
    if (!props.payLoad) {
      return
    }
    try {
      loading.value = true
      const { cancel, promise } = getTaskLog({ ...props.payLoad }, getLogProgress)
      canceler.value = cancel
      promise.then(onLogComplete)
    } catch (error) {
      console.log('error :>> ', error)
      loading.value = false
    }
  }

  const onLogComplete = (res: string | undefined) => {
    loading.value = res == undefined
    cancelSseConnect()
  }

  const getLogProgress = ({ event }: AxiosProgressEvent) => {
    logMessage.value = event.target.responseText
    scrollToBottom(viewRef.value)
  }

  const cancelSseConnect = () => {
    if (canceler.value) {
      canceler.value()
    }
  }

  const copyLogs = () => {
    if (!parsedLog.value) {
      return
    }
    copyText(parsedLog.value)
      .then(() => {
        message.success(`${t('common.copied')}`)
      })
      .catch((err: Error) => {
        message.error(`${t('common.copy_fail')}`)
        console.log('err :>> ', err)
      })
  }

  const downloadLogs = () => {
    const blob = new Blob([parsedLog.value as BlobPart], { type: 'text/plain' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'log.txt'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
  }

  const handleOk = () => {
    emits('update:open', false)
  }
</script>

<template>
  <a-modal
    :width="$props.width || '60%'"
    style="min-width: 400px"
    :centered="true"
    :mask="false"
    :open="open"
    :z-index="99999"
    :title="t(`job.${title}`)"
    @ok="handleOk"
    @cancel="handleOk"
  >
    <template #closeIcon>
      <svg-icon style="margin: 0" name="close" />
    </template>
    <template #footer>
      <div class="log-load">
        <div>
          <span v-show="loading" class="text-loading">{{ t('job.log_loading') }}</span>
        </div>
        <a-button key="submit" type="primary" @click="handleOk">
          {{ t('common.confirm') }}
        </a-button>
      </div>
    </template>
    <section>
      <span class="sub-title">{{ props.subTitle || '' }}</span>
      <a-space :size="16" class="options">
        <div v-for="option in options" :key="option.icon" v-on="option.on">
          <svg-icon :name="option.icon" />
          <a-typography-link underline :content="t(option.text)" />
        </div>
      </a-space>
    </section>
    <article ref="viewRef">
      <pre>{{ parsedLog }}</pre>
    </article>
  </a-modal>
</template>

<style lang="scss" scoped>
  .log-load {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  section {
    display: flex;
    align-content: flex-end;
    justify-content: space-between;
    margin-bottom: $space-sm;
    color: rgba(0, 0, 0, 0.45);
  }
  article {
    height: 500px;
    overflow: auto;
    pre {
      font-size: 12px;
      padding: 8px 16px;
      line-height: 32px;
      min-height: 500px;
      overflow-y: hidden;
      background-color: rgba(0, 0, 0, 0.88);
      height: auto;
      color: #fff;
      margin: 0;
    }
  }
</style>
