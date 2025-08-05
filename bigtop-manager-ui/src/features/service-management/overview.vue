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
  import { CommonStatus, CommonStatusTexts } from '@/enums/state'

  import GaugeChart from '@/features/metric/gauge-chart.vue'
  import CategoryChart from '@/features/metric/category-chart.vue'

  import { Empty } from 'ant-design-vue'
  import type { ServiceVO, ServiceStatusType } from '@/api/service/types'

  type TimeRangeText = '1m' | '15m' | '30m' | '1h' | '6h' | '30h'
  type TimeRangeItem = {
    text: TimeRangeText
    time: string
  }

  const { t } = useI18n()
  const attrs = useAttrs() as unknown as Required<ServiceVO> & { clusterId: number }
  const currTimeRange = ref<TimeRangeText>('15m')
  const chartData = ref({
    chart1: [],
    chart2: [],
    chart3: [],
    chart4: []
  })
  const statusColors = shallowRef<Record<ServiceStatusType, keyof typeof CommonStatusTexts>>({
    1: 'healthy',
    2: 'unhealthy',
    3: 'unknown'
  })
  const serviceKeys = computed(() => Object.keys(baseConfig.value) as (keyof ServiceVO)[])
  const noChartData = computed(() => Object.values(chartData.value).every((v) => v.length === 0))
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
  const baseConfig = computed((): Partial<Record<keyof ServiceVO, string>> => {
    return {
      status: t('overview.service_status'),
      displayName: t('overview.service_name'),
      version: t('overview.service_version'),
      stack: t('common.stack'),
      restartFlag: t('service.required_restart'),
      metrics: t('overview.metrics'),
      kerberos: t('overview.kerberos')
    }
  })

  const handleTimeRange = (time: TimeRangeItem) => {
    currTimeRange.value = time.text
  }
</script>

<template>
  <div class="dashboard">
    <a-row :gutter="[50, 16]" :wrap="true">
      <a-col :xs="24" :sm="24" :md="24" :lg="10" :xl="7" style="display: flex; flex-direction: column; gap: 24px">
        <div class="base-info">
          <div class="box-title">
            <a-typography-text strong :content="t('overview.basic_info')" />
          </div>
          <div>
            <a-descriptions layout="vertical" bordered>
              <a-descriptions-item>
                <template #label>
                  <div class="desc-sub-label">
                    <a-typography-text strong :content="t('overview.detail')" />
                  </div>
                </template>
                <div class="desc-sub-item-wrp">
                  <div class="desc-sub-item">
                    <template v-for="key in serviceKeys" :key="key">
                      <div class="desc-sub-item-desc">
                        <a-typography-text
                          class="desc-sub-item-desc-column"
                          type="secondary"
                          :content="baseConfig[`${key}`]"
                        />
                        <a-tag
                          v-if="key === 'status'"
                          class="reset-tag"
                          :color="CommonStatus[statusColors[attrs[key]]]"
                        >
                          <status-dot :color="CommonStatus[statusColors[attrs[key]]]" />
                          {{ attrs[key] && t(`common.${statusColors[attrs[key]]}`) }}
                        </a-tag>
                        <a-typography-text
                          v-else-if="key === 'stack'"
                          class="desc-sub-item-desc-column"
                          :content="attrs[key]?.toLowerCase()"
                        />
                        <a-typography-text
                          v-else-if="key === 'restartFlag'"
                          class="desc-sub-item-desc-column"
                          :content="attrs[key] ? t('common.yes') : t('common.no')"
                        />
                        <a-typography-text
                          v-else-if="['kerberos', 'metrics'].includes(key)"
                          class="desc-sub-item-desc-column"
                          :content="t('common.disabled')"
                        />
                        <a-typography-text v-else class="desc-sub-item-desc-column" :content="`${attrs[key] ?? ''}`" />
                      </div>
                    </template>
                  </div>
                </div>
              </a-descriptions-item>
            </a-descriptions>
          </div>
        </div>
      </a-col>
      <a-col :xs="24" :sm="24" :md="24" :lg="14" :xl="17">
        <div class="box-title">
          <a-typography-text strong :content="t('overview.chart')" />
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
        <template v-if="noChartData">
          <div class="box-empty">
            <a-empty :image="Empty.PRESENTED_IMAGE_SIMPLE" />
          </div>
        </template>
        <a-row v-else class="box-content">
          <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <div class="chart-item-wrp">
              <gauge-chart chart-id="chart1" :title="t('overview.memory_usage')" />
            </div>
          </a-col>
          <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <div class="chart-item-wrp">
              <gauge-chart chart-id="chart2" :title="t('overview.cpu_usage')" />
            </div>
          </a-col>
          <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <div class="chart-item-wrp">
              <category-chart chart-id="chart4" :title="t('overview.cpu_usage')" />
            </div>
          </a-col>
          <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <div class="chart-item-wrp">
              <category-chart chart-id="chart3" :title="t('overview.memory_usage')" />
            </div>
          </a-col>
        </a-row>
      </a-col>
    </a-row>
  </div>
</template>

<style lang="scss" scoped>
  :deep(.ant-avatar) {
    border-radius: 4px;

    img {
      object-fit: contain !important;
    }
  }

  .box {
    &-title {
      @include flexbox($justify: space-between);
      margin-bottom: 20px;
    }

    &-content {
      border-radius: 8px;
      overflow: visible;
      box-sizing: border-box;
      border: 1px solid $color-border;
    }

    &-empty {
      @include flexbox($justify: center, $align: center);
      min-height: 200px;
      border-radius: 8px;
      box-sizing: border-box;
      border: 1px solid $color-border;
    }
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

  .dashboard {
    :deep(.ant-descriptions-view) {
      overflow: hidden;
      border-color: $color-border;

      .ant-descriptions-item-label {
        padding: 0;
      }

      .ant-descriptions-item-content {
        padding: 0;
      }
    }

    .desc-sub-item-wrp {
      @include flexbox($gap: $space-md);
      padding: $space-md;

      :deep(.ant-tag) {
        width: fit-content;
        @include flexbox($align: center, $gap: 4px);
      }
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
