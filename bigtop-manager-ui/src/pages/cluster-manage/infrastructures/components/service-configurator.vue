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
  import { computed, onActivated, ref } from 'vue'
  import Sidebar from './sidebar.vue'
  import type { ServiceConfigReq } from '@/api/command/types'

  // interface Props {
  //   stepData: any
  // }

  // const props = defineProps<Props>()
  // const emits = defineEmits(['update:stepData'])

  const searchStr = ref('')
  const activeKey = ref<number[]>([])
  const configs = ref<ServiceConfigReq[]>([])
  const layout = computed(() => ({
    labelCol: {
      xs: { span: 24 },
      sm: { span: 8 },
      md: { span: 8 },
      lg: { span: 6 }
    },
    wrapperCol: {
      xs: { span: 24 },
      sm: { span: 16 },
      lg: { span: 18 }
    }
  }))

  const createNewConfigItem = () => {
    return {
      name: '',
      displayName: '',
      value: '',
      isManual: true
    }
  }

  const manualAddConfig = (config: ServiceConfigReq) => {
    config.properties?.push(createNewConfigItem())
  }

  onActivated(() => {
    configs.value = getConfigsData()
    activeKey.value = configs.value.map((v: any) => v.id)
  })

  const getConfigsData = () => {
    return Array.from({ length: 3 }, (_v, k) => ({
      id: k,
      name: 'zookeeper-env',
      properties: [
        {
          name: 'zookeeper_log_dir',
          displayName: 'zookeeper_log_dir',
          value: ''
        },
        {
          name: 'zookeeper-env template',
          displayName: 'zookeeper-env template',
          value: ''
        },
        {
          type: 'textarea',
          name: 'zookeeper-env template',
          displayName: 'zookeeper-env template',
          value: ''
        }
      ].map((v) => ({ ...v, isManual: false }))
    })) as ServiceConfigReq[]
  }
</script>

<template>
  <div class="service-configurator">
    <section>
      <div class="list-title">
        <div>{{ $t('service.service_list') }}</div>
      </div>
      <sidebar :data="[]" />
    </section>
    <a-divider type="vertical" class="divider" />
    <section>
      <div class="list-title">
        <div>{{ $t('service.host_preview') }}</div>
        <a-input v-model:value="searchStr" :placeholder="$t('service.please_enter_search_keyword')" />
      </div>
      <a-form>
        <a-collapse v-model:active-key="activeKey" :bordered="false" :ghost="true">
          <a-collapse-panel v-for="config in configs" :key="config.id">
            <template #extra>
              <a-button type="text" shape="circle" @click.stop="manualAddConfig(config)">
                <template #icon>
                  <svg-icon name="plus_dark" />
                </template>
              </a-button>
            </template>
            <template #header>
              <span>{{ config.name }}</span>
            </template>
            <a-row v-for="(item, idx) in config.properties" :key="idx" :gutter="[16, 0]">
              <a-col v-bind="layout.labelCol">
                <a-form-item>
                  <a-textarea v-if="item.isManual" v-model:value="item.name" :auto-size="{ minRows: 1, maxRows: 5 }" />
                  <span v-else>{{ item.displayName ?? item.name }}</span>
                </a-form-item>
              </a-col>
              <a-col v-bind="layout.wrapperCol">
                <a-form-item>
                  <a-textarea v-model:value="item.value" :auto-size="{ minRows: 1, maxRows: 5 }" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-collapse-panel>
        </a-collapse>
      </a-form>
    </section>
  </div>
</template>

<style lang="scss" scoped>
  .service-configurator {
    display: grid;
    grid-template-columns: 1fr auto 4fr;

    :deep(.ant-collapse-header) {
      display: flex;
      align-items: center;
      background-color: $color-fill-quaternary;
      border-bottom: 1px solid $color-border-secondary;
    }
    :deep(.ant-collapse-content-box) {
      padding-inline: 24px !important;
    }

    .ant-form {
      max-height: 500px;
      overflow: auto;
    }

    .list-title {
      display: flex;
      height: 32px;
      align-items: center;
      justify-content: space-between;
      font-weight: 500;
      border-bottom: 1px solid $color-border;
      padding-bottom: 16px;
      margin-bottom: 16px;
      .ant-input {
        flex: 0 1 160px;
      }
    }
    .divider {
      height: 100%;
      margin-inline: 16px;
    }
  }
</style>
