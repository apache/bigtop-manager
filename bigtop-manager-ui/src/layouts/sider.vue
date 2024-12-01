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
  import { useRouter } from 'vue-router'
  import { RouteExceptions } from '@/enums'
  import { useMenuStore } from '@/store/menu'
  import type { MenuItem } from '@/store/menu/types'

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
  const menuStore = useMenuStore()
  const emits = defineEmits(['onSiderClick'])

  const toggleActivatedIcon = (menuItem: MenuItem) => {
    const { key, icon } = menuItem
    if (menuStore.isDynamicRouteMatched) {
      return key === RouteExceptions.SPECIAL_ROUTE_PATH ? `${icon}_activated` : icon
    } else {
      return key === siderMenuSelectedKey.value ? `${icon}_activated` : icon
    }
  }

  const addCluster = () => {
    router.push({ name: 'ClusterCreate' })
  }

  const onSiderClick = ({ key }: any) => {
    emits('onSiderClick', key)
  }
</script>

<template>
  <a-layout-sider class="sider">
    <a-menu :selected-keys="[siderMenuSelectedKey]" mode="inline" @select="onSiderClick">
      <template v-for="menuItem in siderMenus" :key="menuItem.key">
        <a-sub-menu
          v-if="menuItem.children && menuItem.name === RouteExceptions.SPECIAL_ROUTE_NAME"
          :key="menuItem.key"
        >
          <template #icon>
            <svg-icon :name="toggleActivatedIcon(menuItem)" />
          </template>
          <template #title>
            <span>{{ $t(menuItem.label) }}</span>
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
            <span>{{ $t(menuItem.label) }}</span>
          </a-menu-item>
        </template>
      </template>
    </a-menu>
    <div v-show="menuStore.isClusterCreateVisible">
      <a-divider />
      <div class="create-option">
        <a-button type="primary" ghost @click="addCluster">
          <svg-icon name="plus" />
          <label>{{ $t('menu.create') }}</label>
        </a-button>
      </div>
    </div>
  </a-layout-sider>
</template>

<style scoped lang="scss">
  @mixin reset-sider-menu {
    width: 100%;
    border-radius: 0;
    padding: 0 0 0 14px !important;
    margin: 4px 0 0 0 !important;
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

    .create-option {
      width: 100%;
      display: flex;
      justify-content: center;
      padding-bottom: $space-lg;
      button {
        width: 160px;
        @include flexbox($align: center);
        label {
          margin-left: 10px;
        }
      }
    }
  }
</style>
