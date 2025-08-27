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
  import { parseHostNamesAsPatternExpression } from '@/utils/transform'
  import ConflictResolver from './conflict-resolver.vue'

  import type { HostReq } from '@/api/command/types'

  type Data = HostReq & { hostname?: string }
  type conflictItem = HostReq & { strategy: 'override' | 'keep' }

  interface ParsedRes {
    parsedData: Data
    confirmStatus: boolean
    duplicateHosts?: conflictItem[]
  }

  const props = defineProps<{ data: Data; isPublic: boolean | undefined }>()
  const emits = defineEmits<{ (event: 'parsed', value: ParsedRes): void }>()

  const { t } = useI18n()
  const open = ref(false)
  const showConflictList = ref(false)
  const parsedHostNames = ref<string[]>([])
  const duplicateHostnames = ref<string[]>([])
  const parsedHosts = ref<conflictItem[]>([])
  const cacheCurrentHosts = shallowRef<Data[]>([])

  const parsed = (currentHosts: Data[]) => {
    resetState()
    open.value = true
    cacheCurrentHosts.value = currentHosts
    parsedHostNames.value = parseHostNamesAsPatternExpression(props.data.hostname || '')
    parsedHosts.value = parsedHostNames.value.map((v) => ({ hostname: v, strategy: 'override' }))
  }

  const getDuplicateHostnames = () => {
    const currentHostNamse = cacheCurrentHosts.value.map((v) => v.hostname)
    return parsedHostNames.value.filter((item) => currentHostNamse.includes(item))
  }

  const handleOk = () => {
    const duplicates = getDuplicateHostnames()
    duplicateHostnames.value = duplicates

    const hasConflicts = duplicates.length > 0

    if (!hasConflicts || showConflictList.value) {
      emits('parsed', {
        parsedData: { ...props.data, hostnames: parsedHostNames.value },
        confirmStatus: true,
        duplicateHosts: hasConflicts ? parsedHosts.value : []
      })
      resetState()
    }

    if (hasConflicts) {
      showConflictList.value = true
    }
  }

  const handleCancel = () => {
    emits('parsed', { parsedData: { ...props.data }, confirmStatus: false, duplicateHosts: [] })
    resetState()
  }

  const resetState = () => {
    open.value = false
    showConflictList.value = false
    parsedHostNames.value = []
    duplicateHostnames.value = []
  }

  defineExpose({
    parsed
  })
</script>

<template>
  <a-modal v-model:open="open" :centered="true" width="680px" :z-index="2000" @ok="handleOk" @cancel="handleCancel">
    <template #title>
      <div class="icon">
        <span class="icon-wrp">
          <svg-icon :name="showConflictList ? 'warn' : 'success'"></svg-icon>
        </span>
        <span class="title">
          {{
            showConflictList
              ? t('cluster.duplicate_hostname', [`${duplicateHostnames.length}`])
              : t('cluster.show_hosts_resolved')
          }}
        </span>
      </div>
    </template>
    <div v-if="!showConflictList" class="content">
      <div v-for="(name, index) in parsedHostNames" :key="index" class="item">{{ name }}</div>
    </div>
    <div v-else>
      <conflict-resolver v-model:parsed-hosts="parsedHosts" :duplicate-hostnames="duplicateHostnames" />
    </div>
    <template #footer>
      <footer>
        <a-space size="middle">
          <a-button @click="handleCancel">
            {{ t('common.cancel') }}
          </a-button>
          <a-button v-if="showConflictList" @click="() => (showConflictList = false)">
            {{ t('common.prev') }}
          </a-button>
          <a-button type="primary" @click="handleOk">
            {{ t('common.confirm') }}
          </a-button>
        </a-space>
      </footer>
    </template>
  </a-modal>
</template>

<style lang="scss" scoped>
  .content {
    margin-inline-start: 2.125rem;
    max-height: 31.25rem;
    overflow: auto;
  }

  .title {
    font-size: 1rem;
    font-weight: 600;
  }

  .icon {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .icon-wrp {
    font-size: 1.375rem;
    font-weight: 600;
    color: green;
  }

  .item {
    margin: 0.625rem;
  }

  footer {
    width: 100%;
    @include flexbox($justify: flex-end);
  }
</style>
