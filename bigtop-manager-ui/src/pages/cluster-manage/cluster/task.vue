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
  import { type TaskListItem, getTaskList } from './components/mock'
  import { ref } from 'vue'
  import type { TableColumnType } from 'ant-design-vue'
  import useBaseTable from '@/composables/use-base-table'

  const columns: TableColumnType[] = [
    {
      title: '#',
      width: '48px',
      key: 'index',
      customRender: ({ index }) => {
        return `${index + 1}`
      }
    },
    {
      title: '名称',
      key: 'name',
      dataIndex: 'name',
      ellipsis: true
    },
    {
      title: '状态',
      key: 'status',
      dataIndex: 'status'
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      ellipsis: true
    },
    {
      title: '更新时间',
      dataIndex: 'updateTime',
      ellipsis: true
    }
  ]
  const data = ref<TaskListItem[]>(getTaskList(50))
  const { columnsProp, dataSource, loading, paginationProps, onChange } = useBaseTable({
    columns,
    rows: data.value
  })
</script>

<template>
  <div class="task">
    <header>
      <div class="host-title">{{ $t('task.task_list') }}</div>
    </header>
    <a-table
      :loading="loading"
      :data-source="dataSource"
      :columns="columnsProp"
      :pagination="paginationProps"
      @change="onChange"
    >
      <template #bodyCell="{ record, column }">
        <template v-if="column.key === 'name'">
          <a-typography-link underline> {{ record.name }} </a-typography-link>
        </template>
        <template v-if="column.key === 'status'">
          <a-progress :percent="record.progress" :status="record.status" />
        </template>
      </template>
    </a-table>
  </div>
</template>

<style lang="scss" scoped>
  .task {
    header {
      margin-bottom: $space-md;
    }
  }
</style>
