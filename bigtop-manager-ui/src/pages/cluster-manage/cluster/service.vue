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
  import { Empty } from 'ant-design-vue'
  import { useServiceStore } from '@/store/service'
  import { CommonStatus, CommonStatusTexts } from '@/enums/state'
  import { useJobProgress } from '@/store/job-progress'
  import { usePngImage } from '@/utils/tools'

  import type { GroupItem } from '@/components/common/button-group/types'
  import type { FilterFormItem } from '@/components/common/form-filter/types'
  import type { ServiceListParams, ServiceStatusType, ServiceVO } from '@/api/service/types'
  import type { ClusterVO } from '@/api/cluster/types'
  import type { Command, CommandRequest } from '@/api/command/types'

  type GroupItemActionType = keyof typeof Command | 'More'

  const { t } = useI18n()
  const attrs = useAttrs() as ClusterVO
  const router = useRouter()
  const jobProgressStore = useJobProgress()
  const serviceStore = useServiceStore()
  const { services, loading } = toRefs(serviceStore)

  const statusColors = shallowRef<Record<ServiceStatusType, keyof typeof CommonStatusTexts>>({
    1: 'healthy',
    2: 'unhealthy',
    3: 'unknown'
  })

  const actionGroups = shallowRef<GroupItem<GroupItemActionType>[]>([
    {
      action: 'Start',
      icon: 'start',
      clickEvent: (item, payload) => {
        handleServiceOperate(item!.action!, payload)
      }
    },
    {
      action: 'Stop',
      icon: 'stop',
      clickEvent: (item, payload) => {
        handleServiceOperate(item!.action!, payload)
      }
    },
    {
      action: 'Restart',
      icon: 'restart',
      clickEvent: (item, payload) => {
        handleServiceOperate(item!.action!, payload)
      }
    },
    {
      action: 'More',
      icon: 'more-line',
      clickEvent: (item, payload) => {
        handleServiceOperate(item!.action!, payload)
      }
    }
  ])

  const filterFormItems = computed((): FilterFormItem[] => [
    {
      type: 'search',
      key: 'name',
      label: t('service.name')
    },
    {
      type: 'status',
      key: 'restartFlag',
      label: t('service.required_restart'),
      options: [
        { label: t('common.required'), value: true },
        { label: t('common.not_required'), value: false }
      ]
    },
    {
      type: 'status',
      key: 'status',
      label: t('common.status'),
      options: [
        { label: t('common.healthy'), value: 1 },
        { label: t('common.unhealthy'), value: 2 },
        { label: t('common.unknown'), value: 3 }
      ]
    }
  ])

  const handleServiceOperate = (command: GroupItemActionType, service: ServiceVO) => {
    if (command !== 'More') {
      const execCommandParams = {
        command: command,
        clusterId: attrs.id,
        commandLevel: 'service',
        serviceCommands: [{ serviceName: service.name, installed: true }]
      } as CommandRequest
      jobProgressStore.processCommand(execCommandParams, getServices, { displayName: service.displayName })
    }
  }

  const getServices = (filters?: ServiceListParams) => {
    if (attrs.id != undefined) {
      serviceStore.getServices(attrs.id, filters)
    }
  }

  const viewServiceDetail = (payload: ServiceVO) => {
    router.push({
      name: 'ServiceDetail',
      params: { serviceId: payload.id }
    })
  }

  onActivated(() => {
    getServices()
  })
</script>

<template>
  <a-spin :spinning="loading" class="service">
    <form-filter :filter-items="filterFormItems" @filter="getServices" />
    <a-empty v-if="services.length == 0" style="width: 100%" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
    <div v-else class="service-item-wrp">
      <a-card
        v-for="item in services"
        :key="item.id"
        :hoverable="true"
        class="service-item"
        @click="viewServiceDetail(item)"
      >
        <div class="header">
          <div class="header-base-wrp">
            <a-avatar v-if="item.name" :src="usePngImage(item.name.toLowerCase())" :size="42" class="header-icon" />
            <div class="header-base-title">
              <span>{{ `${item.displayName}` }}</span>
              <span class="small-gray">{{ item.version }}</span>
            </div>
            <div class="header-base-status">
              <a-tag :color="CommonStatus[statusColors[item.status]]">
                <div class="header-base-status-inner">
                  <status-dot :color="CommonStatus[statusColors[item.status]]" />
                  <span class="small">{{ t(`common.${statusColors[item.status]}`) }}</span>
                </div>
              </a-tag>
            </div>
          </div>
          <div class="header-restart-status">
            <span class="small-gray">{{ `${t('common.restart')}` }}</span>
            <status-dot :color="item.restartFlag ? 'error' : 'success'" />
            <span class="small">{{ `${item.restartFlag ? t('common.required') : t('common.not_required')}` }}</span>
          </div>
        </div>
        <div class="item-content" @click.stop>
          <button-group :auto="true" :space="0" :payload="item" :groups="actionGroups">
            <template #icon="{ item: groupItem }">
              <svg-icon :name="groupItem.icon || ''" />
            </template>
          </button-group>
        </div>
      </a-card>
    </div>
  </a-spin>
</template>

<style lang="scss" scoped>
  .small {
    font-size: 12px;
  }

  .small-gray {
    line-height: 12px;
    font-size: 12px;
    color: rgba(0, 0, 0, 0.45);
  }

  .service-item-wrp {
    display: flex;
    flex-wrap: wrap;
    gap: 19px;
  }

  .service-item {
    width: 280px;
    height: 150px;
    border-radius: 8px;
    @include flexbox($direction: column);
    :deep(.ant-card-body) {
      padding: 0;
      height: 100%;
      @include flexbox($direction: column);
    }
  }

  .item-content {
    flex: 1;
    @include flexbox($align: center);
    padding-inline: 8px;
  }

  .header {
    padding: $space-md;
    border-bottom: 1px solid $color-border-secondary;
    &-base-wrp {
      @include flexbox($gap: $space-sm);
      margin-bottom: 10px;
    }
    &-base-title {
      @include flexbox($direction: column);
    }
    &-base-status {
      flex: 1;
      text-align: end;
      &-inner {
        @include flexbox($align: center, $gap: 4px);
      }
      .ant-tag {
        margin-inline-end: 0;
      }
    }
    &-restart-status {
      @include flexbox($align: center, $gap: 6px);
      .dot {
        margin-top: 2px;
      }
    }
    &-icon {
      flex-shrink: 0;
      border-radius: 0;
    }
    :deep(.ant-avatar) {
      img {
        object-fit: contain !important;
      }
    }
  }
</style>
