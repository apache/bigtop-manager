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
  import { computed, onMounted, ref, shallowRef } from 'vue'
  import { getServices, StatusColors, StatusTexts, type ServiceItem } from './components/mock'
  import { usePngImage } from '@/utils/tools'
  import { useI18n } from 'vue-i18n'
  import FilterForm from '@/components/common/filter-form/index.vue'
  import type { GroupItem } from '@/components/common/button-group/types'
  import type { FilterFormItem } from '@/components/common/filter-form/types'

  const { t } = useI18n()
  const data = ref<ServiceItem[]>([])
  const actionGroups = shallowRef<GroupItem[]>([
    {
      action: 'start',
      icon: 'start',
      clickEvent: (item) => {
        console.log('item :>> ', item?.action)
      }
    },
    {
      action: 'stop',
      icon: 'stop',
      clickEvent: (item) => {
        console.log('item :>> ', item?.action)
      }
    },
    {
      action: 'restart',
      icon: 'restart',
      clickEvent: (item) => {
        console.log('item :>> ', item?.action)
      }
    },
    {
      action: 'more',
      icon: 'more_line',
      clickEvent: (item) => {
        console.log('item :>> ', item?.action)
      }
    }
  ])

  const filterFormItems = computed((): FilterFormItem[] => [
    {
      type: 'search',
      key: 'serviceName',
      label: '服务名'
    },
    {
      type: 'status',
      key: 'restart',
      label: '需要重启',
      options: [
        {
          label: '需重启',
          value: 1
        },
        {
          label: '无需重启',
          value: 2
        }
      ]
    },
    {
      type: 'status',
      key: 'status',
      label: '状态',
      options: [
        {
          label: t(`common.${StatusTexts.success}`),
          value: StatusTexts.success
        },
        {
          label: t(`common.${StatusTexts.error}`),
          value: StatusTexts.error
        },
        {
          label: t(`common.${StatusTexts.unknow}`),
          value: StatusTexts.unknow
        }
      ]
    }
  ])

  const onFilter = (filters: any) => {
    console.log('filters :>> ', filters)
  }

  onMounted(() => {
    data.value = getServices()
  })
</script>

<template>
  <div class="service">
    <filter-form :filter-items="filterFormItems" @filter="onFilter" />
    <a-card v-for="item in data" :key="item.key" :hoverable="true" class="service-item">
      <div class="header">
        <div class="header-base-wrp">
          <a-avatar
            v-if="item.serviceName"
            :src="usePngImage(item.serviceName.toLowerCase())"
            :size="42"
            class="header-icon"
          />
          <div class="header-base-title">
            <span>{{ `${item.serviceName}` }}</span>
            <span class="small-gray">{{ item.version }}</span>
          </div>
          <div class="header-base-status">
            <a-tag :color="StatusColors[item.status]">
              <div class="header-base-status-inner">
                <status-dot :color="StatusColors[item.status]" />
                <span class="small">{{ $t(`common.${StatusTexts[item.status]}`) }}</span>
              </div>
            </a-tag>
          </div>
        </div>
        <div class="header-restart-status">
          <span class="small-gray">{{ `${$t('common.restart')}` }}</span>
          <status-dot :color="StatusColors[item.status]" />
          <span class="small">{{ `${item.restart ? $t('common.required') : $t('common.not_required')}` }}</span>
        </div>
      </div>
      <div class="item-content">
        <button-group :auto="true" :space="0" :groups="actionGroups">
          <template #icon="{ item: groupItem }">
            <svg-icon :name="groupItem.icon || ''" />
          </template>
        </button-group>
      </div>
    </a-card>
  </div>
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
