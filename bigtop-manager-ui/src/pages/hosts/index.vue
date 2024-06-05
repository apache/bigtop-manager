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
  import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
  import { message } from 'ant-design-vue'
  import { storeToRefs } from 'pinia'
  import {
    CaretRightFilled,
    CheckCircleTwoTone,
    CloseCircleTwoTone,
    MinusCircleTwoTone,
    StopFilled
  } from '@ant-design/icons-vue'
  import { useHostStore } from '@/store/host'
  import { DEFAULT_PAGE_SIZE } from '@/utils/constant.ts'

  const hostStore = useHostStore()
  const { hosts, loading } = storeToRefs(hostStore)
  const currentPage = ref<number>(1)
  const pagination = computed(() => ({
    total: hosts.value.length,
    current: currentPage.value,
    pageSize: DEFAULT_PAGE_SIZE
  }))

  const selectedRowKeys = ref<string[]>([])

  const hostColumns = [
    {
      title: 'hosts.hostname',
      dataIndex: 'hostname',
      align: 'center'
    },
    {
      title: 'hosts.cluster_name',
      dataIndex: 'clusterName',
      align: 'center'
    },
    {
      title: 'common.os',
      dataIndex: 'os',
      align: 'center'
    },
    {
      title: 'common.arch',
      dataIndex: 'arch',
      align: 'center'
    },
    {
      title: 'hosts.ipv4',
      dataIndex: 'ipv4',
      align: 'center'
    },
    {
      title: 'hosts.cores',
      dataIndex: 'availableProcessors',
      align: 'center'
    },
    {
      title: 'hosts.ram',
      dataIndex: 'totalMemorySize',
      align: 'center'
    },
    {
      title: 'hosts.disk',
      dataIndex: 'totalDisk',
      align: 'center'
    },
    {
      title: 'common.status',
      dataIndex: 'state',
      align: 'center'
    }
  ]

  const displayKeys = (selectedRowKeys: string[]) => {
    message.info('This is a normal message' + selectedRowKeys)
  }

  onMounted(async () => {
    hostStore.resumeIntervalFn()
  })

  onBeforeUnmount(() => {
    hostStore.pauseIntervalFn()
  })
</script>

<template>
  <div>
    <a-page-header class="host-page-header" :title="$t('common.host')">
      <template #extra>
        <span class="host-selected-span">
          <template v-if="selectedRowKeys.length > 0">
            {{ $t('hosts.host_selected', [selectedRowKeys.length]) }}
          </template>
        </span>

        <a-button type="primary" @click="displayKeys(selectedRowKeys)">
          {{ $t('hosts.add') }}
        </a-button>

        <a-dropdown :trigger="['click']">
          <template #overlay>
            <a-menu>
              <a-menu-item @click="displayKeys(selectedRowKeys)">
                <CaretRightFilled />
                {{ $t('hosts.host_restart') }}
              </a-menu-item>
              <a-menu-item @click="displayKeys(selectedRowKeys)">
                <StopFilled />
                {{ $t('hosts.host_stop') }}
              </a-menu-item>
            </a-menu>
          </template>
          <a-button type="primary" :disabled="selectedRowKeys.length === 0">
            {{ $t('common.action') }}
          </a-button>
        </a-dropdown>
      </template>
    </a-page-header>
    <br />

    <a-table
      :row-key="hostColumns[0].dataIndex"
      :columns="hostColumns"
      :data-source="hosts"
      :loading="loading"
      :pagination="pagination"
      :row-selection="{
        selectedRowKeys,
        onChange: (value: string[]) => (selectedRowKeys = value)
      }"
      @change="(v: any) => (currentPage = v.current)"
    >
      <template #headerCell="{ column }">
        <span>{{ $t(column.title) }}</span>
      </template>
      <template #bodyCell="{ column, text }">
        <template v-if="column.dataIndex === 'state'">
          <CheckCircleTwoTone
            v-if="text === 'INSTALLED'"
            two-tone-color="#52c41a"
          />
          <MinusCircleTwoTone
            v-else-if="text === 'MAINTAINED'"
            two-tone-color="orange"
          />
          <CloseCircleTwoTone v-else two-tone-color="red" />
        </template>
        <template v-if="column.dataIndex === 'hostname'">
          <router-link to="/dashboard"> {{ text }} </router-link>
        </template>
      </template>
    </a-table>
  </div>
</template>

<style scoped lang="scss">
  .host-page-header {
    border: 1px solid rgb(235, 237, 240);

    .host-selected-span {
      margin-left: 8px;
    }
  }
</style>
