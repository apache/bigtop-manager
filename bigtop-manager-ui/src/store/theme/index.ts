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

import { theme as antdTheme } from 'ant-design-vue'
import { GlobalToken } from 'ant-design-vue/es/theme'
import { componentsConfigProvider } from './config'

const themeMap = {
  default: {
    components: componentsConfigProvider,
    algorithm: antdTheme.defaultAlgorithm
  },
  dark: {
    components: componentsConfigProvider,
    algorithm: antdTheme.darkAlgorithm
  }
}

type ThemeMode = keyof typeof themeMap

export const useThemeStore = defineStore(
  'theme',
  () => {
    const { useToken } = antdTheme
    const { token } = useToken()
    const themeMode = ref<ThemeMode>('default')
    const themeConfig = computed(() => themeMap[themeMode.value])

    // change of theme token is async
    watch(token, (newToken) => {
      injectCssVariablesIntoHead(generateCssVariables(newToken))
    })

    const toggleTheme = (currTheme: ThemeMode) => {
      themeMode.value = currTheme
    }

    const generateCssVariables = (token: GlobalToken) => {
      const variables = Object.keys(token).map((key) => {
        const cssVarName = `--${key.replace(/([A-Z])/g, '-$1').toLowerCase()}`
        return `${cssVarName}: ${token[key as keyof typeof token]};`
      })
      return `:root {\n${variables.join('\n')}\n}`
    }

    // inject style variables in token into style tag
    const injectCssVariablesIntoHead = (cssVariables: string) => {
      let styleElement = document.getElementById('theme-variables')
      if (styleElement) {
        document.head.removeChild(styleElement)
      }
      styleElement = document.createElement('style')
      styleElement.id = 'theme-variables'
      document.head.appendChild(styleElement)
      styleElement.innerHTML = cssVariables
    }

    const initTheme = () => {
      injectCssVariablesIntoHead(generateCssVariables(token.value))
    }

    return {
      themeMode,
      themeConfig,
      initTheme,
      toggleTheme,
      generateCssVariables,
      injectCssVariablesIntoHead
    }
  },
  { persist: true }
)
