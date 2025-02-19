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
  import { computed, onMounted, ref } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { useClusterStore } from '@/store/cluster'
  import { storeToRefs } from 'pinia'
  import { execCommand } from '@/api/command'
  import Overview from './overview.vue'
  import Service from './service.vue'
  import Host from './host.vue'
  import User from './user.vue'
  import Job from '@/components/job/index.vue'
  import type { TabItem } from '@/components/common/main-card/types'
  import type { GroupItem } from '@/components/common/button-group/types'
  import { Command } from '@/api/command/types'

  const { t } = useI18n()
  const clusterStore = useClusterStore()
  const { currCluster, loading } = storeToRefs(clusterStore)
  const activeKey = ref('1')
  const tabs = computed((): TabItem[] => [
    {
      key: '1',
      title: t('common.overview')
    },
    {
      key: '2',
      title: t('common.service')
    },
    {
      key: '3',
      title: t('common.host')
    },
    {
      key: '4',
      title: t('common.user')
    },
    {
      key: '5',
      title: t('common.job')
    }
  ])
  const actionGroup = computed<GroupItem[]>(() => [
    {
      shape: 'default',
      type: 'primary',
      text: t('common.add', [t('common.service')]),
      clickEvent: () => addService && addService()
    },
    {
      shape: 'default',
      type: 'default',
      text: t('common.more_operations'),
      dropdownMenu: [
        {
          action: 'Start',
          text: t('common.start', [t('common.cluster')])
        },
        {
          action: 'Restart',
          text: t('common.restart', [t('common.cluster')])
        },
        {
          action: 'Stop',
          text: t('common.stop', [t('common.cluster')])
        }
      ],
      dropdownMenuClickEvent: (info) => dropdownMenuClick && dropdownMenuClick(info)
    }
  ])

  const getCompName = computed(() => {
    const componnts = [Overview, Service, Host, User, Job]
    return componnts[parseInt(activeKey.value) - 1]
  })

  const dropdownMenuClick: GroupItem['dropdownMenuClickEvent'] = async ({ key }) => {
    try {
      await execCommand({
        command: key as keyof typeof Command,
        clusterId: currCluster.value.id,
        commandLevel: 'cluster'
      })
      clusterStore.loadClusters()
      clusterStore.getClusterDetail()
    } catch (error) {
      console.log('error :>> ', error)
    }
  }

  const addService: GroupItem['clickEvent'] = () => {
    console.log('add :>> ')
  }

  onMounted(() => {
    clusterStore.getClusterDetail()
  })
</script>

<template>
  <a-spin :spinning="loading">
    <header-card
      :title="currCluster.displayName"
      avatar="cluster"
      :desc="currCluster.desc"
      :action-groups="actionGroup"
    />
    <main-card v-model:active-key="activeKey" :tabs="tabs">
      <template #tab-item>
        <keep-alive>
          <component :is="getCompName" v-bind="currCluster"></component>
        </keep-alive>
      </template>
    </main-card>
  </a-spin>
</template>

<style lang="scss" scoped></style>
