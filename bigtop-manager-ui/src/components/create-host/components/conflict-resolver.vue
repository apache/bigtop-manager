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
  import { computed, shallowRef } from 'vue'
  import { useI18n } from 'vue-i18n'

  import type { HostReq } from '@/api/command/types'
  import type { TableColumnType } from 'ant-design-vue'

  type conflictItem = HostReq & { strategy: 'override' | 'keep' }

  defineProps<{ parsedHosts: conflictItem[]; duplicateHostnames: string[] }>()
  defineEmits<{ (event: 'update:parsedHosts', val: conflictItem[]): void }>()

  const { t } = useI18n()
  const options = shallowRef(['keep', 'override'])
  const columns = computed((): TableColumnType[] => [
    {
      title: t('host.hostname'),
      dataIndex: 'hostname',
      key: 'hostname',
      ellipsis: true
    },
    {
      key: 'operation',
      width: '200px',
      fixed: 'right'
    }
  ])
</script>

<template>
  <div>
    <a-table :data-source="$props.parsedHosts" :columns="columns">
      <template #headerCell="{ column }">
        <template v-if="column === 'operation'">
          <a-dropdown :get-popup-container="(trigger) => trigger.parentNode">
            <a class="ant-dropdown-link" @click.prevent> {{ t('common.operation') }} </a>
            <template #overlay>
              <a-menu>
                <a-menu-item key="keep">keep</a-menu-item>
                <a-menu-item key="override">override</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </template>
      </template>
      <template #bodyCell="{ record, column }">
        <template v-if="column.key === 'operation'">
          <a-radio-group
            v-model:value="record.strategy"
            style="display: flex"
            :disabled="!$props.duplicateHostnames.includes(record.hostname)"
          >
            <a-radio v-for="opt in options" :key="opt" :value="opt">{{ opt }}</a-radio>
          </a-radio-group>
        </template>
      </template>
    </a-table>
  </div>
</template>

<style scoped></style>
