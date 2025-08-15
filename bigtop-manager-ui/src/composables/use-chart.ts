/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import * as echarts from 'echarts/core'
import { GaugeChart, GaugeSeriesOption, LineChart, LineSeriesOption } from 'echarts/charts'
import {
  TitleComponent,
  GridComponent,
  GridComponentOption,
  TooltipComponent,
  TooltipComponentOption,
  LegendComponent,
  LegendComponentOption
} from 'echarts/components'
import { UniversalTransition } from 'echarts/features'
import { CanvasRenderer } from 'echarts/renderers'

export type EChartsOption = echarts.ComposeOption<
  GaugeSeriesOption | GridComponentOption | TooltipComponentOption | LineSeriesOption | LegendComponentOption
>

echarts.use([
  TitleComponent,
  GaugeChart,
  CanvasRenderer,
  GridComponent,
  LineChart,
  TooltipComponent,
  CanvasRenderer,
  UniversalTransition,
  LegendComponent
])

export const useChart = () => {
  let resizeTimer: number | null = null
  const chartsRef = shallowRef<echarts.ECharts | null>(null)
  const opts = shallowRef({ devicePixelRatio: window.devicePixelRatio })

  const initChart = (selector: HTMLElement, option: EChartsOption) => {
    if (!selector) {
      new Error('Selector is required to initialize the chart.')
    }
    if (chartsRef.value) {
      chartsRef.value.dispose()
    }
    chartsRef.value = echarts.init(selector, null, opts.value)
    chartsRef.value?.setOption(option)
  }

  const setOptions = (options: EChartsOption) => {
    chartsRef.value?.setOption(options)
  }

  const resizeChart = () => {
    if (resizeTimer) clearTimeout(resizeTimer)
    resizeTimer = window.setTimeout(() => {
      chartsRef.value?.resize()
    }, 300)
  }

  const disposeChart = () => {
    chartsRef.value?.dispose()
    chartsRef.value = null
  }

  onMounted(() => {
    window.addEventListener('resize', resizeChart, true)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('resize', resizeChart, true)
    disposeChart()
  })

  return {
    chartsRef,
    initChart,
    setOptions,
    disposeChart
  }
}
