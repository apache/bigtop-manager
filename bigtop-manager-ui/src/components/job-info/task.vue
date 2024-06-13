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
  import { computed, reactive, ref } from 'vue'
  import { PaginationConfig } from 'ant-design-vue/es/pagination/Pagination'
  import { TaskVO, State } from '@/api/job/types.ts'

  import {
    CheckCircleTwoTone,
    MinusCircleTwoTone,
    CloseCircleTwoTone,
    LoadingOutlined
  } from '@ant-design/icons-vue'

  interface TaskProps {
    tasks: TaskVO[]
    columns: any
  }
  const props = withDefaults(defineProps<TaskProps>(), {})

  const pagedList = ref(computed(() => props.tasks))

  const handlePageChange = (page: number) => {
    paginationProps.current = page
  }

  const handlePageSizeChange = (_current: number, size: number) => {
    paginationProps.pageSize = size
  }

  const paginationProps = reactive<PaginationConfig>({
    current: 1,
    pageSize: 10,
    size: 'small',
    showSizeChanger: true,
    pageSizeOptions: ['10', '20', '30', '40', '50'],
    total: pagedList.value.length,
    onChange: handlePageChange,
    onShowSizeChange: handlePageSizeChange
  })

  const emits = defineEmits(['clickTask'])
  const clickTask = (record: TaskVO) => {
    emits('clickTask', record)
  }
</script>

<template>
  <div class="task-info">
    <a-table
      :pagination="paginationProps"
      :data-source="pagedList"
      :columns="props.columns"
      :scroll="{ y: 500 }"
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
          <CheckCircleTwoTone
            v-if="text === 'Successful'"
            :two-tone-color="State[text as keyof typeof State]"
          />
          <CloseCircleTwoTone
            v-else-if="text === 'Failed'"
            :two-tone-color="State[text as keyof typeof State]"
          />
          <MinusCircleTwoTone
            v-else-if="text === 'Canceled'"
            :two-tone-color="State[text as keyof typeof State]"
          />
          <LoadingOutlined v-else />
        </template>
      </template>
    </a-table>
  </div>
</template>

<style scoped></style>
