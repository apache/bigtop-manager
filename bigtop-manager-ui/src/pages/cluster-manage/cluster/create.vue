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
  import { useMenuStore } from '@/store/menu'
  import { shallowRef } from 'vue'
  const menuStore = useMenuStore()

  const onSave = () => {
    menuStore.updateSiderMenu()
  }

  const onDel = () => {
    menuStore.updateSiderMenu(true)
  }

  const stepItems = shallowRef([
    {
      title: '集群管理'
    },
    {
      title: '组件信息'
    },
    {
      title: '主机配置'
    },
    {
      title: '创建集群'
    }
  ])
</script>

<template>
  <div class="cluster-create">
    <header-card>
      <a-steps style="margin-inline: 6%" :current="1" :items="stepItems"></a-steps>
    </header-card>
    <div> create & del</div>
    <a-button @click="onSave">create</a-button>
    <a-button @click="onDel">delete</a-button>
  </div>
</template>

<style lang="scss" scoped>
  .cluster-create {
    min-width: 600px;
  }
</style>
