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
  import { computed, onActivated, ref, shallowRef, useAttrs } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { storeToRefs } from 'pinia'
  import { useServiceStore } from '@/store/service'
  import { formatFromByte } from '@/utils/storage'
  import { usePngImage } from '@/utils/tools'
  import { CommonStatus, CommonStatusTexts } from '@/enums/state'
  import GaugeChart from './components/gauge-chart.vue'
  import CategoryChart from './components/category-chart.vue'
  import type { ClusterStatusType, ClusterVO } from '@/api/cluster/types'
  import { type MenuProps, Empty } from 'ant-design-vue'
  import type { ServiceListParams } from '@/api/service/types'
  import type { StackVO } from '@/api/stack/types'

  type TimeRangeText = '1m' | '15m' | '30m' | '1h' | '6h' | '30h'
  type TimeRangeItem = {
    text: TimeRangeText
    time: string
  }

  const { t } = useI18n()
  const attrs = useAttrs() as ClusterVO
  const serviceStore = useServiceStore()
  const currTimeRange = ref<TimeRangeText>('15m')
  const chartData = ref({
    chart1: [],
    chart2: [],
    chart3: [],
    chart4: []
  })
  const statusColors = shallowRef<Record<ClusterStatusType, keyof typeof CommonStatusTexts>>({
    1: 'healthy',
    2: 'unhealthy',
    3: 'unknown'
  })
  const { locateStackWithService, serviceNames } = storeToRefs(serviceStore)
  const clusterDetail = computed(() => ({
    ...attrs,
    totalMemory: formatFromByte(attrs.totalMemory as number),
    totalDisk: formatFromByte(attrs.totalDisk as number)
  }))
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
  const unitOfBaseConfig = computed((): Partial<Record<keyof ClusterVO, string>> => {
    return {
      totalHost: t('overview.unit_host'),
      totalService: t('overview.unit_service'),
      totalProcessor: t('overview.unit_core')
    }
  })
  const detailKeys = computed(() => Object.keys(baseConfig.value) as (keyof ClusterVO)[])
  const serviceOperates = computed(() => [
    {
      action: 'start',
      text: t('common.start', [t('common.service')])
    },
    {
      action: 'restart',
      text: t('common.restart', [t('common.service')])
    },
    {
      action: 'stop',
      text: t('common.stop', [t('common.service')])
    }
  ])

  const handleServiceOperate: MenuProps['onClick'] = (item) => {
    console.log('item :>> ', item.key)
  }

  const handleTimeRange = (time: TimeRangeItem) => {
    currTimeRange.value = time.text
  }

  const getServices = (filters?: ServiceListParams) => {
    attrs.id != undefined && serviceStore.getServices(attrs.id, filters)
  }

  const servicesFromCurrentCluster = (stack: StackVO) => {
    return stack.services.filter((v) => serviceNames.value.includes(v.name))
  }

  onActivated(() => {
    getServices()
  })
</script>

<template>
  <div class="dashboard">
    <a-row :gutter="[50, 16]" :wrap="true">
      <a-col :xs="24" :sm="24" :md="24" :lg="10" :xl="7" style="display: flex; flex-direction: column; gap: 24px">
        <div class="base-info">
          <div class="box-title">
            <a-typography-text strong :content="$t('overview.basic_info')" />
          </div>
          <div>
            <a-descriptions layout="vertical" bordered>
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
                        <a-typography-text
                          class="desc-sub-item-desc-column"
                          type="secondary"
                          :content="baseConfig[base]"
                        />
                        <a-tag
                          v-if="base === 'status'"
                          class="reset-tag"
                          :color="CommonStatus[statusColors[clusterDetail[base] as ClusterStatusType]]"
                        >
                          <status-dot :color="CommonStatus[statusColors[clusterDetail[base] as ClusterStatusType]]" />
                          {{
                            clusterDetail[base] &&
                            $t(`common.${statusColors[clusterDetail[base] as ClusterStatusType]}`)
                          }}
                        </a-tag>
                        <a-typography-text
                          v-else
                          class="desc-sub-item-desc-column"
                          :content="
                            Object.keys(unitOfBaseConfig).includes(base)
                              ? `${clusterDetail[base]} ${unitOfBaseConfig[base]}`
                              : `${clusterDetail[base]}`
                          "
                        />
                      </div>
                    </template>
                  </div>
                </div>
              </a-descriptions-item>
            </a-descriptions>
          </div>
        </div>
        <!-- service info -->
        <template v-if="locateStackWithService.length == 0">
          <div class="service-info">
            <div class="box-title">
              <a-typography-text strong :content="$t('overview.service_info')" />
            </div>
            <div class="box-empty">
              <a-empty :image="Empty.PRESENTED_IMAGE_SIMPLE" />
            </div>
          </div>
        </template>
        <a-descriptions v-else layout="vertical" bordered :column="1">
          <template #title>
            <a-typography-text strong :content="$t('overview.service_info')" />
          </template>
          <a-descriptions-item v-for="stack in locateStackWithService" :key="stack.stackName">
            <template #label>
              <div class="desc-sub-label">
                <a-typography-text
                  strong
                  :content="stack.stackName.charAt(0).toUpperCase() + stack.stackName.slice(1) + ' Stack'"
                />
                <a-typography-text type="secondary" :content="`${stack.stackName}-${stack.stackVersion}`" />
              </div>
            </template>
            <div v-for="service in servicesFromCurrentCluster(stack)" :key="service.id" class="service-item">
              <a-avatar v-if="service.name" :src="usePngImage(service.name.toLowerCase())" :size="18" />
              <a-typography-text :content="service.displayName" />
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
        </a-descriptions>
      </a-col>
      <a-col :xs="24" :sm="24" :md="24" :lg="14" :xl="17">
        <div class="box-title">
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
        <template v-if="noChartData">
          <div class="box-empty">
            <a-empty />
          </div>
        </template>
        <a-row v-else class="box-content">
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
      overflow: hidden;
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

  .service-item {
    display: grid;
    grid-template-columns: auto 1fr auto;
    gap: $space-md;
    align-items: center;
    padding: 12px 16px;
    border-top: 1px solid #f0f0f0;
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
