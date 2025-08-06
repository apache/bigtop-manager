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
import router from '@/router'
import pinia from '@/store'
import i18n from '@/locales'
import { message } from 'ant-design-vue'
import components from '@/components'
import directives from '@/directives'
import VueDOMPurifyHTML from 'vue-dompurify-html'

interface PluginOptions {
  antdMessageMaxCount: number
}

export default {
  install(app: App, options: PluginOptions) {
    app.use(pinia)
    app.use(router)
    app.use(i18n)
    app.use(directives)
    app.use(components)
    app.use(VueDOMPurifyHTML) // xss defense
    message.config({ maxCount: options.antdMessageMaxCount })
  }
}
