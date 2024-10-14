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
  import { useI18n } from 'vue-i18n'
  import { storeToRefs } from 'pinia'
  import { ref, createVNode } from 'vue'
  import { type MenuProps, Modal } from 'ant-design-vue'
  import { QuestionCircleOutlined } from '@ant-design/icons-vue'

  import { useClusterStore } from '@/store/cluster'
  import { useServiceStore } from '@/store/service'

  import { type ServiceVO } from '@/api/service/types.ts'
  import { execCommand } from '@/api/command'

  import ServiceAdd from '@/components/service-add/index.vue'
  import Job from '@/components/job-info/job.vue'

  interface Menu {
    key: string
    action: string
    dicText: string
  }

  const menuOps: Menu[] = [
    {
      key: '1',
      action: 'start',
      dicText: 'service.start_all'
    },
    {
      key: '2',
      action: 'stop',
      dicText: 'service.stop_all'
    },
    {
      key: '3',
      action: 'restart',
      dicText: 'service.restart_all'
    }
  ]

  const { t } = useI18n()
  const clusterStore = useClusterStore()
  const serviceStore = useServiceStore()
  const { clusterId } = storeToRefs(clusterStore)
  const { installedServices } = storeToRefs(serviceStore)
  const menuClicked = ref<Menu>()
  const addWindowOpened = ref(false)
  const jobWindowOpened = ref(false)

  const handleMenuClick: MenuProps['onClick'] = (e) => {
    if (e.key === '4') {
      addWindowOpened.value = true
      return
    }
    const menu = menuOps.find((menu) => menu.key == e.key)
    const text = `${menu?.dicText}_services`
    menuClicked.value = menu
    Modal.confirm({
      title: t('common.confirm'),
      icon: createVNode(QuestionCircleOutlined),
      content: t(text),
      centered: true,
      okText: t('common.confirm'),
      cancelText: t('common.cancel'),
      onOk: onOk
    })
  }

  const onOk = async () => {
    try {
      if (!menuClicked.value) {
        return
      }
      await execCommand({
        command: menuClicked.value.action,
        clusterId: clusterId.value,
        commandLevel: 'service',
        serviceCommands: getServiceCommands()
      })
      jobWindowOpened.value = true
    } catch (error) {
      console.log('error :>> ', error)
    }
  }

  const getServiceCommands = () => {
    return installedServices.value.map((service: ServiceVO) => {
      return {
        serviceName: service?.serviceName
      }
    })
  }
</script>

<template>
  <a-dropdown trigger="click" @click.stop>
    <a class="dropdown-text">···</a>
    <template #overlay>
      <a-menu @click="handleMenuClick">
        <a-menu-item v-for="menu in menuOps" :key="menu.key">
          <svg-icon :name="menu.action" />
          <span>{{ $t(menu.dicText) }}</span>
        </a-menu-item>
        <a-menu-divider />
        <a-menu-item key="4">
          <svg-icon name="plus" />
          <span> {{ $t('service.add') }}</span>
        </a-menu-item>
      </a-menu>
    </template>
  </a-dropdown>
  <job v-model:visible="jobWindowOpened" />
  <suspense>
    <service-add v-model:open="addWindowOpened" />
  </suspense>
</template>

<style scoped lang="less">
  .dropdown-text {
    font-size: 24px;
    color: white;
  }
</style>
