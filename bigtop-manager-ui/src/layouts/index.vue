<!--
  - Licensed to the Apache Software Foundation (ASF) under one
  - or more contributor license agreements.  See the NOTICE file
  - distributed with this work for additional information
  - regarding copyright ownership.  The ASF licenses this file
  - to you under the Apache License, Version 2.0 (the
  - "License"); you may not use this file except in compliance
  - with the License.  You may obtain a copy of the License at
  -
  -    https://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing,
  - software distributed under the License is distributed on an
  - "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  - KIND, either express or implied.  See the License for the
  - specific language governing permissions and limitations
  - under the License.
  -->

<script setup lang="ts">
  import { onMounted } from 'vue'
  import LayoutFooter from '@/layouts/footer.vue'
  import LayoutContent from '@/layouts/content.vue'
  import LayoutHeader from '@/layouts/header.vue'
  import LayoutSider from '@/layouts/sider.vue'
  import { useUserStore } from '@/store/user'
  import { useClusterStore } from '@/store/cluster'
  import { useServiceStore } from '@/store/service'
  import { useComponentStore } from '@/store/component'
  import { useConfigStore } from '@/store/config'

  const userStore = useUserStore()
  const clusterStore = useClusterStore()
  const serviceStore = useServiceStore()
  const componentStore = useComponentStore()
  const configStore = useConfigStore()

  onMounted(() => {
    userStore.getUserInfo()

    clusterStore.loadClusters()

    console.log('loading...')
    serviceStore.loadServices()
    componentStore.loadHostComponents()
    configStore.loadLatestConfigs()
  })
</script>

<template>
  <a-layout class="layout">
    <layout-sider />
    <a-layout class="layout-inner">
      <layout-header />
      <layout-content />
      <layout-footer />
    </a-layout>
  </a-layout>
</template>

<style scoped lang="scss">
  .layout {
    min-height: 100vh;
    &-inner {
      overflow: auto;
    }
  }
</style>
