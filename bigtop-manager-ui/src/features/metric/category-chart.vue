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
  import { roundFixed } from '@/utils/storage'
  import { type EChartsOption, useChart } from '@/composables/use-chart'

  interface Props {
    chartId: string
    title: string
    data?: any
    legendMap?: [string, string][] | undefined
    config?: EChartsOption
    xAxisData?: string[]
    formatter?: {
      yAxis?: (value: unknown) => string
      tooltip?: (value: unknown) => string
    }
  }

  const props = withDefaults(defineProps<Props>(), {
    legendMap: undefined,
    xAxisData: () => {
      return []
    },
    data: () => {
      return {}
    },
    config: () => {
      return {}
    },
    formatter: () => {
      return {}
    }
  })

  const { data, chartId, title, config, legendMap, xAxisData, formatter } = toRefs(props)
  const { initChart, setOptions } = useChart()
  const baseConfig = { type: 'line' }

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
        },
        formatter: createTooltipFormatter(formatter.value.tooltip)
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
          axisLabel: {
            width: 32,
            fontSize: 8,
            overflow: 'truncate',
            formatter: formatter.value.yAxis ?? '{value} %'
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

  const defaultTooltipFormatter = (val: unknown) => {
    const num = roundFixed(val)
    return num ? `${num} %` : '--'
  }

  const tooltipHtml = (item: any) => {
    return `
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; gap: 12px">
            <div style="display: flex; align-items: center;">
              <div>${item.marker}${item.seriesName}</div>
            </div>
            <div>${item.valueText}</div>
          </div>
        `
  }

  const createTooltipFormatter = (formatValue?: (value: unknown) => string) => {
    const format = formatValue ?? defaultTooltipFormatter
    return (params: any) => {
      const title = params[0]?.axisValueLabel ?? ''
      const lines = params
        .map((item: any) => {
          const valueText = format(item.value)
          return tooltipHtml({ ...item, valueText })
        })
        .join('')
      return `<div style="margin-bottom: 4px;">${title}</div>${lines}`
    }
  }

  /**
   * Generates ECharts series config by mapping legend keys to data and formatting values.
   *
   * @param data - A partial object containing data arrays for each series key.
   * @param legendMap - An array of [key, displayName] pairs.
   * @returns An array of ECharts series config objects with populated and formatted data.
   */
  const generateChartSeries = <T,>(data: Partial<T>, legendMap: [string, string][]) => {
    return legendMap.map(([key, name]) => ({
      name,
      ...baseConfig,
      data: (data[key] || []).map((v: unknown) => roundFixed(v))
    }))
  }

  onMounted(() => {
    const selector = document.getElementById(`${chartId.value}`)
    if (selector) {
      initChart(selector!, option.value)
    }
  })

  watchEffect(() => {
    let series = [] as any,
      legend = [] as any

    if (legendMap.value) {
      legend = new Map(legendMap.value).values()
      series = generateChartSeries(data.value, legendMap.value)
    } else {
      series = [
        {
          name: title.value.toLowerCase(),
          data: data.value.map((v) => roundFixed(v))
        }
      ]
    }

    setOptions({
      xAxis: xAxisData.value
        ? [{ data: xAxisData.value?.map((v) => dayjs(Number(v) * 1000).format('HH:mm')) || [] }]
        : [],
      ...config.value,
      legend,
      series
    })
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
