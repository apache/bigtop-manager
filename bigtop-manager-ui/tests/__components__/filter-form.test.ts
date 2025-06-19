import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import FilterForm from '../../src/components/common/filter-form/index.vue'

describe('FilterForm.vue', () => {
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
    const wrapper = mount(FilterForm, {
      props: {
        filterItems
      },
      global: {
        mocks: {
          $t: (key: string) => key
        }
      }
    })

    expect(wrapper.findAll('.filter-form-label').length).toBe(filterItems.length)
    expect(wrapper.html()).toContain('Status')
    expect(wrapper.html()).toContain('Search')
  })

  it('emits filter event with correct parameters', async () => {
    const wrapper = mount(FilterForm, {
      props: {
        filterItems
      },
      global: {
        mocks: {
          $t: (key: string) => key
        }
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
    const wrapper = mount(FilterForm, {
      props: {
        filterItems
      },
      global: {
        mocks: {
          $t: (key: string) => key
        }
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
