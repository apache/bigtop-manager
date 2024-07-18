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
  import { copyText, scrollToBottom } from '@/utils/tools'
  import { message } from 'ant-design-vue'
  import { ref, watch, onBeforeUnmount } from 'vue'
  import { CopyOutlined } from '@ant-design/icons-vue'
  import { AxiosProgressEvent, Canceler } from 'axios'
  import { getLogs } from '@/api/sse'
  import { storeToRefs } from 'pinia'
  import { useClusterStore } from '@/store/cluster'

  const { t } = useI18n()
  const clusterStore = useClusterStore()
  const { clusterId } = storeToRefs(clusterStore)
  const logsInfoRef = ref<HTMLElement | null>(null)
  const logText = ref<string | null>(null)
  const logMeta = ref<string>('')
  const canceler = ref<Canceler>()

  watch(logMeta, (val) => {
    logText.value = val
      .split('\n\n')
      .map((s) => {
        return s.substring(5)
      })
      .join('\n')
  })

  const getLogsInfo = async (id: number) => {
    const { cancel } = getLogs(clusterId.value, id, getLogProgress)
    canceler.value = cancel
  }

  const getLogProgress = ({ event }: AxiosProgressEvent) => {
    logMeta.value = event.target.responseText
    scrollToBottom(logsInfoRef.value)
  }

  const cancelSseConnect = () => {
    if (!canceler.value) {
      return
    }
    canceler.value()
  }

  const copyLogTextContent = (text: string | null) => {
    if (!text) {
      return
    }
    copyText(text)
      .then(() => {
        message.success(`${t('common.copy_success')}`)
      })
      .catch((err: Error) => {
        message.error(`${t('common.copy_fail')}`)
        console.log('err :>> ', err)
      })
  }

  onBeforeUnmount(() => {
    cancelSseConnect()
  })

  defineExpose({
    getLogsInfo
  })
</script>

<template>
  <div class="logs">
    <div class="logs_header">
      <span>Task Logs</span>
      <div class="logs_header-ops">
        <a-button type="link" size="small" @click="copyLogTextContent(logText)">
          <template #icon>
            <copy-outlined />
          </template>
          <span class="copy-button">{{ $t('common.copy') }}</span>
        </a-button>
      </div>
    </div>
    <div class="logs_info">
      <pre id="logs" ref="logsInfoRef">{{ logText }}</pre>
    </div>
  </div>
</template>

<style scoped lang="scss">
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
      scroll-behavior: smooth;
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
