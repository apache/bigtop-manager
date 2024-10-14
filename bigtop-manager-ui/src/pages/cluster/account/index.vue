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
  import { storeToRefs } from 'pinia'
  import { useServiceStore } from '@/store/service'

  const serviceStore = useServiceStore()
  const { installedServices, loadingServices } = storeToRefs(serviceStore)

  const serviceColumns = [
    {
      title: 'service.service_name',
      dataIndex: 'displayName',
      align: 'center'
    },
    {
      title: 'service.service_user',
      dataIndex: 'serviceUser',
      align: 'center'
    },
    {
      title: 'service.service_group',
      dataIndex: 'serviceGroup',
      align: 'center'
    }
  ]
</script>

<template>
  <div>
    <a-page-header
      class="account-page-header"
      :title="$t('service.service_account')"
    >
    </a-page-header>
    <br />
    <a-table
      :data-source="installedServices"
      :columns="serviceColumns"
      :loading="loadingServices"
      :pagination="false"
    >
      <template #headerCell="{ column }">
        <span>{{ $t(column.title) }}</span>
      </template>
    </a-table>
  </div>
</template>

<style scoped lang="less">
  .account-page-header {
    border: 1px solid rgb(235, 237, 240);
  }
</style>
