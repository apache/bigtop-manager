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
