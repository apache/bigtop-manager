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
  import { PlusOutlined, CaretRightOutlined } from '@ant-design/icons-vue'
  import SquareFilled from '@/components/icons/square-filled.vue'
  import ServiceAdd from '@/components/service-add/index.vue'

  const addWindowOpened = ref(false)
</script>

<template>
  <a-dropdown trigger="click" @click.stop>
    <a class="dropdown-text">···</a>
    <template #overlay>
      <a-menu>
        <a-menu-item key="start">
          <caret-right-outlined />
          {{ $t('service.start_all') }}
        </a-menu-item>
        <a-menu-item key="stop">
          <square-filled />
          {{ $t('service.stop_all') }}
        </a-menu-item>
        <a-menu-divider />
        <a-menu-item key="add" @click="() => (addWindowOpened = true)">
          <plus-outlined />
          {{ $t('service.add') }}
        </a-menu-item>
      </a-menu>
    </template>
  </a-dropdown>

  <suspense>
    <service-add v-model:open="addWindowOpened" />
  </suspense>
</template>

<style scoped lang="scss">
  .dropdown-text {
    font-size: 24px;
    color: white;
  }
</style>
