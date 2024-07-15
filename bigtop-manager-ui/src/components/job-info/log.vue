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
  import { copyText } from '../../utils/tools'
  import { message } from 'ant-design-vue'
  import { ref, watch, onBeforeUnmount } from 'vue'
  import { CopyOutlined } from '@ant-design/icons-vue'

  interface Props {
    logMeta: string
  }

  const props = withDefaults(defineProps<Props>(), {
    logMeta: ''
  })

  const { t } = useI18n()
  const logsInfoRef = ref<HTMLElement | null>(null)
  const logText = ref<string | null>(null)

  watch(
    () => props.logMeta,
    (val) => {
      logText.value = val
        .split('\n\n')
        .map((s) => {
          return s.substring(5)
        })
        .join('\n')
    }
  )

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

  onBeforeUnmount(() => {})
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
      <pre id="logs">{{ logText }}</pre>
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
