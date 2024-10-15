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

import { defineStore } from 'pinia'
import { ref } from 'vue'
import { theme as antdTheme } from 'ant-design-vue'

export const useTheme = defineStore(
  'theme',
  () => {
    const { useToken } = antdTheme
    const { token: antdToken } = useToken()
    const themeType = ref('default')
    const themeMap = {
      default: {
        algorithm: antdTheme.defaultAlgorithm
      },
      dark: {
        token: {
          colorPrimary: '#00b96b'
        },
        algorithm: antdTheme.darkAlgorithm
      }
    }

    const triggerTheme = (triggerVal: string) => {
      themeType.value = triggerVal
      injectCssVariablesIntoHead(generateCssVariables())
    }

    const generateCssVariables = () => {
      const token = antdToken.value
      const variables = Object.keys(token).map((key) => {
        const cssVarName = `--${key.replace(/([A-Z])/g, '-$1').toLowerCase()}`
        return `${cssVarName}: ${token[key as keyof typeof token]};`
      })
      return `:root {\n${variables.join('\n')}\n}`
    }

    // Inject style variables in token into style tag
    function injectCssVariablesIntoHead(cssVariables: string) {
      console.log('cssVariables :>> ', cssVariables)
      let styleElement = document.getElementById('theme-variables')
      if (styleElement) {
        document.head.removeChild(styleElement)
      }
      styleElement = document.createElement('style')
      styleElement.id = 'theme-variables'
      document.head.appendChild(styleElement)
      styleElement.innerHTML = cssVariables
    }

    return {
      themeMap,
      themeType,
      triggerTheme,
      generateCssVariables,
      injectCssVariablesIntoHead
    }
  },
  { persist: true }
)
