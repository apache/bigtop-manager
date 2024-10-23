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
  import { RouteRecordRaw } from 'vue-router'
  import { useRouter } from 'vue-router'

  interface Props {
    sideMenuSelectedKey: string
    siderMenus: RouteRecordRaw[]
  }

  const props = withDefaults(defineProps<Props>(), {
    sideMenuSelectedKey: '',
    siderMenus: () => []
  })
  const { sideMenuSelectedKey, siderMenus } = toRefs(props)
  const router = useRouter()
  const emits = defineEmits(['onSiderClick'])

  const addCluster = () => {
    router.push({ name: 'AddClusters' })
    onSiderClick({ key: '/cluster-mange/add' })
  }

  const onSiderClick = ({ key }: any) => {
    emits('onSiderClick', key)
  }
</script>

<template>
  <a-layout-sider class="sider">
    <a-menu
      :selected-keys="[sideMenuSelectedKey]"
      mode="inline"
      @select="onSiderClick"
    >
      <template v-for="route in siderMenus">
        <a-sub-menu
          v-if="route.children"
          :key="route.path"
          :title="route.meta?.title"
        >
          <a-menu-item
            v-for="child in route.children"
            :key="child.path"
            :path="child.path"
          >
            {{ child.meta?.title }}
          </a-menu-item>
        </a-sub-menu>
        <template v-else>
          <a-menu-item v-if="!route.meta?.hidden" :key="route.path">
            {{ route.meta?.title }}
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
