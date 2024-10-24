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
  import { toRefs } from 'vue'
  import { useRouter, useRoute } from 'vue-router'
  import type { MenuItem } from '@/store/menu'

  interface Props {
    siderMenuSelectedKey: string
    siderMenus: MenuItem[]
  }

  const props = withDefaults(defineProps<Props>(), {
    siderMenuSelectedKey: '',
    siderMenus: () => []
  })

  const { siderMenuSelectedKey, siderMenus } = toRefs(props)
  const router = useRouter()
  const route = useRoute()
  const emits = defineEmits(['onSiderClick'])

  const toggleActivedIcon = (menuItem: MenuItem) => {
    const matchStr = '/:cluster/:id'
    const { key, icon } = menuItem
    const routePath = route.matched.at(-1)?.path

    if (routePath?.includes(matchStr)) {
      return key === routePath.replace(matchStr, '') ? `${icon}_actived` : icon
    } else {
      return key === siderMenuSelectedKey.value ? `${icon}_actived` : icon
    }
  }

  const addCluster = () => {
    router.push({ name: 'ClusterAdd' })
  }

  const onSiderClick = ({ key }: any) => {
    emits('onSiderClick', key)
  }
</script>

<template>
  <a-layout-sider class="sider">
    <a-menu
      :selected-keys="[siderMenuSelectedKey]"
      mode="inline"
      @select="onSiderClick"
    >
      <template v-for="menuItem in siderMenus" :key="menuItem.key">
        <a-sub-menu
          v-if="menuItem.children && menuItem.children.length"
          :key="menuItem.key"
        >
          <template #icon>
            <svg-icon :name="toggleActivedIcon(menuItem)" />
          </template>
          <template #title>
            <span>{{ menuItem.label }}</span>
          </template>
          <a-menu-item v-for="child in menuItem.children" :key="child.key">
            <span>{{ child.label }}</span>
          </a-menu-item>
        </a-sub-menu>
        <template v-else>
          <a-menu-item :key="menuItem.key">
            <template #icon>
              <svg-icon :name="toggleActivedIcon(menuItem)" />
            </template>
            <span>{{ menuItem.label }}</span>
          </a-menu-item>
        </template>
      </template>
    </a-menu>
    <a-divider />
    <div class="add-option">
      <a-button type="primary" ghost @click="addCluster">添加集群</a-button>
    </div>
  </a-layout-sider>
</template>

<style scoped lang="scss">
  .sider {
    width: $layout-header-height;
    background: $layout-sider-bg-color;
    overflow: auto;

    .menu-title-flex {
      @include flexbox($justify: space-between, $align: center);
    }
    .add-option {
      width: 160px;
      display: flex;
      justify-content: center;
      padding-bottom: $space-lg;
    }
  }
</style>
