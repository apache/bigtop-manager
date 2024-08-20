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

  const emit = defineEmits(['onLogReceive', 'onLogComplete'])

  watch(logMeta, (val) => {
    logText.value = val
      .split('\n\n')
      .map((s) => {
        return s.substring(5)
      })
      .join('\n')
  })

  const getLogInfo = async (id: number) => {
    const { cancel, promise } = getLogs(clusterId.value, id, onLogReceive)
    canceler.value = cancel
    promise.then(onLogComplete)
    emit('onLogReceive', true)
  }

  const onLogReceive = ({ event }: AxiosProgressEvent) => {
    logMeta.value = event.target.responseText
    scrollToBottom(logsInfoRef.value)
  }

  const onLogComplete = (res: string | undefined) => {
    cancelSseConnect()
    emit('onLogComplete', res != undefined)
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
    emit('onLogReceive', false)
    emit('onLogComplete', false)
  })

  defineExpose({
    getLogInfo
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
    <div ref="logsInfoRef" class="logs_info">
      <div id="logs">
        <pre>{{ logText }}</pre>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
  .logs {
    height: 50vh;
    @include flexbox($direction: column);
    &_header {
      font-size: 16px;
      font-weight: 600;
      margin: 0 0 10px 0;
      @include flexbox($justify: space-between);

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
      & > div {
        height: 100%;
        margin: 0;
        padding: 16px 14px;
        box-sizing: border-box;
        border-color: #eee;
        box-sizing: border-box;
        pre {
          line-height: 20px;
          color: #444;
        }
      }
    }
  }
</style>
