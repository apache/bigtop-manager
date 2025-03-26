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
  import { computed, ref, shallowRef, toRefs, watch } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { Empty, MenuProps } from 'ant-design-vue'
  import { formatFromByte } from '@/utils/storage.ts'
  import { usePngImage } from '@/utils/tools'
  import { CommonStatus, CommonStatusTexts } from '@/enums/state.ts'
  import CategoryChart from '@/pages/cluster-manage/cluster/components/category-chart.vue'
  import GaugeChart from '@/pages/cluster-manage/cluster/components/gauge-chart.vue'
  import { getComponentsByHost } from '@/api/hosts'
  import type { HostStatusType, HostVO } from '@/api/hosts/types.ts'
  import type { ClusterStatusType } from '@/api/cluster/types.ts'
  import type { ComponentVO } from '@/api/component/types.ts'

  type TimeRangeText = '1m' | '15m' | '30m' | '1h' | '6h' | '30h'
  type TimeRangeItem = {
    text: TimeRangeText
    time: string
  }

  interface Props {
    hostInfo: HostVO
  }

  const props = defineProps<Props>()
  const { hostInfo } = toRefs(props)
  const { t } = useI18n()
  const currExecuteComponent = shallowRef<ComponentVO>({})
  const currTimeRange = ref<TimeRangeText>('15m')
  const statusColors = shallowRef<Record<HostStatusType, keyof typeof CommonStatusTexts>>({
    1: 'healthy',
    2: 'unhealthy',
    3: 'unknown'
  })
  const chartData = ref({
    chart1: [],
    chart2: [],
    chart3: [],
    chart4: []
  })
  const componentsFromCurrentHost = shallowRef<Map<string, ComponentVO[]>>(new Map())
  const needFormatFormByte = computed(() => ['totalMemorySize', 'totalDisk'])
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
  const baseConfig = computed((): Partial<Record<keyof HostVO, string>> => {
    return {
      status: t('overview.host_status'),
      hostname: t('overview.host_name'),
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
    }
  })
  const unitOfBaseConfig = computed(
    (): Partial<Record<keyof HostVO, string>> => ({
      componentNum: t('overview.unit_component'),
      availableProcessors: t('overview.unit_core')
    })
  )
  const detailKeys = computed((): (keyof HostVO)[] => Object.keys(baseConfig.value))
  const componentOperates = computed(() => [
    {
      action: 'start',
      text: t('common.start', [t('common.component')])
    },
    {
      action: 'restart',
      text: t('common.restart', [t('common.component')])
    },
    {
      action: 'stop',
      text: t('common.stop', [t('common.component')])
    }
  ])

  const handleHostOperate: MenuProps['onClick'] = (item) => {
    console.log('item :>> ', item.key)
    console.log(currExecuteComponent.value)
  }

  const handleTimeRange = (time: TimeRangeItem) => {
    currTimeRange.value = time.text
  }

  const recordClickComp = (comp: ComponentVO) => {
    currExecuteComponent.value = comp
  }

  watch(
    () => hostInfo.value,
    async (val) => {
      if (val.id) {
        try {
          const data = await getComponentsByHost({ id: val.id })
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
    }
  )
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
                          :color="CommonStatus[statusColors[hostInfo[base] as ClusterStatusType]]"
                        >
                          <status-dot :color="CommonStatus[statusColors[hostInfo[base] as ClusterStatusType]]" />
                          {{ hostInfo[base] && $t(`common.${statusColors[hostInfo[base] as ClusterStatusType]}`) }}
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
              <a-typography-text strong :content="$t('overview.component_info')" />
            </div>
            <div class="box-empty">
              <a-empty :image="Empty.PRESENTED_IMAGE_SIMPLE" />
            </div>
          </div>
        </template>
        <a-descriptions v-else layout="vertical" bordered :column="1">
          <template #title>
            <a-typography-text strong :content="$t('overview.component_info')" />
          </template>
          <a-descriptions-item v-for="stack in componentsFromCurrentHost.keys()" :key="stack">
            <template #label>
              <div class="desc-sub-label">
                <a-typography-text strong :content="stack.split('-')[0] + ' Stack'" />
                <a-typography-text type="secondary" :content="`${stack.toLowerCase()}`" />
              </div>
            </template>
            <div v-for="comp in componentsFromCurrentHost.get(stack)" :key="comp.id" class="component-item">
              <a-avatar v-if="comp.serviceName" :src="usePngImage(comp.serviceName.toLowerCase())" :size="18" />
              <a-typography-text :content="`${comp.serviceDisplayName}/${comp.displayName}`" />
              <a-dropdown :trigger="['click']">
                <a-button type="text" shape="circle" size="small" @click="recordClickComp(comp)">
                  <svg-icon name="more" style="margin: 0" />
                </a-button>
                <template #overlay>
                  <a-menu @click="handleHostOperate">
                    <a-menu-item v-for="operate in componentOperates" :key="operate.action">
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
