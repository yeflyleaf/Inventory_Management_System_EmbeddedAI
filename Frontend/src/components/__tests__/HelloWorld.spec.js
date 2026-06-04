/**
 * 模块名称: HelloWorld 组件测试
 * 功能描述: HelloWorld 组件的单元测试文件，使用 Vitest 和 Vue Test Utils 验证组件渲染是否正确。
 */
import { describe, expect, it } from 'vitest'

import { mount } from '@vue/test-utils'
import HelloWorld from '../HelloWorld.vue'

describe('HelloWorld', () => {
  it('renders properly', () => {
    const wrapper = mount(HelloWorld, { props: { msg: 'Hello Vitest' } })
    expect(wrapper.text()).toContain('Hello Vitest')
  })
})
