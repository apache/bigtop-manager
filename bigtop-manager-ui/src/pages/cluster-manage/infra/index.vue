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
  import { computed, shallowRef, onMounted, toRefs } from 'vue'
  import { usePngImage } from '@/utils/tools'
  import { useI18n } from 'vue-i18n'
  import { CommonStatus, CommonStatusTexts } from '@/enums/state'
  import { useServiceStore } from '@/store/service'
  import type { ServiceListParams, ServiceStatusType } from '@/api/service/types'
  import type { GroupItem } from '@/components/common/button-group/types'
  import type { FilterFormItem } from '@/components/common/filter-form/types'
  // import { execCommand } from '@/api/command';
  import type { Command } from '@/api/command/types'

  const { t } = useI18n()
  const serviceStore = useServiceStore()
  const { services, loading } = toRefs(serviceStore)

  const statusColors = shallowRef<Record<ServiceStatusType, keyof typeof CommonStatusTexts>>({
    1: 'healthy',
    2: 'unhealthy',
    3: 'unknown'
  })
  const filterFormItems = computed((): FilterFormItem[] => [
    { type: 'search', key: 'serviceName', label: t('service.name') },
    {
      type: 'status',
      key: 'restartFlag',
      label: t('service.required_restart'),
      options: [
        { label: t('common.required'), value: 1 },
        { label: t('common.not_required'), value: 2 }
      ]
    },
    {
      type: 'status',
      key: 'status',
      label: t('common.status'),
      options: [
        { label: t(`common.${statusColors.value[1]}`), value: 1 },
        { label: t(`common.${statusColors.value[2]}`), value: 2 },
        { label: t(`common.${statusColors.value[3]}`), value: 3 }
      ]
    }
  ])
  const actionGroups = shallowRef<GroupItem[]>([
    {
      action: 'start',
      icon: 'start',
      clickEvent: (item, args) => {
        console.log('item :>> ', item?.action)
        infraAction('Start', args.name)
      }
    },
    {
      action: 'stop',
      icon: 'stop',
      clickEvent: (item, args) => {
        console.log('item :>> ', item?.action)
        infraAction('Stop', args.name)
      }
    },
    {
      action: 'restart',
      icon: 'restart',
      clickEvent: (item, args) => {
        console.log('item :>> ', item?.action)
        infraAction('Restart', args.name)
      }
    },
    {
      action: 'more',
      icon: 'more_line',
      clickEvent: (item, args) => {
        console.log('item :>> ', item?.action)
        infraAction('More', args.name)
      }
    }
  ])

  const infraAction = async (command: keyof typeof Command, serviceName: string) => {
    console.log(command, serviceName)
    // await execCommand({
    //   command: command,
    //   clusterId: 0,
    //   commandLevel: "service",
    //   serviceCommands: [{ serviceName: serviceName }]
    // })
  }

  const getServices = async (filters?: ServiceListParams) => {
    await serviceStore.getServices(0, filters)
  }

  onMounted(async () => {
    await getServices()
  })
</script>

<template>
  <a-spin :spinning="loading" class="service">
    <div class="infra-header">
      <div>
        <div class="menu-title">{{ $t('menu.infra') }}</div>
        <div class="menu-info">{{ $t('infra.info') }}</div>
      </div>
      <a-button
        type="primary"
        @click="
          () => $router.push({ name: 'CreateInfraService', params: { id: 0, cluster: '', creationMode: 'public' } })
        "
      >
        {{ $t('infra.action') }}
      </a-button>
    </div>
    <div class="infra-body">
      <filter-form :filter-items="filterFormItems" @filter="getServices" />
      <a-empty v-if="services.length == 0" style="width: 100%" />
      <template v-else>
        <a-card v-for="item in services" :key="item.name" :hoverable="true" class="service-item">
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
                    <span class="small">{{ $t(`common.${statusColors[item.status]}`) }}</span>
                  </div>
                </a-tag>
              </div>
            </div>
            <div class="header-restart-status">
              <span class="small-gray">{{ `${$t('common.restart')}` }}</span>
              <status-dot :color="CommonStatus[statusColors[item.status]]" />
              <span class="small">{{ `${item.restartFlag ? $t('common.required') : $t('common.not_required')}` }}</span>
            </div>
          </div>
          <div class="item-content">
            <button-group :auto="true" :space="0" :args="item" :groups="actionGroups">
              <template #icon="{ item: groupItem }">
                <svg-icon :name="groupItem.icon || ''" />
              </template>
            </button-group>
          </div>
        </a-card>
      </template>
    </div>
  </a-spin>
</template>

<style scoped lang="scss">
  .infra-header {
    padding: 24px 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: #fff;
    .menu-title {
      font-size: 16px;
      font-weight: 500;
      line-height: 24px;
    }
    .menu-info {
      margin-top: 5px;
      color: #00000072;
      font-size: 14px;
      font-weight: 400;
      line-height: 22px;
    }
  }
  .small {
    font-size: 12px;
  }
  .small-gray {
    line-height: 12px;
    font-size: 12px;
    color: rgba(0, 0, 0, 0.45);
  }
  .infra-body {
    margin-top: 16px;
    padding: 16px;
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
</style>
