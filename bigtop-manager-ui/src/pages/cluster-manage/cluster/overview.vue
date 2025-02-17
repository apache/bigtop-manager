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
  import GaugeChart from './components/gauge-chart.vue'
  import CategoryChart from './components/category-chart.vue'
  import type { ClusterVO } from '@/api/cluster/types'
  import type { MenuProps } from 'ant-design-vue'
  import { useI18n } from 'vue-i18n'

  type TimeRangeText = '1m' | '15m' | '30m' | '1h' | '6h' | '30h'
  type TimeRangeItem = {
    text: TimeRangeText
    time: string
  }

  const { t } = useI18n()
  const currTimeRange = ref<TimeRangeText>('15m')
  const clusterDetail = ref<ClusterVO>({
    id: 0,
    name: 'string',
    displayName: 'string',
    desc: 'string',
    type: 0,
    userGroup: 'string',
    rootDir: 'string',
    status: 0,
    createUser: 'string',
    totalHost: 0,
    totalService: 0,
    totalProcessor: 0,
    totalMemory: 0,
    totalDisk: 0
  })

  const timeRanges = computed((): TimeRangeItem[] => [
    {
      text: '1m',
      time: ''
    },
    {
      text: '15m',
      time: ''
    },
    {
      text: '30m',
      time: ''
    },
    {
      text: '1h',
      time: ''
    },
    {
      text: '6h',
      time: ''
    },
    {
      text: '30h',
      time: ''
    }
  ])

  const baseConfig = computed((): Partial<Record<keyof ClusterVO, string>> => {
    return {
      status: t('overview.cluster_status'),
      displayName: t('overview.cluster_name'),
      desc: t('overview.cluster_desc'),
      totalHost: t('overview.host_count'),
      totalService: t('overview.service_count'),
      totalMemory: t('overview.memory'),
      totalProcessor: t('overview.core_count'),
      totalDisk: t('overview.disk_size'),
      createUser: t('overview.creator')
    }
  })
  const detailKeys = computed(() => Object.keys(baseConfig.value) as (keyof ClusterVO)[])

  const serviceStack = [
    {
      stackId: 1,
      stackName: 'Bigtop Stack',
      stackVersion: 'bigtop-3.3.0',
      services: [
        {
          id: 1,
          name: 'HDFS'
        },
        {
          id: 2,
          name: 'Hive'
        }
      ]
    },
    {
      stackId: 2,
      stackName: 'Extra Stack',
      stackVersion: 'extra-1.0.0',
      services: [
        {
          id: 1,
          name: 'Doris'
        },
        {
          id: 2,
          name: 'SeaTunnel'
        }
      ]
    }
  ]

  const serviceOperates = computed(() => [
    {
      action: 'start',
      text: t('service.start')
    },
    {
      action: 'restart',
      text: t('service.restart')
    },
    {
      action: 'stop',
      text: t('service.stop')
    }
  ])

  const handleServiceOperate: MenuProps['onClick'] = (item) => {
    console.log('item :>> ', item.key)
  }

  const handleTimeRange = (time: TimeRangeItem) => {
    currTimeRange.value = time.text
  }
</script>

<template>
  <div class="dashboard">
    <a-row :gutter="[50, 16]" :wrap="true">
      <a-col :xs="24" :sm="24" :md="24" :lg="10" :xl="7" style="display: flex; flex-direction: column; gap: 24px">
        <!-- base info -->
        <a-descriptions layout="vertical" bordered>
          <template #title>
            <a-typography-text strong :content="$t('overview.basic_info')" />
          </template>
          <a-descriptions-item>
            <template #label>
              <div class="desc-sub-label">
                <a-typography-text strong :content="$t('overview.detail')" />
              </div>
            </template>
            <div class="desc-sub-item-wrp">
              <div class="desc-sub-item">
                <template v-for="base in detailKeys" :key="base">
                  <div class="desc-sub-item-desc">
                    <a-typography-text class="desc-sub-item-desc-column" type="secondary" :content="baseConfig[base]" />
                    <a-typography-text class="desc-sub-item-desc-column" :content="clusterDetail[base]" />
                  </div>
                </template>
              </div>
            </div>
          </a-descriptions-item>
        </a-descriptions>
        <!-- service info -->
        <a-descriptions layout="vertical" bordered :column="1">
          <template #title>
            <a-typography-text strong :content="$t('overview.service_info')" />
          </template>
          <template v-for="stack in serviceStack" :key="stack.stackId">
            <a-descriptions-item>
              <template #label>
                <div class="desc-sub-label">
                  <a-typography-text strong :content="stack.stackName" />
                  <a-typography-text type="secondary" :content="stack.stackVersion" />
                </div>
              </template>
              <div v-for="service in stack.services" :key="service.id" class="service-item">
                <a-avatar shape="square" :size="16" />
                <a-typography-text :content="service.name" />

                <a-dropdown :trigger="['click']">
                  <a-button type="text" shape="circle" size="small">
                    <svg-icon name="more" style="margin: 0" />
                  </a-button>
                  <template #overlay>
                    <a-menu @click="handleServiceOperate">
                      <a-menu-item v-for="operate in serviceOperates" :key="operate.action">
                        <span>{{ operate.text }}</span>
                      </a-menu-item>
                    </a-menu>
                  </template>
                </a-dropdown>
              </div>
            </a-descriptions-item>
          </template>
        </a-descriptions>
      </a-col>
      <a-col :xs="24" :sm="24" :md="24" :lg="14" :xl="17">
        <div class="chart-title">
          <a-typography-text strong :content="$t('overview.chart')" />
          <a-space :size="12">
            <div
              v-for="time in timeRanges"
              :key="time.text"
              tabindex="0"
              class="time-range"
              :class="{ 'time-range-activated': currTimeRange === time.text }"
              @click="handleTimeRange(time)"
            >
              {{ time.text }}
            </div>
          </a-space>
        </div>
        <a-row class="chart-wrp">
          <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <div class="chart-item-wrp">
              <gauge-chart chart-id="chart1" :title="$t('overview.memory_usage')" />
            </div>
          </a-col>
          <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <div class="chart-item-wrp">
              <gauge-chart chart-id="chart2" :title="$t('overview.cpu_usage')" />
            </div>
          </a-col>
          <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <div class="chart-item-wrp">
              <category-chart chart-id="chart4" :title="$t('overview.cpu_usage')" />
            </div>
          </a-col>
          <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <div class="chart-item-wrp">
              <category-chart chart-id="chart3" :title="$t('overview.memory_usage')" />
            </div>
          </a-col>
        </a-row>
      </a-col>
    </a-row>
  </div>
</template>

<style lang="scss" scoped>
  .dashboard {
    .service-item {
      display: grid;
      grid-template-columns: auto 1fr auto;
      gap: $space-md;
      align-items: center;
      padding: $space-md;
      border-bottom: 1px solid #e5e5e5;
    }

    .chart-title {
      display: flex;
      justify-content: space-between;
      margin-bottom: 20px;
    }

    .time-range {
      padding-inline: 6px;
      border-radius: 4px;
      text-align: center;
      cursor: pointer;
      user-select: none;
      outline: none;
      transition: background-color 0.3s;

      &:hover {
        color: $color-primary-text-hover;
      }
      &-activated {
        color: $color-primary-text;
      }
    }

    .chart-wrp {
      border-radius: 8px;
      overflow: hidden;
      box-sizing: border-box;
      border: 1px solid $color-border;
    }

    .chart-item-wrp {
      border: 1px solid $color-border;
      margin-right: -1px;
      margin-bottom: -1px;

      &:first-child {
        border-left: 0;
        border-top: 0;
      }

      &:not(:last-child) {
        border-right: 0;
      }

      &:nth-child(n + 3):not(:nth-child(4)) {
        border-bottom: 0;
        border-left: 0;
      }
    }

    :deep(.ant-descriptions-view) {
      overflow: hidden;
      .ant-descriptions-item-label {
        padding: 0;
      }
      .ant-descriptions-item-content {
        padding: 0;
      }
    }

    .desc-sub-item-wrp {
      display: flex;
      gap: $space-md;
      padding: $space-md;
    }

    .desc-sub-item {
      display: grid;
      grid-template-columns: max-content 1fr;
      gap: 16px;
      &-desc {
        display: contents;
        &-column {
          text-align: left;
        }
      }
    }

    .desc-sub-label {
      @include flexbox($align: center, $gap: $space-sm);
      background-color: $color-border-secondary;
      padding: $space-md;

      :deep(.a-typography-text:last-child) {
        font-size: 12px;
      }
    }
  }
</style>
