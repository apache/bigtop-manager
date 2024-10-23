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
  import { onMounted } from 'vue'
  import LayoutFooter from '@/layouts/footer.vue'
  import LayoutHeader from '@/layouts/header.vue'
  import LayoutSider from '@/layouts/sider.vue'
  import { useUserStore } from '@/store/user'
  import { useClusterStore } from '@/store/cluster'
  import { useMenuStore } from '@/store/menu/index'
  import { storeToRefs } from 'pinia'

  const userStore = useUserStore()
  const menuStore = useMenuStore()
  const clusterStore = useClusterStore()
  const { headerSelectedKey, headerMenus, siderMenuSelectedKey, siderMenus } =
    storeToRefs(menuStore)

  onMounted(async () => {
    userStore.getUserInfo()
    await clusterStore.loadClusters()
    menuStore.setBaseRoutesMap()
    menuStore.setupDynamicRoutes()
  })
</script>

<template>
  <a-layout class="layout">
    <layout-header
      :header-selected-key="headerSelectedKey"
      :header-menus="headerMenus"
      @on-header-click="menuStore.onHeaderClick"
    />
    <a-layout>
      <layout-sider
        :side-menu-selected-key="siderMenuSelectedKey"
        :sider-menus="siderMenus"
        @on-sider-click="menuStore.onSiderClick"
      />
      <a-layout class="layout-inner">
        <router-view />
        <layout-footer />
      </a-layout>
    </a-layout>
  </a-layout>
</template>

<style lang="scss" scoped>
  .layout {
    height: 100vh;
    &-inner {
      padding: $space-lg $space-md;
      overflow: auto;
    }
  }
</style>
