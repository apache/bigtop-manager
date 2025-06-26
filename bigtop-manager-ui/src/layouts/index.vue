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
  import LayoutFooter from '@/layouts/footer.vue'
  import LayoutHeader from '@/layouts/header.vue'
  import LayoutSider from '@/layouts/sider.vue'
</script>

<template>
  <a-layout class="layout">
    <layout-header />
    <a-layout>
      <layout-sider />
      <a-layout class="layout-inner">
        <router-view :key="$route.fullPath" />
        <layout-footer />
      </a-layout>
    </a-layout>
  </a-layout>
</template>

<style lang="scss" scoped>
  .layout {
    height: 100vh;

    &-inner {
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      padding: $space-lg $space-md;
      overflow: auto;
    }
  }
</style>
