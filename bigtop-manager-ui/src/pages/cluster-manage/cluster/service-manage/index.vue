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
  import { computed, ref } from 'vue'
  import { useI18n } from 'vue-i18n'
  // import { execCommand } from '@/api/command'
  // import { Command } from '@/api/command/types'
  // import { CommonStatus, CommonStatusTexts } from '@/enums/state'
  import { useRoute } from 'vue-router'
  import Overview from './overview.vue'
  import Components from './components.vue'
  import Configs from './configs.vue'
  import type { TabItem } from '@/components/common/main-card/types'
  import type { GroupItem } from '@/components/common/button-group/types'

  interface ServieceInfo {
    cluster: string
    id: number
    service: string
    serviceId: number
  }

  const { t } = useI18n()
  const route = useRoute()
  const loading = ref(false)
  const activeKey = ref('1')
  const currServiceInfo = computed(() => route.params as unknown as ServieceInfo)
  const tabs = computed((): TabItem[] => [
    {
      key: '1',
      title: t('common.overview')
    },
    {
      key: '2',
      title: t('common.host')
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
    const components = [Overview, Components, Configs]
    return components[parseInt(activeKey.value) - 1]
  })

  const dropdownMenuClick: GroupItem['dropdownMenuClickEvent'] = async ({ key }) => {
    console.log('key :>> ', key)
    // try {
    //   await execCommand({
    //     command: key as keyof typeof Command,
    //     clusterId: currServiceInfo.value.id,
    //     commandLevel: 'cluster'
    //   })
    // } catch (error) {
    //   console.log('error :>> ', error)
    // }
  }
</script>

<template>
  <a-spin :spinning="loading">
    <header-card
      :title="`${$route.params.service}`"
      :avatar="`${$route.params.service}`.toLowerCase()"
      desc="test"
      :action-groups="actionGroup"
    />
    <main-card v-model:active-key="activeKey" :tabs="tabs">
      <template #tab-item>
        <keep-alive>
          <component :is="getCompName" v-bind="currServiceInfo"></component>
        </keep-alive>
      </template>
    </main-card>
  </a-spin>
</template>

<style lang="scss" scoped></style>
