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

import { createApp } from 'vue'
import App from './App.vue'
import plugins from '@/plugins'

import 'ant-design-vue/dist/reset.css'
import 'github-markdown-css/github-markdown.css'
import 'highlight.js/styles/github-dark.css'
import '@/styles/scrollbar.scss'
import '@/styles/index.scss'

const app = createApp(App)
app.use(plugins, { antdMessageMaxCount: 1 })
app.mount('#app')
