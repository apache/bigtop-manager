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
  import { useMenuStore } from '@/store/menu/index'
  import { useClusterStore } from '@/store/cluster'
  import { useStackStore } from '@/store/stack'

  import type { ClusterStatusType } from '@/api/cluster/types'

  const { t } = useI18n()
  const router = useRouter()
  const route = useRoute()
  const stackStore = useStackStore()
  const menuStore = useMenuStore()
  const clusterStore = useClusterStore()
  const routeParamsLen = ref(0)
  const openKeys = ref<string[]>(['clusters'])

  const { siderMenuSelectedKey, headerSelectedKey, siderMenus, routePathFromClusters } = storeToRefs(menuStore)
  const { clusterCount, clusterMap } = storeToRefs(clusterStore)

  const showCreateClusterBtn = computed(() => headerSelectedKey.value === '/cluster-manage')
  const selectMenuKeyFromClusters = computed(() => siderMenuSelectedKey.value.includes(routePathFromClusters.value))
  const clusterStatus = computed(
    (): Record<ClusterStatusType, string> => ({
      1: 'success',
      2: 'error',
      3: 'warning'
    })
  )

  /**
   * Handles route changes and updates the selected menu key.
   */
  const handleRouteChange = async (newRoute: typeof route) => {
    const token = localStorage.getItem('Token') ?? sessionStorage.getItem('Token') ?? undefined
    const { params, path, meta, name: RouteName } = newRoute

    if (!token) {
      return
    }

    routeParamsLen.value = Object.keys(params).length

    if (path.includes(routePathFromClusters.value) && RouteName !== 'CreateCluster') {
      if (params.id && clusterMap.value[`${params.id}`]) {
        siderMenuSelectedKey.value = `${routePathFromClusters.value}/${params.id}`
        openKeys.value.push(siderMenuSelectedKey.value)
        return
      }

      siderMenuSelectedKey.value = ''
      menuStore.setupSider()
      return
    }

    siderMenuSelectedKey.value = meta.activeMenu ?? path
  }

  /**
   * Toggles the activated icon for menu items.
   */
  const toggleActivatedIcon = (menuItem: { key: string; icon: string }) => {
    const { key } = menuItem
    const matchedRouteFromClusters = selectMenuKeyFromClusters.value && routeParamsLen.value > 0
    return key === siderMenuSelectedKey.value || (matchedRouteFromClusters && key === 'clusters')
  }

  const addCluster = () => {
    router.push({ name: 'CreateCluster' })
  }

  onMounted(() => {
    stackStore.loadStacks()
    clusterStore.loadClusters()
  })

  watch(
    () => [route, clusterCount],
    (val) => {
      const [newRoute] = val
      handleRouteChange(newRoute as typeof route)
    },
    { deep: true, immediate: true }
  )
</script>

<template>
  <a-layout-sider class="sider">
    <a-menu
      :open-keys="openKeys"
      :selected-keys="[siderMenuSelectedKey]"
      mode="inline"
      @click="({ key }) => menuStore.onSiderClick(key as string)"
    >
      <template v-for="menuItem in siderMenus" :key="menuItem.path">
        <a-sub-menu v-if="menuItem.name === 'Clusters'" :key="menuItem.path">
          <template #icon>
            <svg-icon
              color="inherit"
              style="height: 16px; width: 16px"
              :highlight="toggleActivatedIcon({ key: menuItem.path, icon: menuItem.meta?.icon || '' })"
              :name="menuItem.meta?.icon || ''"
            />
          </template>
          <template #title>
            <span>{{ t(menuItem.meta!.title!) }}</span>
          </template>
          <a-menu-item
            v-for="child of clusterMap"
            :key="`${routePathFromClusters}/${child.id}`"
            :title="child.displayName"
          >
            <template #icon>
              <div class="cluster-status">
                <status-dot :size="8" :color="clusterStatus[child.status as ClusterStatusType] as any" />
              </div>
            </template>
            <div>
              <span>{{ child.displayName }}</span>
            </div>
          </a-menu-item>
        </a-sub-menu>
        <template v-else>
          <a-menu-item v-if="!menuItem.meta?.hidden" :key="menuItem.redirect as string">
            <template #icon>
              <svg-icon
                style="height: 16px; width: 16px"
                :highlight="toggleActivatedIcon({ key: menuItem.redirect as string, icon: menuItem.meta?.icon || '' })"
                :name="menuItem.meta?.icon || ''"
              />
            </template>
            <span>{{ t(menuItem.meta!.title!) }}</span>
          </a-menu-item>
        </template>
      </template>
    </a-menu>
    <div v-if="showCreateClusterBtn">
      <a-divider />
      <div class="create-option">
        <a-button type="primary" ghost @click="addCluster">
          <div>
            <label>{{ t('menu.create') }}</label>
          </div>
        </a-button>
      </div>
    </div>
  </a-layout-sider>
</template>

<style scoped lang="scss">
  .cluster-status {
    height: 10px;
    margin-inline: 7px;
    display: flex;
    justify-content: center;
    align-items: flex-end;
  }
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
