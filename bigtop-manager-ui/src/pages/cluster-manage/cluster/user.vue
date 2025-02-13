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
  import { type UserListItem, getUserList } from './components/mock'
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
      title: '服务名',
      dataIndex: 'serviceName',
      width: '20%',
      ellipsis: true
    },
    {
      title: '用户名',
      dataIndex: 'userName',
      width: '15%',
      ellipsis: true
    },
    {
      title: '用户组',
      dataIndex: 'userGroup',
      width: '20%',
      ellipsis: true
    },
    {
      title: '描述',
      dataIndex: 'descrip',
      ellipsis: true
    }
  ]
  const data = ref<UserListItem[]>(getUserList(50))
  const { columnsProp, dataSource, loading, paginationProps, onChange } = useBaseTable({
    columns,
    rows: data.value
  })
</script>

<template>
  <div class="user">
    <header>
      <div class="header-title">{{ $t('user.user_list') }}</div>
    </header>
    <a-table
      :loading="loading"
      :data-source="dataSource"
      :columns="columnsProp"
      :pagination="paginationProps"
      @change="onChange"
    ></a-table>
  </div>
</template>

<style lang="scss" scoped></style>
