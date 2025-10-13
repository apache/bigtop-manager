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
  import { getUserListOfService } from '@/api/cluster'

  import type { TableColumnType } from 'ant-design-vue'
  import type { ServiceUserVO } from '@/api/cluster/types'

  const { t } = useI18n()
  const route = useRoute()
  const clusterId = ref(Number(route.params.id))
  const columns = computed((): TableColumnType[] => [
    {
      title: '#',
      width: '48px',
      key: 'index',
      customRender: ({ index }) => {
        return `${index + 1}`
      }
    },
    {
      title: t('service.name'),
      dataIndex: 'displayName',
      width: '20%',
      ellipsis: true
    },
    {
      title: t('user.username'),
      dataIndex: 'user',
      width: '15%',
      ellipsis: true
    },
    {
      title: t('user.user_group'),
      dataIndex: 'userGroup',
      width: '20%',
      ellipsis: true
    },
    {
      title: t('common.desc'),
      dataIndex: 'desc',
      ellipsis: true
    }
  ])

  const { dataSource, loading, filtersParams, paginationProps, onChange } = useBaseTable<ServiceUserVO>({
    columns: columns.value,
    rows: []
  })

  const loadUserListOfService = async () => {
    if (clusterId.value == undefined || !paginationProps.value) {
      loading.value = false
      return
    }
    try {
      const data = await getUserListOfService(clusterId.value, filtersParams.value)
      dataSource.value = data.content
      paginationProps.value.total = data.total
    } catch (error) {
      console.log('error :>> ', error)
    } finally {
      loading.value = false
    }
  }

  onActivated(() => {
    loadUserListOfService()
  })
</script>

<template>
  <div class="user">
    <header>
      <div class="header-title">{{ t('user.user_list') }}</div>
    </header>
    <a-table
      :loading="loading"
      :data-source="dataSource"
      :columns="columns"
      :pagination="paginationProps"
      @change="onChange"
    ></a-table>
  </div>
</template>

<style lang="scss" scoped></style>
