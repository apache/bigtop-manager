<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements.  See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership.  The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License.  You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
-->

<script setup lang="ts">
  import { TabsProps } from 'ant-design-vue'
  import { TabItem } from './types'

  interface Props {
    activeKey: string
    tabs: TabItem[]
  }

  withDefaults(defineProps<Props>(), {
    tabs: () => {
      return []
    }
  })

  const emits = defineEmits(['update:activeKey'])

  const handleChange: TabsProps['onChange'] = (activeKey) => {
    emits('update:activeKey', activeKey)
  }
</script>

<template>
  <div class="main-card">
    <a-tabs v-mode:active-key="activeKey" :tab-bar-gutter="0" @change="handleChange">
      <a-tab-pane v-for="tab in tabs" :key="tab.key" :tab="tab.title">
        <slot></slot>
      </a-tab-pane>
      <template #renderTabBar="{ DefaultTabBar, ...props }">
        <component :is="DefaultTabBar" v-bind="props" />
      </template>
    </a-tabs>
  </div>
</template>

<style lang="scss" scoped>
  .main-card {
    background-color: $color-white;
    padding-inline: $space-md;
    padding-bottom: $space-md;
    :deep(.ant-tabs-tab) {
      padding-inline: 16px;
    }
  }
</style>
