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
import { describe, it, expect } from 'vitest'
import { i18nPlugins } from '../test-util.ts'
import FormFilter from '../../src/components/common/form-filter/index.vue'

const i18n = i18nPlugins()

describe('FormFilter.vue', () => {
  const filterItems = [
    {
      key: 'status',
      label: 'Status',
      type: 'status',
      options: [
        { value: 'active', label: 'Active' },
        { value: 'inactive', label: 'Inactive' }
      ]
    },
    {
      key: 'search',
      label: 'Search',
      type: 'search'
    }
  ]

  it('renders filter items correctly', () => {
    const wrapper = mount(FormFilter, {
      props: {
        filterItems
      },
      global: {
        plugins: [i18n]
      }
    })

    expect(wrapper.findAll('.filter-form-label').length).toBe(filterItems.length)
    expect(wrapper.html()).toContain('Status')
    expect(wrapper.html()).toContain('Search')
  })

  it('emits filter event with correct parameters', async () => {
    const wrapper = mount(FormFilter, {
      props: {
        filterItems
      },
      global: {
        plugins: [i18n]
      }
    })

    // Simulate selecting a status
    wrapper.vm['filterParams'].status = 'active'
    await wrapper.vm.$nextTick()

    // Confirm filter
    wrapper.vm['confirmFilterParams']()
    expect(wrapper.emitted().filter).toBeTruthy()
    expect(wrapper.emitted().filter[0]).toEqual([{ status: 'active', search: undefined }])
  })

  it('resets filter correctly', async () => {
    const wrapper = mount(FormFilter, {
      props: {
        filterItems
      },
      global: {
        plugins: [i18n]
      }
    })

    // Simulate selecting a status
    wrapper.vm['filterParams'].status = 'active'
    await wrapper.vm.$nextTick()

    // Reset filter
    wrapper.vm['resetFilter'](filterItems[0])
    await wrapper.vm.$nextTick()

    expect(wrapper.vm['filterParams'].status).toBeUndefined()
  })
})
