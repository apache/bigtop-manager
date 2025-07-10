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
  import dayjs from 'dayjs'
  import { computed, onMounted, toRefs, watchEffect } from 'vue'
  import { type EChartsOption, useChart } from '@/composables/use-chart'
  import { formatSeriesData, roundFixed } from '@/utils/chart'

  interface Props {
    chartId: string
    title: string
    data?: any
    timeDistance?: string
    legendMap?: [string, string][] | undefined
    config?: EChartsOption
    yAxisUnit?: string
  }

  const props = withDefaults(defineProps<Props>(), {
    yAxisUnit: '%',
    timeDistance: '15m',
    data: () => [],
    legendMap: undefined,
    config: () => {
      return {}
    }
  })

  const { data, chartId, title, config, legendMap } = toRefs(props)
  const { initChart, setOptions } = useChart()

  const option = computed(
    (): EChartsOption => ({
      grid: {
        top: '30px',
        left: '40px',
        right: '30px',
        bottom: '30px'
      },
      tooltip: {
        trigger: 'axis',
        backgroundColor: '#000',
        borderColor: 'rgba(236,236,236,0.1)',
        textStyle: {
          color: '#fff'
        }
      },
      xAxis: [
        {
          type: 'category',
          boundaryGap: false,
          data: [],
          axisPointer: {
            type: 'line'
          },
          axisLabel: {
            fontSize: 10
          }
        }
      ],
      yAxis: [
        {
          type: 'value',
          axisPointer: {
            type: 'shadow',
            label: {
              formatter: '{value}' + `${props.yAxisUnit ?? ''}`
            }
          },
          axisLabel: {
            width: 32,
            fontSize: 8,
            formatter: '{value}' + `${props.yAxisUnit ?? ''}`,
            overflow: 'truncate'
          }
        }
      ],
      series: [
        {
          name: title.value,
          type: 'line',
          data: [],
          lineStyle: {
            color: '#49A4FF',
            width: 2
          }
        }
      ]
    })
  )

  // const intervalToMs = (interval: string): number => {
  //   const unit = interval.replace(/\d+/g, '')
  //   const value = parseInt(interval)

  //   switch (unit) {
  //     case 'm':
  //       return value * 60 * 1000
  //     case 'h':
  //       return value * 60 * 60 * 1000
  //     default:
  //       throw new Error('Unsupported interval: ' + interval)
  //   }
  // }

  // const getTimePoints = (interval: string = '15m'): string[] => {
  //   const now = dayjs()
  //   const gap = intervalToMs(interval)
  //   const result: string[] = []

  //   for (let i = 5; i >= 0; i--) {
  //     const time = now.subtract(i * gap, 'millisecond')
  //     result.push(time.format('HH:mm'))
  //   }

  //   return result
  // }

  onMounted(() => {
    const selector = document.getElementById(`${chartId.value}`)
    selector && initChart(selector!, option.value)
  })

  watchEffect(() => {
    setOptions({
      xAxis: [{ data: Object.keys(data.value).map((v) => dayjs(Number(v) * 1000).format('HH:mm')) || [] }]
    })
  })

  watchEffect(() => {
    let series = [] as any,
      legend = [] as any

    if (legendMap.value) {
      legend = new Map(legendMap.value).values()
      series = formatSeriesData(data.value, legendMap.value)
    } else {
      series = [{ name: title.value.toLowerCase(), data: Object.values(data.value).map((v) => roundFixed(Number(v))) }]
    }

    setOptions({ ...config.value, legend, series })
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
