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
  import { useNavigation } from '@/composables/use-menu'
  import { computed, watch } from 'vue'

  const { headerSelectedKey, headerMenus, sideMenuSelectedKey, onSiderClick } =
    useNavigation()

  const siderMenus = computed(() => {
    const res = headerMenus.value.filter(
      (v) => v.path === headerSelectedKey.value
    )
    return res[0]?.children ? res[0].children : []
  })

  watch(siderMenus, (value) => {
    console.log('value :>> ', value)
  })
</script>

<template>
  <a-layout-sider class="sider">
    <a-menu
      :selected-keys="[sideMenuSelectedKey]"
      mode="inline"
      @select="onSiderClick"
    >
      <template v-for="route in siderMenus">
        <a-sub-menu
          v-if="route.children"
          :key="route.path"
          :title="route.meta?.title"
        >
          <a-menu-item
            v-for="child in route.children"
            :key="child.path"
            :path="child.path"
          >
            {{ child.meta?.title }}
          </a-menu-item>
        </a-sub-menu>
        <template v-else>
          <a-menu-item :key="route.path">
            {{ route.meta?.title }}
          </a-menu-item>
        </template>
      </template>
    </a-menu>
  </a-layout-sider>
</template>

<style scoped lang="scss">
  .sider {
    width: $layout-header-height;
    background: $layout-sider-bg-color;

    .menu-title-flex {
      @include flexbox($justify: space-between, $align: center);
    }
  }
</style>
