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

  const toggleActivatedIcon = (menuItem: MenuItem) => {
    const matchStr = '/:cluster/:id'
    const { key, icon } = menuItem
    const routePath = route.matched.at(-1)?.path
    if (routePath?.includes(matchStr)) {
      return key === routePath.replace(matchStr, '')
        ? `${icon}_activated`
        : icon
    } else {
      return key === siderMenuSelectedKey.value ? `${icon}_activated` : icon
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
            <svg-icon :name="toggleActivatedIcon(menuItem)" />
          </template>
          <template #title>
            <span>{{ menuItem.label }}</span>
          </template>
          <a-menu-item v-for="child in menuItem.children" :key="child.key">
            <span style="margin: 0 6px">{{ child.icon }}</span>
            <span>{{ child.label }}</span>
          </a-menu-item>
        </a-sub-menu>
        <template v-else>
          <a-menu-item :key="menuItem.key">
            <template #icon>
              <svg-icon :name="toggleActivatedIcon(menuItem)" />
            </template>
            <span>{{ menuItem.label }}</span>
          </a-menu-item>
        </template>
      </template>
    </a-menu>
    <a-divider />
    <div class="add-option">
      <a-button type="primary" ghost @click="addCluster">
        <svg-icon name="plus" />
        <label>添加集群</label>
      </a-button>
    </div>
  </a-layout-sider>
</template>

<style scoped lang="scss">
  @mixin reset-sider-menu {
    width: 100%;
    border-radius: 0;
    margin-inline: 0 !important;
    padding: 0 0 0 14px !important;
  }
  .sider {
    width: $layout-header-height;
    background: $layout-sider-bg-color;
    overflow: auto;

    :deep(.ant-menu-submenu-title) {
      @include reset-sider-menu();
    }

    :deep(.ant-menu-item) {
      @include reset-sider-menu();
    }

    :deep(.ant-menu-item-selected) {
      border-right: 2px solid $color-primary;
    }

    .add-option {
      width: 100%;
      display: flex;
      justify-content: center;
      padding-bottom: $space-lg;
      button {
        width: 160px;
        label {
          margin-left: 6px;
        }
      }
    }
  }
</style>
