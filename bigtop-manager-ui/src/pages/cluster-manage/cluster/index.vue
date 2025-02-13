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
  import { computed, ref } from 'vue'
  import { useRoute } from 'vue-router'
  import type { GroupItem } from '@/components/common/button-group/types'
  import type { TabItem } from '@/components/common/main-card/types'
  import Overview from './overview.vue'
  import Service from './service.vue'
  import Host from './host.vue'
  import User from './user.vue'
  import Job from '@/components/job/index.vue'

  const route = useRoute()
  const title = computed(() => route.params.cluster as string)
  const desc = ref('我是描述')
  const activeKey = ref('1')
  const tabs = ref<TabItem[]>([
    {
      key: '1',
      title: '概览'
    },
    {
      key: '2',
      title: '服务'
    },
    {
      key: '3',
      title: '主机'
    },
    {
      key: '4',
      title: '用户'
    },
    {
      key: '5',
      title: '作业'
    }
  ])
  const actionGroup = computed<GroupItem[]>(() => [
    {
      shape: 'default',
      type: 'primary',
      text: '添加服务',
      clickEvent: () => addService && addService()
    },
    {
      shape: 'default',
      type: 'default',
      text: '其他操作',
      dropdownMenu: [
        {
          action: 'start',
          text: '启动集群'
        },
        {
          action: 'restart',
          text: '重启集群'
        },
        {
          action: 'stop',
          text: '停止集群'
        }
      ],
      dropdownMenuClickEvent: (info) => dropdownMenuClick && dropdownMenuClick(info)
    }
  ])

  const getCompName = computed(() => {
    const componnts = [Overview, Service, Host, User, Job]
    return componnts[parseInt(activeKey.value) - 1]
  })

  const dropdownMenuClick: GroupItem['dropdownMenuClickEvent'] = ({ key }) => {
    console.log('key :>> ', key)
  }

  const addService: GroupItem['clickEvent'] = () => {
    console.log('add :>> ')
  }
</script>

<template>
  <header-card :title="title" avatar="cluster" :desc="desc" :action-groups="actionGroup" />
  <main-card v-model:active-key="activeKey" :tabs="tabs">
    <template #tab-item>
      <keep-alive>
        <component :is="getCompName"></component>
      </keep-alive>
    </template>
  </main-card>
</template>

<style lang="scss" scoped></style>
