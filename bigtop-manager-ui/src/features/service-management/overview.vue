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
  import { getServiceMetricsInfo } from '@/api/metrics'

  import { CommonStatus } from '@/enums/state'
  import { formatFromByte } from '@/utils/storage.ts'
  import { isEmpty } from '@/utils/tools'
  import { STATUS_COLOR, TIME_RANGES, POLLING_INTERVAL } from '@/utils/constant'

  import { useTabStore } from '@/store/tab-state'

  import CategoryChart from '@/features/metric/category-chart.vue'

  import type { ServiceVO } from '@/api/service/types'
  import type { ServiceMetricItem, ServiceMetrics, ServiceMetricType, TimeRangeType } from '@/api/metrics/types'

  type RouteParams = { id: number; serviceId: number }

  type BaseConfigType = Partial<Record<keyof ServiceVO, string>>

  type UnitMapType = Record<Lowercase<ServiceMetricType>, string | ((value: number) => string)>

  const UNIT_MAP: UnitMapType = {
    number: '',
    percent: '%',
    byte: (val) => formatFromByte(val, 0),
    millisecond: 'ms',
    bps: 'B/s',
    nps: 'N/s'
  }

  const { t } = useI18n()
  const route = useRoute()
  const tabStore = useTabStore()
  const attrs = useAttrs() as Partial<ServiceVO>

  const isRunning = ref(false)
  const interval = ref<TimeRangeType>('5m')
  const chartData = ref<Partial<ServiceMetrics>>({})

  const noChartData = computed(() => Object.values(chartData.value).length === 0)
  const serviceKeys = computed(() => Object.keys(baseConfig.value) as (keyof ServiceVO)[])

  const payload = computed(() => {
    const { id: clusterId, serviceId } = route.params as unknown as RouteParams
    return { clusterId, serviceId }
  })

  const baseConfig = computed(
    (): BaseConfigType => ({
      status: t('overview.service_status'),
      displayName: t('overview.service_name'),
      version: t('overview.service_version'),
      stack: t('common.stack'),
      restartFlag: t('service.required_restart'),
      metrics: t('overview.metrics'),
      kerberos: t('overview.kerberos')
    })
  )

  const getChartFormatter = (chart: ServiceMetricItem) => {
    const unit = UNIT_MAP[chart.valueType]
    const valueWithUnit = (val: any) => (typeof unit === 'function' ? unit(val as number) : `${val} ${unit}`)
    return {
      tooltip: (val: any) => `${isEmpty(val) ? '--' : valueWithUnit(val)}`,
      yAxis: valueWithUnit
    }
  }

  const shouldRunMetrics = () => {
    const currTab = tabStore.getActiveTab(route.path) ?? '1'
    const clusterId = payload.value.clusterId ?? 0
    return clusterId != 0 && currTab === '1'
  }

  const getServiceMetrics = async () => {
    if (isRunning.value) {
      return
    }

    isRunning.value = true

    try {
      const { serviceId: id } = payload.value
      const data = await getServiceMetricsInfo({ id }, { interval: interval.value })
      chartData.value = { ...data }
    } catch (error) {
      console.log('Failed to fetch service metrics:', error)
    } finally {
      isRunning.value = false
    }
  }

  const { pause, resume } = useIntervalFn(getServiceMetrics, POLLING_INTERVAL, { immediate: true })

  const handleTimeRange = (time: TimeRangeType) => {
    if (interval.value === time) return
    interval.value = time
    if (!shouldRunMetrics()) return
    getServiceMetrics()
    restartMetrics()
  }

  const restartMetrics = () => {
    pause()
    resume()
  }

  onActivated(() => {
    if (!shouldRunMetrics()) return
    getServiceMetrics()
    resume()
  })

  onDeactivated(() => {
    pause()
  })
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
                          :color="CommonStatus[STATUS_COLOR[attrs[key]!]]"
                        >
                          <status-dot :color="CommonStatus[STATUS_COLOR[attrs[key]!]]" />
                          {{ attrs[key] && t(`common.${STATUS_COLOR[attrs[key]]}`) }}
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
              v-for="time in TIME_RANGES"
              :key="time"
              tabindex="0"
              class="time-range"
              :class="{ 'time-range-activated': interval === time }"
              @click="handleTimeRange(time)"
            >
              {{ time }}
            </div>
          </a-space>
        </div>
        <template v-if="noChartData">
          <div class="box-empty">
            <a-empty :image="Empty.PRESENTED_IMAGE_SIMPLE" />
          </div>
        </template>
        <a-row v-else class="box-content">
          <a-col v-for="(chart, index) in chartData.charts" :key="index" :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <div class="chart-item-wrp">
              <category-chart
                :chart-id="`chart${index}`"
                :x-axis-data="chartData?.timestamps"
                :data="chart"
                :title="chart.title"
                :formatter="getChartFormatter(chart)"
              />
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
