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
  import { useClusterStore } from '@/store/cluster'
  import { CommonStatus, CommonStatusTexts } from '@/enums/state'
  import { useJobProgress } from '@/store/job-progress'

  import Overview from './overview.vue'
  import Service from './service.vue'
  import Host from './host.vue'
  import User from './user.vue'
  import Job from '@/features/job/index.vue'

  import type { Command } from '@/api/command/types'
  import type { TabItem } from '@/components/base/main-card/types'
  import type { GroupItem } from '@/components/common/button-group/types'
  import type { ClusterStatusType } from '@/api/cluster/types'

  const { t } = useI18n()
  const router = useRouter()
  const route = useRoute()
  const jobProgressStore = useJobProgress()
  const clusterStore = useClusterStore()
  const { loading, currCluster } = storeToRefs(clusterStore)

  const { activeTab } = useTabState(route.path, '1')

  const statusColors = shallowRef<Record<ClusterStatusType, keyof typeof CommonStatusTexts>>({
    1: 'healthy',
    2: 'unhealthy',
    3: 'unknown'
  })

  /**
   * Determines the component to render based on the active tab.
   */
  const getCompName = computed(() => [Overview, Service, Host, User, Job][parseInt(activeTab.value) - 1])

  const tabs = computed((): TabItem[] => [
    { key: '1', title: t('common.overview') },
    { key: '2', title: t('common.service') },
    { key: '3', title: t('common.host') },
    { key: '4', title: t('common.user') },
    { key: '5', title: t('common.job') }
  ])

  const actionGroup = computed<GroupItem[]>(() => [
    {
      shape: 'default',
      type: 'primary',
      text: t('common.add', [t('common.service')]),
      clickEvent: () => addService!()
    },
    {
      shape: 'default',
      type: 'default',
      text: t('common.more_operations'),
      dropdownMenu: [
        { action: 'Start', text: t('common.start', [t('common.cluster')]) },
        { action: 'Restart', text: t('common.restart', [t('common.cluster')]) },
        { action: 'Stop', text: t('common.stop', [t('common.cluster')]) }
      ],
      dropdownMenuClickEvent: (info) => dropdownMenuClick!(info)
    }
  ])

  /**
   * Handles dropdown menu click events for cluster operations.
   */
  const dropdownMenuClick: GroupItem['dropdownMenuClickEvent'] = ({ key }) => {
    try {
      jobProgressStore.processCommand(
        {
          command: key as keyof typeof Command,
          clusterId: currCluster.value.id,
          commandLevel: 'cluster'
        },
        async () => {
          await clusterStore.loadClusters()
          await clusterStore.getClusterDetail(currCluster.value.id!)
        },
        { displayName: currCluster.value.displayName }
      )
    } catch (error) {
      console.error('Error processing command:', error)
    }
  }

  const addService: GroupItem['clickEvent'] = () => {
    router.push({ name: 'CreateService', params: { id: currCluster.value.id, creationMode: 'internal' } })
  }
</script>

<template>
  <a-spin :spinning="loading">
    <header-card
      :title="currCluster.displayName"
      avatar="cluster"
      :status="CommonStatus[statusColors[currCluster.status as ClusterStatusType]]"
      :desc="currCluster.desc"
      :action-groups="actionGroup"
    />
    <main-card v-model:active-key="activeTab" :tabs="tabs">
      <template #tab-item>
        <keep-alive>
          <component :is="getCompName" :key="activeTab" v-bind="currCluster" v-model:payload="currCluster"></component>
        </keep-alive>
      </template>
    </main-card>
  </a-spin>
</template>

<style lang="scss" scoped></style>
