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

<script lang="ts" setup>
  import { StageVO } from '@/api/job/types.ts'
  import { watch } from 'vue'
  import CustomProgress from './custom-progress.vue'
  import useBaseTable from '@/composables/useBaseTable'
  import type { TableColumnType } from 'ant-design-vue'

  interface StageProps {
    stages: StageVO[]
    columns: TableColumnType[]
  }

  const props = defineProps<StageProps>()
  const baseTable = useBaseTable<StageVO>(props.columns, props.stages)
  const { dataSource, columnsProp, loading, paginateProp, onChange } = baseTable
  watch(
    () => props.stages,
    (val) => {
      if (val.length == 0) {
        loading.value = true
      } else {
        loading.value = false
      }
    },
    {
      immediate: true,
      deep: true
    }
  )
  const emits = defineEmits(['clickStage'])
  const clickStage = (record: StageVO) => {
    emits('clickStage', record)
  }
</script>

<template>
  <div class="stage-info">
    <a-table
      :scroll="{ y: 500 }"
      :loading="loading"
      :columns="columnsProp"
      :data-source="dataSource"
      :pagination="paginateProp"
      @change="onChange"
    >
      <template #headerCell="{ column }">
        <span>{{ $t(column.title) }}</span>
      </template>
      <template #bodyCell="{ column, text, record }">
        <template v-if="column.dataIndex === 'name'">
          <a @click="clickStage(record)">
            {{ text }}
          </a>
        </template>
        <template v-if="column.dataIndex === 'state'">
          <custom-progress
            :key="record.id"
            :state="text"
            :progress-data="record.tasks"
          />
        </template>
      </template>
    </a-table>
  </div>
</template>

<style lang="scss" scoped></style>
