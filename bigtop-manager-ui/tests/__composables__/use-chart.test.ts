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

import { mount } from '@vue/test-utils'
import { describe, it, vi, expect, beforeEach } from 'vitest'
import { defineComponent, h } from 'vue'
import { useChart } from '../../src/composables/use-chart'
import { withSetup } from '../test-util'

vi.mock('echarts/core', async () => {
  const setOption = vi.fn()
  const resize = vi.fn()
  const dispose = vi.fn()

  return {
    init: vi.fn(() => ({
      setOption,
      resize,
      dispose
    })),
    use: vi.fn()
  }
})

describe('useChart', () => {
  const mockDiv = document.createElement('div')

  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('should initialize chart', async () => {
    const echarts = vi.mocked(await import('echarts/core'), true)
    const [chart] = withSetup(useChart, { title: { text: 'Test' } })

    chart.initChart(mockDiv, { title: { text: 'Test' } })
    expect(echarts.init).toHaveBeenCalledWith(mockDiv, null, expect.any(Object))
  })

  it('should call setOption', async () => {
    const echarts = vi.mocked(await import('echarts/core'), true)
    const [chart] = withSetup(useChart, { title: { text: 'Init' } })

    chart.initChart(mockDiv, { title: { text: 'Init' } })
    chart.setOptions({ title: { text: 'Updated' } })

    expect(echarts.init.mock.results[0].value.setOption).toHaveBeenCalledWith({
      title: { text: 'Updated' }
    })
  })

  it('should listen and cleanup resize event', () => {
    const addEventListenerSpy = vi.spyOn(window, 'addEventListener')
    const removeEventListenerSpy = vi.spyOn(window, 'removeEventListener')

    const wrapper = mount(
      defineComponent({
        setup() {
          useChart()
          return () => h('div')
        }
      })
    )

    expect(addEventListenerSpy).toHaveBeenCalledWith('resize', expect.any(Function), true)

    wrapper.unmount()

    expect(removeEventListenerSpy).toHaveBeenCalledWith('resize', expect.any(Function), true)
  })

  it('should dispose chart and remove event listener on unmount', async () => {
    const echarts = vi.mocked(await import('echarts/core'), true)
    const el = document.createElement('div')
    const [chart, app] = withSetup(useChart, { title: { text: 'test' } })

    chart.initChart(el, { title: { text: 'test' } })
    const instance = echarts.init.mock.results[0].value

    await app.mount()
    expect(instance.dispose).not.toHaveBeenCalled()

    await app.unmount()
    expect(instance.dispose).toHaveBeenCalled()
  })
})
