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
  import { useJobProgress } from '@/store/job-progress'

  import { CommonStatus } from '@/enums/state'
  import { usePngImage } from '@/utils/tools'
  import { STATUS_COLOR } from '@/utils/constant'

  import type { ServiceVO } from '@/api/service/types'
  import type { GroupItem } from '@/components/common/button-group/types'
  import type { FilterFormItem } from '@/components/common/form-filter/types'
  import type { Command, CommandRequest } from '@/api/command/types'
  import type { ClusterVO } from '@/api/cluster/types'

  type GroupItemActionType = keyof typeof Command | 'More'

  const props = defineProps<{ payload: ClusterVO }>()

  const { t } = useI18n()
  const router = useRouter()
  const jobProgressStore = useJobProgress()
  const serviceStore = useServiceStore()
  const { services, loading } = toRefs(serviceStore)

  const filterValue = ref({})

  const clusterId = computed(() => props.payload.id)
  const filterFormItems = computed((): FilterFormItem[] => [
    { type: 'search', key: 'name', label: t('service.name') },
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
        { label: t(`common.${STATUS_COLOR[1]}`), value: 1 },
        { label: t(`common.${STATUS_COLOR[2]}`), value: 2 },
        { label: t(`common.${STATUS_COLOR[3]}`), value: 3 }
      ]
    }
  ])
  const actionGroups = shallowRef<GroupItem<GroupItemActionType>[]>([
    {
      action: 'Start',
      icon: 'start',
      tip: t('common.start', [t('common.service')]),
      clickEvent: (item, args) => {
        infraAction(item!.action!, args)
      }
    },
    {
      action: 'Stop',
      icon: 'stop',
      tip: t('common.stop', [t('common.service')]),
      clickEvent: (item, args) => {
        infraAction(item!.action!, args)
      }
    },
    {
      action: 'Restart',
      icon: 'restart',
      tip: t('common.restart', [t('common.service')]),
      clickEvent: (item, args) => {
        infraAction(item!.action!, args)
      }
    },
    {
      action: 'More',
      icon: 'more-line',
      dropdownMenu: [
        {
          danger: true,
          action: 'remove',
          text: t('common.remove', [t('common.service')])
        }
      ],
      dropdownMenuClickEvent: (_item, payload) => {
        infraAction('Remove', payload)
      }
    }
  ])

  const infraAction = async (command: GroupItemActionType | 'Remove', service: ServiceVO) => {
    if (!['More', 'Remove'].includes(command)) {
      const { name: serviceName, displayName } = service
      const execCommandParams = {
        command,
        clusterId: clusterId.value,
        commandLevel: 'service',
        serviceCommands: [{ serviceName, installed: true }]
      } as CommandRequest
      jobProgressStore.processCommand(execCommandParams, getServices, { displayName })
    } else {
      serviceStore.removeService(service, clusterId.value!, getServices)
    }
  }

  const getServices = async () => {
    await serviceStore.getServices(clusterId.value!, filterValue.value)
  }

  const viewServiceDetail = (payload: ServiceVO) => {
    router.push({
      name: 'InfraServiceDetail',
      params: {
        id: clusterId.value,
        serviceId: payload.id
      }
    })
  }

  onActivated(async () => {
    await getServices()
  })
</script>

<template>
  <a-spin :spinning="loading" class="service">
    <div class="infra-body">
      <form-filter v-model:filter-value="filterValue" :filter-items="filterFormItems" @filter="getServices" />
      <a-empty v-if="services.length == 0" style="width: 100%" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
      <div v-else class="service-item-wrp">
        <a-card
          v-for="item in services"
          :key="item.name"
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
                <a-tag :color="CommonStatus[STATUS_COLOR[item.status]]">
                  <div class="header-base-status-inner">
                    <status-dot :color="CommonStatus[STATUS_COLOR[item.status]]" />
                    <span class="small">{{ t(`common.${STATUS_COLOR[item.status]}`) }}</span>
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
    </div>
  </a-spin>
</template>

<style scoped lang="scss">
  .small {
    font-size: 12px;
  }
  .small-gray {
    line-height: 12px;
    font-size: 12px;
    color: rgba(0, 0, 0, 0.45);
  }
  .infra-body {
    background-color: #fff;
    .service {
      display: flex;
      flex-wrap: wrap;
      gap: 16px;
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
      .item-content {
        flex: 1;
        @include flexbox($align: center);
        padding-inline: 8px;
      }
    }
  }
  :deep(.ant-dropdown-link) {
    margin-right: 10px;
  }

  .service-item-wrp {
    display: flex;
    flex-wrap: wrap;
    gap: 19px;
  }
</style>
