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
  import { GaugeChart, GaugeSeriesOption } from 'echarts/charts'
  import { CanvasRenderer } from 'echarts/renderers'

  echarts.use([GaugeChart, CanvasRenderer])

  type EChartsOption = echarts.ComposeOption<GaugeSeriesOption>

  const props = defineProps<{
    chartId: string
    title: string
  }>()

  const initCharts = () => {
    const chartDom = document.getElementById(`${props.chartId}`)
    const myChart = echarts.init(chartDom, null, {
      devicePixelRatio: window.devicePixelRatio,
      renderer: 'svg'
    })
    const option: EChartsOption = {
      series: [
        {
          type: 'gauge',
          radius: '90%',
          center: ['50%', '56%'],
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
            distance: 28,
            fontSize: 14
          },
          detail: {
            valueAnimation: true,
            formatter: '{value} %',
            color: 'inherit',
            fontSize: 18
          },
          data: [
            {
              value: 80
            }
          ]
        }
      ]
    }

    myChart.setOption<echarts.EChartsCoreOption>({
      series: [
        {
          data: [
            {
              value: +(Math.random() * 100).toFixed(2)
            }
          ]
        }
      ]
    })

    option && myChart.setOption(option)
  }

  onMounted(() => {
    initCharts()
  })
</script>

<template>
  <div class="chart">
    <div class="chart-title">{{ $props.title }}</div>
    <canvas :id="$props.chartId" style="width: 100%; height: 100%"></canvas>
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
