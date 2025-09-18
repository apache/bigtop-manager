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
  import { formatFromByte } from '@/utils/storage.ts'
  import { usePngImage } from '@/utils/tools'
  import { CommonStatus, CommonStatusTexts } from '@/enums/state.ts'
  import { useServiceStore } from '@/store/service'
  import { useJobProgress } from '@/store/job-progress'
  import { useStackStore } from '@/store/stack'
  import { getComponentsByHost } from '@/api/host'
  import { Command } from '@/api/command/types'
  import { getHostMetricsInfo } from '@/api/metrics'

  import GaugeChart from '@/features/metric/gauge-chart.vue'
  import CategoryChart from '@/features/metric/category-chart.vue'

  import type { HostStatusType, HostVO } from '@/api/host/types'
  import type { ClusterStatusType } from '@/api/cluster/types.ts'
  import type { ComponentVO } from '@/api/component/types.ts'
  import type { MetricsData, TimeRangeType } from '@/api/metrics/types'

  type StatusColorType = Record<HostStatusType, keyof typeof CommonStatusTexts>

  interface Props {
    hostInfo: HostVO
  }

  const props = defineProps<Props>()

  const { t } = useI18n()
  const stackStore = useStackStore()
  const serviceStore = useServiceStore()
  const jobProgressStore = useJobProgress()

  const currTimeRange = ref<TimeRangeType>('5m')
  const chartData = ref<Partial<MetricsData>>({})

  const componentsFromCurrentHost = shallowRef<Map<string, ComponentVO[]>>(new Map())
  const needFormatFormByte = shallowRef(['totalMemorySize', 'totalDisk'])
  const timeRanges = shallowRef<TimeRangeType[]>(['1m', '5m', '15m', '30m', '1h', '2h'])
  const statusColors = shallowRef<StatusColorType>({ 1: 'healthy', 2: 'unhealthy', 3: 'unknown' })

  const { hostInfo } = toRefs(props)

  const baseConfig = computed(
    (): Partial<Record<keyof HostVO, string>> => ({
      status: t('overview.host_status'),
      hostname: t('overview.hostname'),
      desc: t('overview.host_desc'),
      clusterDisplayName: t('common.cluster'),
      componentNum: t('overview.component_count'),
      os: t('overview.os'),
      ipv4: t('overview.ip_v4'),
      ipv6: t('overview.ip_v6'),
      arch: t('overview.arch'),
      availableProcessors: t('overview.core_count'),
      totalMemorySize: t('overview.memory'),
      totalDisk: t('overview.disk_size')
    })
  )

  const unitOfBaseConfig = computed(
    (): Partial<Record<keyof HostVO, string>> => ({
      componentNum: t('overview.unit_component'),
      availableProcessors: t('overview.unit_core')
    })
  )

  const componentOperates = computed(() => ({
    Start: t('common.start', [t('common.component')]),
    Restart: t('common.restart', [t('common.component')]),
    Stop: t('common.stop', [t('common.component')])
  }))

  const detailKeys = computed((): (keyof HostVO)[] => Object.keys(baseConfig.value))
  const noChartData = computed(() => Object.values(chartData.value).length === 0)

  const handleComponentOperate = (item: any, component: ComponentVO) => {
    const { serviceName } = component
    const installedServiceMap = Object.values(serviceStore.serviceMap)
      .flat()
      .filter((v) => v.name === serviceName)
    if (installedServiceMap.length > 0) {
      jobProgressStore.processCommand(
        {
          command: item.key as keyof typeof Command,
          clusterId: installedServiceMap[0].clusterId,
          commandLevel: 'component',
          componentCommands: [{ componentName: component.name!, hostnames: [component.hostname!] }]
        },
        getComponentInfo,
        { displayName: component.displayName }
      )
    }
  }

  const handleTimeRange = (time: TimeRangeType) => {
    if (currTimeRange.value == time) {
      return
    }
    currTimeRange.value = time
    getHostMetrics()
  }

  const getHostMetrics = async () => {
    try {
      chartData.value = await getHostMetricsInfo({ id: hostInfo.value.id! }, { interval: currTimeRange.value })
    } catch (error) {
      console.log('Failed to fetch host metrics:', error)
    }
  }

  const getComponentInfo = async () => {
    try {
      const data = await getComponentsByHost({ id: hostInfo.value.id! })
      componentsFromCurrentHost.value = data.reduce((pre, val) => {
        if (!pre.has(val.stack!)) {
          pre.set(val.stack!, [val])
        } else {
          ;(pre.get(val.stack!) as ComponentVO[]).push(val)
        }
        return pre
      }, new Map<string, ComponentVO[]>())
    } catch (error) {
      console.log(error)
    }
  }

  const { pause, resume } = useIntervalFn(getHostMetrics, 30000, { immediate: true })

  watch(
    () => hostInfo.value,
    (val) => {
      if (val.id) {
        getComponentInfo()
        getHostMetrics()
        resume()
      } else {
        pause()
      }
    }
  )

  onUnmounted(() => {
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
                          :color="CommonStatus[statusColors[hostInfo[base] as ClusterStatusType]]"
                        >
                          <status-dot :color="CommonStatus[statusColors[hostInfo[base] as ClusterStatusType]]" />
                          {{ hostInfo[base] && t(`common.${statusColors[hostInfo[base] as ClusterStatusType]}`) }}
                        </a-tag>
                        <a-typography-text v-else class="desc-sub-item-desc-column">
                          <span v-if="Object.keys(unitOfBaseConfig).includes(base as string)">
                            {{ `${hostInfo[base]} ${unitOfBaseConfig[base]}` }}
                          </span>
                          <span v-else>
                            {{
                              `${
                                needFormatFormByte.includes(base as string)
                                  ? formatFromByte(hostInfo[base])
                                  : hostInfo[base] || '--'
                              }`
                            }}
                          </span>
                        </a-typography-text>
                      </div>
                    </template>
                  </div>
                </div>
              </a-descriptions-item>
            </a-descriptions>
          </div>
        </div>

        <template v-if="componentsFromCurrentHost.size === 0">
          <div class="component-info">
            <div class="box-title">
              <a-typography-text strong :content="t('overview.component_info')" />
            </div>
            <div class="box-empty">
              <a-empty :image="Empty.PRESENTED_IMAGE_SIMPLE" />
            </div>
          </div>
        </template>
        <a-descriptions v-else layout="vertical" bordered :column="1">
          <template #title>
            <a-typography-text strong :content="t('overview.component_info')" />
          </template>
          <a-descriptions-item v-for="stack in componentsFromCurrentHost.keys()" :key="stack">
            <template #label>
              <div class="desc-sub-label">
                <a-typography-text strong :content="stack.split('-')[0] + ' Stack'" />
                <a-typography-text type="secondary" :content="`${stack.toLowerCase()}`" />
              </div>
            </template>
            <div v-for="comp in componentsFromCurrentHost.get(stack)" :key="comp.id" class="component-item">
              <a-avatar v-if="comp.serviceName" :src="usePngImage(comp.serviceName.toLowerCase())" :size="22" />
              <a-typography-text :content="`${comp.serviceDisplayName}/${comp.displayName}`" />
              <a-dropdown
                v-if="stackStore.stackRelationMap?.components[comp.name!].category != 'client'"
                :trigger="['click']"
              >
                <a-button type="text" shape="circle" size="small">
                  <svg-icon name="more" style="margin: 0" />
                </a-button>
                <template #overlay>
                  <a-menu @click="handleComponentOperate($event, comp)">
                    <a-menu-item v-for="[operate, text] of Object.entries(componentOperates)" :key="operate">
                      <span>{{ text }}</span>
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
          <a-typography-text strong :content="t('overview.chart')" />
          <a-space :size="12">
            <div
              v-for="time in timeRanges"
              :key="time"
              tabindex="0"
              class="time-range"
              :class="{ 'time-range-activated': currTimeRange === time }"
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
          <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <div class="chart-item-wrp">
              <gauge-chart chart-id="chart1" :percent="chartData?.memoryUsageCur" :title="t('overview.memory_usage')" />
            </div>
          </a-col>
          <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <div class="chart-item-wrp">
              <gauge-chart chart-id="chart2" :percent="chartData?.cpuUsageCur" :title="t('overview.cpu_usage')" />
            </div>
          </a-col>
          <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <div class="chart-item-wrp">
              <category-chart
                chart-id="chart3"
                :x-axis-data="chartData?.timestamps"
                :data="chartData?.memoryUsage"
                :title="t('overview.memory_usage')"
              />
            </div>
          </a-col>
          <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <div class="chart-item-wrp">
              <category-chart
                chart-id="chart4"
                :x-axis-data="chartData?.timestamps"
                :data="chartData?.cpuUsage"
                :title="t('overview.cpu_usage')"
              />
            </div>
          </a-col>
          <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <div class="chart-item-wrp">
              <category-chart
                chart-id="chart5"
                :x-axis-data="chartData?.timestamps"
                :data="chartData"
                :title="t('overview.system_load')"
                :formatter="{
                  tooltip: (val) => `${val == null || val == '' ? '--' : val}`,
                  yAxis: (val) => `${val}`
                }"
                :legend-map="[
                  ['systemLoad1', 'load1'],
                  ['systemLoad5', 'load5'],
                  ['systemLoad15', 'load15']
                ]"
              />
            </div>
          </a-col>
          <a-col :xs="24" :sm="24" :md="12" :lg="12" :xl="12">
            <div class="chart-item-wrp">
              <category-chart
                chart-id="chart6"
                :x-axis-data="chartData?.timestamps"
                :data="chartData"
                :title="t('overview.disk_io')"
                :formatter="{
                  tooltip: (val) => `${val == null || val == '' ? '--' : formatFromByte(val as number, 0)}`,
                  yAxis: (val) => formatFromByte(val as number, 0)
                }"
                :legend-map="[
                  ['diskRead', 'read'],
                  ['diskWrite', 'write']
                ]"
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

  .component-item {
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
