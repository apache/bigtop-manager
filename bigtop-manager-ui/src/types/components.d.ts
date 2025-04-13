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
export {}

declare module 'vue' {
  export interface GlobalComponents {
    HeaderCard: (typeof import('@/components/common/header-card/index.vue'))['default']
    MainCard: (typeof import('@/components/common/main-card/index.vue'))['default']
    AutoForm: (typeof import('@/components/common/auto-form/index.vue'))['default']
    ButtonGroup: (typeof import('@/components/common/button-group/index.vue'))['default']
    MarkdownView: (typeof import('@/components/common/markdown-view/index.vue'))['default']
    StatusDot: (typeof import('@/components/common/status-dot/index.vue'))['default']
  }
}
