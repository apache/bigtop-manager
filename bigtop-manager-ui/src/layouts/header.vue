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
  import SelectLang from '@/components/select-lang/index.vue'
  import UserAvatar from '@/components/user-avatar/index.vue'
  import { computed, ref } from 'vue'
  import {dynamicRoutes} from '@/router/routes/index';
  import { RouteRecordRaw } from 'vue-router';

  const spaceSize = ref(20)
  const selectedKeys = ref()

  const headerMenus = computed(():Map<string,RouteRecordRaw[]> => {
    let map = new Map()
      dynamicRoutes.forEach((route) => {
      let navName = route.meta?.belong
      if(navName){
        const existingNav = map.get(navName) || [];
        map.set(navName, [...existingNav,route]);
      }
     })
     console.log('map :>> ', map);
     return map
  })


</script>

<template>
  <a-layout-header class="header">
    <h1 class="header-left common-layout">
      <svg-icon name="big-manager-logo" />
    </h1>
    <div class="header-menu">
      <a-menu
        v-model:selectedKeys="selectedKeys"
        theme="dark"
        mode="horizontal"
      >
        <a-menu-item v-for="([key, value]) of headerMenus.entries()"  :key="key">{{key}}</a-menu-item>
      </a-menu>
    </div>
    <div class="header-right common-layout">
      <a-space :size="spaceSize">
        <user-avatar />
        <div class="header-item">
          <svg-icon name="communication" />
        </div>
        <select-lang />
        <div class="header-item">
          <svg-icon name="github" />
        </div>
        <div class="header-item">
          <svg-icon name="book" />
        </div>
      </a-space>
    </div>
  </a-layout-header>
</template>

<style scoped lang="scss">
  .common-layout {
    @include flexbox($justify: center, $align: center);
    height: 100%;
  }
  .header {
    @include flexbox($justify: space-between, $align: center);
    padding: 0 24px 0 10px;
    height: $layout-header-height;
    .header-menu{
      flex: 1;
    }
    .header-left {
      :deep(.svg-icon) {
        width: 180px;
        height: 30px;
      }
    }

    nav{
      color: $color-white;
    }
  }
</style>
