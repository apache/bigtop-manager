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
  import { computed, onMounted, provide, ref, shallowRef } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { storeToRefs } from 'pinia'
  // import { execCommand } from '@/api/command'
  // import { Command } from '@/api/command/types'
  // import { CommonStatus, CommonStatusTexts } from '@/enums/state'
  import { useServiceStore } from '@/store/service'
  import { useRoute } from 'vue-router'
  import Overview from './overview.vue'
  import Components from './components.vue'
  import Configs from './configs.vue'
  import type { TabItem } from '@/components/common/main-card/types'
  import type { GroupItem } from '@/components/common/button-group/types'
  import type { ServiceVO } from '@/api/service/types'

  interface ServieceInfo {
    cluster: string
    id: number
    service: string
    serviceId: number
  }

  const { t } = useI18n()
  const route = useRoute()
  const serviceStore = useServiceStore()
  const { loading } = storeToRefs(serviceStore)
  const activeKey = ref('1')
  const serviceDetail = shallowRef<ServiceVO>()
  const routeParams = computed(() => route.params as unknown as ServieceInfo)
  const tabs = computed((): TabItem[] => [
    {
      key: '1',
      title: t('common.overview')
    },
    {
      key: '2',
      title: t('common.component')
    },
    {
      key: '3',
      title: t('common.configs')
    }
  ])
  const actionGroup = computed<GroupItem[]>(() => [
    {
      shape: 'default',
      type: 'primary',
      text: t('common.action'),
      dropdownMenu: [
        {
          action: 'Start',
          text: t('common.start', [t('common.service')])
        },
        {
          action: 'Restart',
          text: t('common.restart', [t('common.service')])
        },
        {
          action: 'Stop',
          text: t('common.stop', [t('common.service')])
        }
      ]
    }
  ])

  const getCompName = computed(() => {
    const components = [Overview, Components, Configs]
    return components[parseInt(activeKey.value) - 1]
  })

  const getServiceDetail = async () => {
    const { id: clusterId, serviceId } = routeParams.value
    serviceDetail.value = await serviceStore.getServiceDetail(clusterId, serviceId)
  }

  provide('getServiceDetail', getServiceDetail)
  provide('service', { routeParams })

  onMounted(() => {
    getServiceDetail()
  })
</script>

<template>
  <a-spin :spinning="loading">
    <header-card
      :title="serviceDetail?.displayName || serviceDetail?.name"
      :avatar="serviceDetail?.name"
      :desc="serviceDetail?.desc"
      :action-groups="actionGroup"
    />
    <main-card v-model:active-key="activeKey" :tabs="tabs">
      <template #tab-item>
        <keep-alive>
          <component :is="getCompName" v-bind="serviceDetail"></component>
        </keep-alive>
      </template>
    </main-card>
  </a-spin>
</template>

<style lang="scss" scoped></style>
