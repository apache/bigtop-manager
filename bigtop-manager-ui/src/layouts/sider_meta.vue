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
  import { computed, onMounted, ref, watch } from 'vue'
  import { useUIStore } from '@/store/ui'
  import { useUserStore } from '@/store/user'
  import { storeToRefs } from 'pinia'
  import { RouterLink, useRouter } from 'vue-router'
  import ServiceDropdown from '@/components/service/service-dropdown.vue'

  const uiStore = useUIStore()
  const userStore = useUserStore()
  const router = useRouter()

  const { siderCollapsed } = storeToRefs(uiStore)
  const { menuItems } = storeToRefs(userStore)

  const selectedKeys = ref<string[]>([])
  const openKeys = ref<string[]>([])

  const siderMenu = computed(() =>
    menuItems.value
      .filter((menuItem) => !menuItem.hidden)
      .sort((pre, next) => (pre.priority ?? 0) - (next.priority ?? 0))
  )

  const updateSideBar = () => {
    const splitPath = router.currentRoute.value.path.split('/')
    const selectedKey = splitPath[splitPath.length - 1]
    selectedKeys.value = [selectedKey]
    if (splitPath.length > 2) {
      openKeys.value = [splitPath[1]]
    } else {
      openKeys.value = []
    }
  }

  watch(router.currentRoute, () => {
    updateSideBar()
  })

  onMounted(async () => {
    updateSideBar()
  })
</script>

<template>
  <a-layout-sider v-model:collapsed="siderCollapsed" class="sider" width="235">
    <div class="header">
      <img class="header-logo" src="@/assets/logo.svg" alt="logo" />
      <div v-if="!siderCollapsed" class="header-title">Bigtop Manager</div>
    </div>
    <a-menu
      v-model:selectedKeys="selectedKeys"
      v-model:open-keys="openKeys"
      theme="dark"
      mode="inline"
    >
      <template v-for="item in siderMenu">
        <template v-if="item.children">
          <a-sub-menu :key="item.key">
            <template #title>
              <div v-if="item.title === 'Services'" class="menu-title-flex">
                <span>
                  <component :is="() => item.icon" />
                  <span>
                    {{ item.title }}
                  </span>
                </span>
                <service-dropdown />
              </div>
              <span v-else>
                <component :is="() => item.icon" />
                <span>
                  {{ item.title }}
                </span>
              </span>
            </template>
            <a-menu-item v-for="subItem in item.children" :key="subItem.key">
              <component :is="() => subItem.icon" />
              <span>
                <router-link :to="subItem.to">{{ subItem.title }}</router-link>
              </span>
            </a-menu-item>
          </a-sub-menu>
        </template>
        <template v-else>
          <a-menu-item :key="item.key">
            <component :is="() => item.icon" />
            <span>
              <router-link :to="item.to">{{ item.title }}</router-link>
            </span>
          </a-menu-item>
        </template>
      </template>
    </a-menu>
  </a-layout-sider>
</template>

<style scoped lang="scss">
  .sider {
    .header {
      @include flexbox($justify: center,$align: center);
      height: 32px;
      margin: 1rem;

      .header-logo {
        height: 32px;
        width: 32px;
      }

      .header-title {
        color: #ccc;
        font-weight: bold;
        font-size: 16px;
        margin-left: 1rem;
      }
    }

    .menu-title-flex {
     @include flexbox( $justify: space-between, $align: center);
    }
  }
</style>
