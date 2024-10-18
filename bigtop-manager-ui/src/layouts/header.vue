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
  import { onMounted, ref, toRaw } from 'vue'
  import { useRouter } from 'vue-router'
  import { useMenuStore } from '@/store/menu'
  import { useClusterStore } from '@/store/cluster'
  import SelectLang from '@/components/select-lang/index.vue'
  import UserAvatar from '@/components/user-avatar/index.vue'

  const router = useRouter()
  const menuStore = useMenuStore()
  const clusterStore = useClusterStore()
  const spaceSize = ref(16)
  const selectedKeys = ref<string[]>([
    router.currentRoute.value.matched[0].path
  ])

  const onSelect = async () => {
    const isCluster = selectedKeys.value[0] == '/cluster-mange/'
    if (isCluster) {
      await clusterStore.loadClusters()
    }
    const cluster = clusterStore.clusters[0]
    const path = isCluster
      ? `${selectedKeys.value[0]}clusters/${cluster?.clusterName}/${cluster.id}`
      : selectedKeys.value[0]
    menuStore.updateSiderRoutes(toRaw(selectedKeys.value), path)
  }

  onMounted(() => {
    menuStore.updateSiderRoutes(
      toRaw(selectedKeys.value),
      router.currentRoute.value.fullPath
    )
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
        @select="onSelect"
      >
        <a-menu-item
          v-for="key of menuStore.headerMenus?.keys()"
          :key="key.split('_')[0]"
        >
          {{ key.split('_')[1] }}
        </a-menu-item>
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
    padding-inline: 0 16px;
    height: $layout-header-height;
    .header-menu {
      flex: 1;
    }
    .header-left {
      width: $layout-sider-width;
      :deep(.svg-icon) {
        width: 180px;
        height: 30px;
      }
    }

    nav {
      color: $color-white;
    }
  }
</style>
