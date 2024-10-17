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
  import { ref } from 'vue'
  import { storeToRefs } from 'pinia'
  import { CheckCircleTwoTone, CloseCircleTwoTone } from '@ant-design/icons-vue'
  import ClusterCreate from '@/components/cluster-create/index.vue'
  import { useServiceStore } from '@/store/service'

  const serviceStore = useServiceStore()
  const { mergedServices, loadingServices } = storeToRefs(serviceStore)

  const createWindowOpened = ref(false)

  const serviceColumns = [
    {
      title: 'service.service_name',
      dataIndex: 'displayName',
      align: 'center'
    },
    {
      title: 'common.version',
      dataIndex: 'serviceVersion',
      align: 'center'
    },
    {
      title: 'common.status',
      dataIndex: 'state',
      align: 'center'
    },
    {
      title: 'common.desc',
      dataIndex: 'serviceDesc',
      align: 'center'
    }
  ]
</script>

<template>
  <div>
    <a-card class="host-page-header" :title="$t('common.stack')">
      <template #extra>
        <a-button type="primary" @click="createWindowOpened = true">
          {{ $t('service.add') }}
        </a-button>
      </template>
    </a-card>
    <a-card>
      <a-table
        :columns="serviceColumns"
        :loading="loadingServices"
        :data-source="mergedServices"
        :pagination="false"
      >
        <template #headerCell="{ column }">
          <span>{{ $t(column.title) }}</span>
        </template>
        <template #bodyCell="{ column, text, record }">
          <template v-if="column.dataIndex === 'state'">
            <CheckCircleTwoTone
              v-if="record.installed"
              two-tone-color="#52c41a"
            />
            <CloseCircleTwoTone v-else two-tone-color="red" />
          </template>
          <template
            v-if="column.dataIndex === 'displayName' && record.installed"
          >
            <router-link :to="'/services/' + record.serviceName.toLowerCase()">
              {{ text }}
            </router-link>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>

  <cluster-create v-model:open="createWindowOpened" />
</template>

<style scoped lang="scss"></style>
