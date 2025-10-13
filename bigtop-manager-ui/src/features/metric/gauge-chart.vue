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
  import { type EChartsOption, useChart } from '@/composables/use-chart'
  import { roundFixed } from '@/utils/storage'

  interface GaugeChartProps {
    chartId: string
    title: string
    percent?: string
  }

  const props = withDefaults(defineProps<GaugeChartProps>(), { percent: '0.00' })
  const { percent, chartId, title } = toRefs(props)
  const { initChart, setOptions } = useChart()

  const option = shallowRef<EChartsOption>({
    series: [
      {
        type: 'gauge',
        radius: '92%',
        center: ['50%', '50%'],
        axisLine: {
          lineStyle: {
            width: 14,
            color: [
              [0.3, '#67e0e3'],
              [0.7, '#37a2da'],
              [1, '#fd666d']
            ]
          }
        },
        pointer: {
          width: 3,
          length: '58%',
          itemStyle: {
            color: 'auto'
          }
        },
        axisTick: {
          distance: -50,
          length: 40,
          lineStyle: {
            color: '#fff',
            width: 1
          }
        },
        splitLine: {
          distance: -28,
          length: 28,
          lineStyle: {
            color: '#fff',
            width: 4
          }
        },
        axisLabel: {
          color: 'inherit',
          distance: 22,
          fontSize: 12
        },
        detail: {
          valueAnimation: true,
          color: 'inherit',
          fontSize: 18,
          formatter: (val: number) => `${roundFixed(val, 2, '0.00', false)}%`
        },
        data: [
          {
            value: 0.0
          }
        ]
      }
    ]
  })

  const renderChart = () => {
    const el = document.getElementById(chartId.value)
    if (el) initChart(el, option.value)
  }

  onMounted(renderChart)
  onActivated(renderChart)

  watchEffect(() => {
    setOptions({ series: [{ data: [{ value: percent.value }] }] })
  })
</script>

<template>
  <div class="chart">
    <div class="chart-title">{{ title }}</div>
    <div :id="chartId" style="height: 260px; width: 100%"></div>
  </div>
</template>

<style lang="scss" scoped>
  .chart {
    height: 300px;
    box-sizing: border-box;
    padding: 10px;
    &-title {
      text-align: start;
      font-size: 12px;
      font-weight: 600;
    }
  }
</style>
