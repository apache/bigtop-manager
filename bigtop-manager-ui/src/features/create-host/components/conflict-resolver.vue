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
  import type { HostReq } from '@/api/command/types'
  import type { TableColumnType } from 'ant-design-vue'

  type conflictItem = HostReq & { strategy: 'override' | 'keep' }

  const props = defineProps<{ parsedHosts: conflictItem[]; duplicateHostnames: string[] }>()
  const emits = defineEmits<{ (event: 'update:parsedHosts', val: conflictItem[]): void }>()

  const options = ['keep', 'override']

  const { t } = useI18n()
  const columns = computed((): TableColumnType[] => [
    {
      title: t('host.hostname'),
      dataIndex: 'hostname',
      key: 'hostname',
      ellipsis: true
    },
    {
      title: t('common.operation'),
      key: 'operation',
      width: '200px',
      fixed: 'right'
    }
  ])

  const handleStrategyChange = (hostname: string, strategy: 'keep' | 'override') => {
    const updated = props.parsedHosts.map((item) => (item.hostname === hostname ? { ...item, strategy } : item))
    emits('update:parsedHosts', updated)
  }
</script>

<template>
  <div>
    <a-table
      :data-source="$props.parsedHosts.filter((v) => $props.duplicateHostnames.includes(v.hostname))"
      :columns="columns"
    >
      <template #bodyCell="{ record, column }">
        <template v-if="column.key === 'operation'">
          <a-radio-group
            :value="record.strategy"
            style="display: flex"
            @change="(e) => handleStrategyChange(record.hostname, e.target.value)"
          >
            <a-radio v-for="opt in options" :key="opt" :value="opt">{{ opt }}</a-radio>
          </a-radio-group>
        </template>
      </template>
    </a-table>
  </div>
</template>

<style scoped></style>
