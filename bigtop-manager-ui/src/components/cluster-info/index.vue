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
  import { ref } from 'vue'
  import { useClusterStore } from '@/store/cluster'
  import { storeToRefs } from 'pinia'
  import ClusterCreate from '@/components/cluster-create/index.vue'

  const clusterStore = useClusterStore()
  const { selectedCluster } = storeToRefs(clusterStore)

  const createWindowOpened = ref(false)
</script>

<template>
  <a-dropdown placement="bottom">
    <div class="icon">
      <div class="name">{{ selectedCluster?.clusterName }}</div>
    </div>
    <template #overlay>
      <a-menu>
        <a-menu-item key="switch">
          {{ $t('cluster.switch') }}
        </a-menu-item>
        <a-menu-item key="create" @click="() => (createWindowOpened = true)">
          {{ $t('cluster.create') }}
        </a-menu-item>
      </a-menu>
    </template>
  </a-dropdown>

  <cluster-create v-model:open="createWindowOpened" />
</template>

<style lang="scss" scoped>
  .icon {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 0 0.5rem;
    border-radius: 6px;
    cursor: pointer;
    height: 36px;

    &:hover {
      background-color: var(--hover-color);
    }

    .name {
      font-size: 14px;
    }
  }
</style>
