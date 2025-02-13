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
  import { computed, ref, watch } from 'vue'
  import { copyText, scrollToBottom } from '@/utils/tools'
  import { message } from 'ant-design-vue'
  import { useI18n } from 'vue-i18n'
  import { getLogs } from '@/api/sse'
  import type { AxiosProgressEvent, Canceler } from 'axios'

  type EventListener = (val: any) => void

  interface Option {
    icon: 'copy' | 'download'
    text: string
    on: Record<string, EventListener>
  }

  interface PayLoad {
    clusterId?: number
    jobId: number
    stageId: number
    taskId: number
  }

  export interface LogsViewProps {
    open: boolean
    loading: boolean
    payLoad?: PayLoad
    subTitle?: string
  }

  const props = defineProps<LogsViewProps>()
  const emits = defineEmits(['update:open'])

  const { t } = useI18n()
  const title = ref('task_log')
  const viewRef = ref<HTMLElement | null>(null)
  const parsedLogs = ref<string | null>(null)
  const logMessage = ref<string>('')
  const canceler = ref<Canceler>()
  const copiedFlag = ref(false)
  const clusterId = ref(0)
  const taskId = ref<number | undefined>()
  const options = computed((): Option[] => {
    return [
      {
        icon: 'copy',
        text: copiedFlag.value ? 'common.copied' : 'common.copy',
        on: {
          click: copiedFlag.value ? () => {} : () => copyLogs()
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
    parsedLogs.value = parseLogContent(val)
  })

  watch(open, (val) => {
    val ? getLogMessage() : cancelSseConnect()
  })

  const parseLogContent = (content: string) => {
    return content
      .split('\n\n')
      .map((s) => {
        return s.substring(5)
      })
      .join('\n')
  }

  const getLogMessage = async () => {
    if (taskId.value === undefined) {
      return
    }
    try {
      const { cancel } = getLogs(clusterId.value, taskId.value, getLogProgress)
      canceler.value = cancel
    } catch (error) {
      console.log('error :>> ', error)
    }
  }

  const getLogProgress = ({ event }: AxiosProgressEvent) => {
    logMessage.value = event.target.responseText
    scrollToBottom(viewRef.value)
  }

  const cancelSseConnect = () => {
    canceler.value && canceler.value()
  }

  const copyLogs = () => {
    if (!parsedLogs.value) {
      return
    }
    copyText(parsedLogs.value)
      .then(() => {
        copiedFlag.value = true
        message.success(`${t('common.copy_success')}`)
        setTimeout(() => {
          copiedFlag.value = false
        }, 5000)
      })
      .catch((err: Error) => {
        message.error(`${t('common.copy_fail')}`)
        console.log('err :>> ', err)
      })
  }

  const downloadLogs = () => {
    console.log('download :>> ')
  }

  const handleOk = () => {
    emits('update:open', false)
  }
</script>

<template>
  <a-modal width="800px" :mask="false" :open="open" :title="$t(`job.${title}`)" @ok="handleOk" @cancel="handleOk">
    <template #closeIcon>
      <svg-icon style="margin: 0" name="close" />
    </template>
    <template #footer>
      <a-button key="submit" type="primary" :loading="props.loading" @click="handleOk">确认</a-button>
    </template>
    <section>
      <span class="sub-title">{{ props.subTitle || 'Check hosts on bm-01' }}</span>
      <a-space :size="16" class="options">
        <div v-for="option in options" :key="option.icon" v-on="option.on">
          <svg-icon :name="option.icon" />
          <a-typography-link underline :content="$t(option.text)" />
        </div>
      </a-space>
    </section>
    <article>
      <pre ref="viewRef"> {{ parsedLogs }}</pre>
    </article>
  </a-modal>
</template>

<style lang="scss" scoped>
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
      min-height: 500px;
      overflow: hidden;
      background-color: rgba(0, 0, 0, 0.88);
      height: auto;
      color: #fff;
      padding: 0;
      margin: 0;
    }
  }
</style>
