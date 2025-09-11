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
  import { message } from 'ant-design-vue'

  import { getHost } from '@/api/host'
  import { CommonStatus, CommonStatusTexts } from '@/enums/state'

  import Overview from './overview.vue'

  import type { GroupItem } from '@/components/common/button-group/types'
  import type { HostStatusType, HostVO } from '@/api/host/types'

  const { t } = useI18n()
  const route = useRoute()
  const hostInfo = shallowRef<HostVO>({})
  const loading = ref(false)
  const statusColors = shallowRef<Record<HostStatusType, keyof typeof CommonStatusTexts>>({
    1: 'healthy',
    2: 'unhealthy',
    3: 'unknown'
  })
  const actionGroup = computed<GroupItem[]>(() => [
    {
      shape: 'default',
      type: 'default',
      text: t('common.more_operations'),
      dropdownMenu: [
        {
          action: 'Start',
          text: t('common.start', [t('common.all')])
        },
        {
          action: 'Restart',
          text: t('common.restart', [t('common.all')])
        },
        {
          action: 'Stop',
          text: t('common.stop', [t('common.all')])
        }
      ],
      dropdownMenuClickEvent: (info) => dropdownMenuClick && dropdownMenuClick(info)
    }
  ])

  const dropdownMenuClick: GroupItem['dropdownMenuClickEvent'] = async () => {
    message.error(t('common.feature_not_supported'))
  }

  const setupHostInfo = async () => {
    try {
      loading.value = true
      const hostId = route.query.hostId
      const clusterId = route.query.clusterId
      const data = await getHost({ id: Number(hostId) })
      hostInfo.value = {
        ...data,
        clusterId
      }
    } catch (error) {
      console.log(error)
    } finally {
      loading.value = false
    }
  }

  onMounted(() => {
    setupHostInfo()
  })
</script>

<template>
  <a-spin :spinning="loading">
    <header-card
      :title="hostInfo.hostname"
      avatar="host"
      :status="CommonStatus[statusColors[hostInfo.status as HostStatusType]]"
      :desc="hostInfo.desc"
      :action-groups="actionGroup"
    />
    <main-card>
      <overview :host-info="hostInfo" />
    </main-card>
  </a-spin>
</template>

<style lang="scss" scoped></style>
