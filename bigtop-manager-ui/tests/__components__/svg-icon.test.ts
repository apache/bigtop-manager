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
import SvgIcon from '../../src/components/common/svg-icon/index.vue'
import { expect, it, describe } from 'vitest'

describe('SvgIcon', () => {
  it('renders default config of SVG icon', () => {
    const wrapper = mount(SvgIcon, {
      props: {
        name: 'test'
      }
    })
    expect(wrapper.find('use').attributes()['href']).toBe('#icon-test')
    expect(wrapper.find('use').attributes().fill).toBe('#000')
    expect(wrapper.find('svg').classes()).toContain('svg-icon')
  })

  it('renders custom config of SVG icon', () => {
    const wrapper = mount(SvgIcon, {
      props: {
        name: 'home',
        prefix: 'vietest',
        color: 'yellow',
        className: 'home'
      }
    })
    expect(wrapper.find('use').attributes()['href']).toBe('#vietest-home')
    expect(wrapper.find('use').attributes().fill).toBe('yellow')
    expect(wrapper.find('svg').classes()).toEqual(['svg-icon', 'home'])
  })
})
