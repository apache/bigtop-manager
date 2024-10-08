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
  import { TaskVO } from '@/api/job/types.ts'
  import { State } from '@/enums/state'
  import useBaseTable from '@/composables/use-base-table'
  import type { TableColumnType } from 'ant-design-vue'

  import {
    CheckCircleTwoTone,
    MinusCircleTwoTone,
    CloseCircleTwoTone,
    LoadingOutlined
  } from '@ant-design/icons-vue'

  interface TaskProps {
    tasks: TaskVO[]
    columns: TableColumnType[]
  }

  const props = defineProps<TaskProps>()
  const baseTable = useBaseTable<TaskVO>({
    columns: props.columns,
    rows: props.tasks
  })
  const { dataSource, columnsProp, loading, paginationProps, onChange } =
    baseTable

  const emits = defineEmits(['clickTask'])
  const clickTask = (record: TaskVO) => {
    emits('clickTask', record)
  }
</script>

<template>
  <div class="task-info">
    <a-table
      :scroll="{ y: 500 }"
      :loading="loading"
      :columns="columnsProp"
      :data-source="dataSource"
      :pagination="paginationProps"
      @change="onChange"
    >
      <template #headerCell="{ column }">
        <span>{{ $t(column.title) }}</span>
      </template>
      <template #bodyCell="{ column, text, record }">
        <template v-if="column.dataIndex === 'name'">
          <a @click="clickTask(record)">
            {{ text }}
          </a>
        </template>
        <template v-if="column.dataIndex === 'state'">
          <check-circle-two-tone
            v-if="text === 'Successful'"
            :two-tone-color="State[text as keyof typeof State]"
          />
          <close-circle-two-tone
            v-else-if="text === 'Failed'"
            :two-tone-color="State[text as keyof typeof State]"
          />
          <minus-circle-two-tone
            v-else-if="text === 'Canceled'"
            :two-tone-color="State[text as keyof typeof State]"
          />
          <loading-outlined v-else />
        </template>
      </template>
    </a-table>
  </div>
</template>

<style scoped></style>
