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
  import { onMounted } from 'vue'
  import * as echarts from 'echarts/core'
  import { GridComponent, GridComponentOption } from 'echarts/components'
  import { LineChart, LineSeriesOption } from 'echarts/charts'
  import { UniversalTransition } from 'echarts/features'
  import { CanvasRenderer } from 'echarts/renderers'

  echarts.use([GridComponent, LineChart, CanvasRenderer, UniversalTransition])

  type EChartsOption = echarts.ComposeOption<GridComponentOption | LineSeriesOption>

  const props = defineProps<{
    chartId: string
    title: string
  }>()

  const generateTimeLabels = () => {
    const times = []
    for (let h = 0; h < 24; h++) {
      for (let m = 0; m < 60; m += 15) {
        const hour = h.toString().padStart(2, '0')
        const minute = m.toString().padStart(2, '0')
        times.push(`${hour}:${minute}`)
      }
    }
    return times
  }

  const initCharts = () => {
    const chartDom = document.getElementById(`${props.chartId}`)
    const myChart = echarts.init(chartDom, null, {
      devicePixelRatio: window.devicePixelRatio,
      renderer: 'svg'
    })
    const option: EChartsOption = {
      grid: {
        top: '12%',
        bottom: '12%',
        left: '12%',
        right: '12%'
      },
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: generateTimeLabels().slice(7, 14),
        axisLabel: {
          fontSize: 12,
          interval: 0
        }
      },
      yAxis: {
        type: 'value',
        interval: 20,
        axisLabel: {
          fontSize: 12,
          formatter: '{value} %'
        }
      },
      series: [
        {
          name: '数据',
          type: 'line',
          data: Array.from({ length: 96 }, () => Math.floor(Math.random() * 100))
        }
      ]
    }
    option && myChart.setOption(option)
  }

  onMounted(() => {
    initCharts()
  })
</script>

<template>
  <div class="chart">
    <div class="chart-title">{{ $props.title }}</div>
    <canvas :id="$props.chartId" style="width: 100%; height: 100%; padding: 16px"></canvas>
  </div>
</template>

<style lang="scss" scoped>
  .chart {
    text-align: center;
    &-title {
      text-align: start;
      font-size: 12px;
      font-weight: 600;
    }
  }
</style>
