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
  import { MenuFoldOutlined, MenuUnfoldOutlined } from '@ant-design/icons-vue'
  import ClusterInfo from '@/components/cluster-info/index.vue'
  import JobInfo from '@/components/job-info/index.vue'
  import AlertInfo from '@/components/alert-info/index.vue'
  import SelectLang from '@/components/select-lang/index.vue'
  import UserAvatar from '@/components/user-avatar/index.vue'
  import { useUIStore } from '@/store/ui'
  import { storeToRefs } from 'pinia'
  import { useClusterStore } from '@/store/cluster'

  const uiStore = useUIStore()
  const clusterStore = useClusterStore()
  const { siderCollapsed } = storeToRefs(uiStore)
  const { clusters } = storeToRefs(clusterStore)
</script>

<template>
  <a-layout-header class="header">
    <div class="header-left">
      <menu-unfold-outlined
        v-if="siderCollapsed"
        @click="uiStore.changeCollapsed"
      />
      <menu-fold-outlined v-else @click="uiStore.changeCollapsed" />
    </div>
    <div class="header-right">
      <template v-if="clusters.length > 0">
        <cluster-info />
        <job-info />
        <alert-info />
      </template>
      <select-lang />
      <user-avatar />
    </div>
  </a-layout-header>
</template>

<style scoped lang="scss">
  .header {
    @include flexbox($justify: space-between, $align: center);
    // background: #fff;
    padding: 0 1rem;
    height: 48px;

    .header-left {
      font-size: 16px;
      cursor: pointer;
      transition: color 0.3s;
    }

    .header-right {
      @include flexbox($justify: start, $align: center);
    }
  }
</style>
