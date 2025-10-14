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
  import { getHost, restartAgent, startAgent, stopAgent } from '@/api/host'
  import { message } from 'ant-design-vue'
  import { CommonStatus } from '@/enums/state'
  import { STATUS_COLOR } from '@/utils/constant'

  import Overview from './overview.vue'

  import type { GroupItem } from '@/components/common/button-group/types'
  import type { HostStatusType, HostVO } from '@/api/host/types'

  const { t } = useI18n()
  const { confirmModal } = useModal()

  const route = useRoute()
  const spinning = ref(false)
  const hostInfo = shallowRef<HostVO>({})
  const apiMap = shallowRef({
    start: startAgent,
    restart: restartAgent,
    stop: stopAgent
  })

  const actionGroup = computed<GroupItem[]>(() => [
    {
      shape: 'default',
      type: 'default',
      text: t('common.more_operations'),
      dropdownMenu: [
        {
          action: 'start',
          text: t('common.start', [t('host.agent')])
        },
        {
          action: 'restart',
          text: t('common.restart', [t('host.agent')])
        },
        {
          action: 'stop',
          text: t('common.stop', [t('host.agent')])
        }
      ],
      dropdownMenuClickEvent: ({ key }) => {
        const action = t(`common.${key.toString()}`).toLowerCase()
        const tipText = t('host.operate_agent', { action, target: hostInfo.value.hostname })
        confirmModal({
          tipText,
          async onOk() {
            await handleOperateAgent(key as string)
          }
        })
      }
    }
  ])

  const handleOperateAgent = async (action: string) => {
    try {
      const res = await apiMap.value[`${action}`]({ id: hostInfo.value.id })
      if (res) {
        message.success(t('common.operate_success'))
      }
    } finally {
      setupHostInfo()
    }
  }

  const setupHostInfo = async () => {
    try {
      const { hostId, clusterId } = route.query
      spinning.value = true
      const data = await getHost({ id: Number(hostId) })
      hostInfo.value = {
        ...data,
        clusterId
      }
    } finally {
      spinning.value = false
    }
  }

  onMounted(() => {
    setupHostInfo()
  })
</script>

<template>
  <a-spin :spinning="spinning">
    <header-card
      :title="hostInfo.hostname"
      avatar="host"
      :status="CommonStatus[STATUS_COLOR[hostInfo.status as HostStatusType]]"
      :desc="hostInfo.desc"
      :action-groups="actionGroup"
    />
    <main-card>
      <overview :host-info="hostInfo" />
    </main-card>
  </a-spin>
</template>

<style lang="scss" scoped></style>
