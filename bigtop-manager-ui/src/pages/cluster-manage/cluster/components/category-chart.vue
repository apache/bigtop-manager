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
  import { onBeforeUnmount, onMounted, shallowRef } from 'vue'
  import * as echarts from 'echarts/core'
  import { GridComponent, GridComponentOption, TooltipComponent, TooltipComponentOption } from 'echarts/components'
  import { LineChart, LineSeriesOption } from 'echarts/charts'
  import { UniversalTransition } from 'echarts/features'
  import { CanvasRenderer } from 'echarts/renderers'

  echarts.use([GridComponent, LineChart, TooltipComponent, CanvasRenderer, UniversalTransition])

  type EChartsOption = echarts.ComposeOption<GridComponentOption | TooltipComponentOption | LineSeriesOption>

  const props = defineProps<{
    chartId: string
    title: string
  }>()

  const myChart = shallowRef<echarts.ECharts | null>(null)

  const generateTimeLabels = () => {
    const times = [] as Array<string>
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
    myChart.value = echarts.init(chartDom, null, {
      devicePixelRatio: window.devicePixelRatio
    })
    const option: EChartsOption = {
      grid: {
        top: '20px',
        left: '40px',
        right: '30px',
        bottom: '20px'
      },
      tooltip: {
        trigger: 'axis',
        backgroundColor: '#000',
        borderColor: 'rgba(236,236,236,0.1)',
        // formatter: (params) => {
        //   console.log('params :>> ', params)
        //   return ''
        // },
        textStyle: {
          color: '#fff'
        },
        axisPointer: {
          type: 'cross',
          crossStyle: {
            color: '#999'
          }
        }
      },
      xAxis: [
        {
          type: 'category',
          data: generateTimeLabels(),
          axisPointer: {
            type: 'line'
          },
          axisLabel: {
            interval: 14,
            fontSize: 8
          }
        }
      ],
      yAxis: [
        {
          type: 'value',
          axisPointer: {
            type: 'shadow',
            label: {
              formatter: '{value} %'
            }
          },
          min: 0,
          max: 100,
          interval: 20,
          axisLabel: {
            fontSize: 8,
            formatter: '{value} %'
          }
        }
      ],
      series: [
        {
          name: props.title,
          type: 'line',
          data: Array.from({ length: generateTimeLabels().length }, () => Math.floor(Math.random() * 0)),
          lineStyle: {
            color: '#49A4FF',
            width: 2
          }
        }
      ]
    }
    option && myChart.value.setOption(option)
  }

  const resizeChart = () => {
    myChart.value?.resize()
  }

  onMounted(() => {
    initCharts()
    window.addEventListener('resize', resizeChart, true)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('resize', resizeChart, true)
  })
</script>

<template>
  <div class="chart">
    <div class="chart-title">{{ $props.title }}</div>
    <div :id="$props.chartId" style="height: 260px; width: 100%"></div>
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
