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
  import { onMounted, ref, watch } from 'vue'
  import { useUIStore } from '@/store/ui'
  import { useMenuStore } from '@/store/menu/index'
  import { storeToRefs } from 'pinia'
  import { useRouter } from 'vue-router'

  const uiStore = useUIStore()
  const menuStore = useMenuStore()
  const router = useRouter()

  const { siderCollapsed } = storeToRefs(uiStore)
  const { siderMenus } = storeToRefs(menuStore)

  const selectedKeys = ref<string[]>([])
  const openKeys = ref<string[]>([])

  const updateSideBar = () => {
    const splitPath = router.currentRoute.value.path.split('/')
    const [len, isClusters] = [splitPath.length, splitPath.includes('clusters')]
    selectedKeys.value = [splitPath[isClusters ? len - 2 : len - 1]]

    if (splitPath.length > 2) {
      openKeys.value = [splitPath[3]]
    } else {
      openKeys.value = []
    }
  }

  const onSelect = ({ item }: any) => {
    router.push({ path: item.to })
  }

  watch(router.currentRoute, (val) => {
    console.log('val :>> ', val)
    updateSideBar()
  })

  onMounted(async () => {
    updateSideBar()
  })
</script>

<template>
  <a-layout-sider v-model:collapsed="siderCollapsed" class="sider">
    <a-menu
      v-model:selectedKeys="selectedKeys"
      v-model:open-keys="openKeys"
      mode="inline"
      :items="siderMenus"
      @select="onSelect"
    >
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
