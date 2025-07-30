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

import type { App } from 'vue'

const kebabToCamel = (kebab: string): string => {
  return kebab.replace(/-./g, (match) => match.charAt(1).toUpperCase()).replace(/^./, (match) => match.toUpperCase())
}

const install = (app: App) => {
  const commons = import.meta.glob('../../components/common/**/index.vue', {
    eager: true
  }) as any

  const bases = import.meta.glob('../../components/base/**/index.vue', {
    eager: true
  }) as any

  const components = { ...commons, ...bases }

  for (const path in components) {
    const component = components[path].default
    const componentName = kebabToCamel(path.split('/')[1])
    if (componentName) {
      app.component(componentName, component)
    }
  }
}

export default {
  install
}
