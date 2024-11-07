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
  import { toRefs } from 'vue'
  import AutoFrom from './auto-from.vue'

  interface Props {
    open: boolean
    title?: string
  }

  interface Emits {
    (event: 'update:open', value: boolean): void
    (event: 'update:loading', value: boolean): void
    (event: 'onOk'): void
    (event: 'onTest'): void
    (event: 'onCancel'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    title: '',
    open: false
  })

  const { open, title } = toRefs(props)

  const emits = defineEmits<Emits>()

  const handleOk = () => {
    emits('onOk')
    emits('update:open', false)
  }

  const handleTest = () => {
    emits('onTest')
  }

  const handleCancel = () => {
    emits('onCancel')
    emits('update:open', false)
  }
</script>

<template>
  <div class="update-llm-config">
    <a-modal
      :open="open"
      :width="480"
      :title="$t(title)"
      :mask-closable="false"
      :destroy-on-close="true"
      @ok="handleOk"
      @cancel="handleCancel"
    >
      <auto-from />
      <template #footer>
        <footer>
          <a-space size="middle">
            <a-button type="primary" @click="handleTest">
              {{ $t('llmConfig.availability') }}
            </a-button>
          </a-space>
          <a-space size="middle">
            <a-button @click="handleCancel">
              {{ $t('common.cancel') }}
            </a-button>
            <a-button type="primary" @click="handleOk">
              {{ $t('common.confirm') }}
            </a-button>
          </a-space>
        </footer>
      </template>
    </a-modal>
  </div>
</template>

<style lang="scss" scoped>
  footer {
    width: 100%;
    display: flex;
    justify-content: space-between;
  }
</style>
