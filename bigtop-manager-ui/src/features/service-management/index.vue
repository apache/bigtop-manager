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
  import { useServiceStore } from '@/store/service'
  import { useJobProgress } from '@/store/job-progress'
  import { Command } from '@/api/command/types'

  import Overview from './overview.vue'
  import Components from './components.vue'
  import Configs from './configs.vue'

  import type { TabItem } from '@/components/base/main-card/types'
  import type { GroupItem } from '@/components/common/button-group/types'
  import type { ServiceVO } from '@/api/service/types'

  interface RouteParams {
    id: number
    serviceId: number
  }

  const { t } = useI18n()
  const route = useRoute()
  const router = useRouter()
  const serviceStore = useServiceStore()
  const jobProgressStore = useJobProgress()
  const { loading, serviceMap } = storeToRefs(serviceStore)

  const activeKey = ref('1')
  const serviceDetail = shallowRef<ServiceVO>()

  const routeParams = computed(() => route.params as unknown as RouteParams)
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
      text: t('common.operation'),
      dropdownMenu: [
        { action: 'Start', text: t('common.start', [t('common.service')]) },
        { action: 'Restart', text: t('common.restart', [t('common.service')]) },
        { action: 'Stop', text: t('common.stop', [t('common.service')]) },
        { action: 'Remove', text: t('common.remove', [t('common.service')]), divider: true, danger: true }
      ],
      dropdownMenuClickEvent: (info) => dropdownMenuClick && dropdownMenuClick(info)
    }
  ])

  const getCompName = computed(() => {
    const components = [Overview, Components, Configs]
    return components[parseInt(activeKey.value) - 1]
  })

  const dropdownMenuClick: GroupItem['dropdownMenuClickEvent'] = async ({ key }) => {
    const { id: clusterId, serviceId } = routeParams.value
    const service = serviceMap.value[clusterId].filter((service) => Number(serviceId) === service.id)[0]

    if (key === 'Remove') {
      serviceStore.removeService(service, clusterId, () => {
        router.replace({ path: `/cluster-manage/clusters/${clusterId}` })
      })
    } else {
      jobProgressStore.processCommand(
        {
          command: key as keyof typeof Command,
          clusterId,
          commandLevel: 'service',
          serviceCommands: [{ serviceName: service.name!, installed: true }]
        },
        getServiceDetail,
        {
          displayName: service.displayName
        }
      )
    }
  }

  const getServiceDetail = async () => {
    try {
      loading.value = true
      const { id: clusterId, serviceId } = routeParams.value
      serviceDetail.value = await serviceStore.getServiceDetail(clusterId, serviceId)
    } catch (error) {
      console.log('error :>> ', error)
    } finally {
      loading.value = false
    }
  }

  provide('getServiceDetail', getServiceDetail)

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
          <component :is="getCompName" v-bind="{ ...serviceDetail, clusterId: routeParams.id }"></component>
        </keep-alive>
      </template>
    </main-card>
  </a-spin>
</template>

<style lang="scss" scoped></style>
