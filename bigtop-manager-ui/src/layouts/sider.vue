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
  import { useRouter, useRoute } from 'vue-router'
  import { useMenuStore } from '@/store/menu/index'
  import { storeToRefs } from 'pinia'
  import { watch } from 'vue'

  const router = useRouter()
  const route = useRoute()
  const menuStore = useMenuStore()
  const { siderMenuSelectedKey, siderMenus } = storeToRefs(menuStore)

  watch(
    () => route,
    (newRoute) => {
      siderMenuSelectedKey.value = newRoute.meta.activeMenu ?? newRoute.path
    },
    {
      deep: true,
      immediate: true
    }
  )

  const addCluster = () => {
    router.push({ name: 'CreateCluster' })
  }

  const toggleActivatedIcon = (menuItem: { key: string; icon: string }) => {
    const { key, icon } = menuItem
    // if (menuStore) {
    //   return key === RouteExceptions.SPECIAL_ROUTE_PATH ? `${icon}_activated` : icon
    // } else {
    // }
    return key === siderMenuSelectedKey.value ? `${icon}_activated` : icon
  }

  // const clusterStatus = ref<Record<ClusterStatusType, string>>({
  //   1: 'success',
  //   2: 'error',
  //   3: 'warning'
  // })
</script>

<template>
  <a-layout-sider class="sider">
    <a-menu :selected-keys="[siderMenuSelectedKey]" mode="inline" @select="({ key }) => menuStore.onSiderClick(key)">
      <template v-for="menuItem in siderMenus" :key="menuItem.path">
        <a-sub-menu v-if="menuItem?.children && menuItem.name === 'Clusters'" :key="menuItem.redirect">
          <template #icon>
            <svg-icon
              style="height: 16px; width: 16px"
              :name="toggleActivatedIcon({ key: menuItem.redirect as string, icon: menuItem.meta?.icon || '' })"
            />
          </template>
          <template #title>
            <span>{{ $t(menuItem.meta!.title!) }}</span>
          </template>
          <template v-for="child in menuItem.children" :key="child.name">
            <a-menu-item v-if="!child.meta?.hidden" :key="child.name">
              <template #icon>
                <div
                  style="
                    height: 10px;
                    margin-inline: 7px;
                    display: flex;
                    justify-content: center;
                    align-items: flex-end;
                  "
                >
                  <!-- <status-dot :size="8" :color="clusterStatus[child.status as ClusterStatusType] as any" /> -->
                </div>
              </template>
              <div>
                <span>{{ child.name }}</span>
              </div>
            </a-menu-item>
          </template>
        </a-sub-menu>
        <template v-else>
          <a-menu-item v-if="!menuItem.meta?.hidden" :key="menuItem.redirect">
            <template #icon>
              <svg-icon
                style="height: 16px; width: 16px"
                :name="toggleActivatedIcon({ key: menuItem.redirect as string, icon: menuItem.meta?.icon || '' })"
              />
            </template>
            <span>{{ $t(menuItem.meta!.title!) }}</span>
          </a-menu-item>
        </template>
      </template>
    </a-menu>
    <div>
      <a-divider />
      <div class="create-option">
        <a-button type="primary" ghost @click="addCluster">
          <div>
            <label>{{ $t('menu.create') }}</label>
          </div>
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
        @include flexbox($align: center, $justify: center);
        label {
          cursor: pointer;
        }
      }
    }
  }
</style>
