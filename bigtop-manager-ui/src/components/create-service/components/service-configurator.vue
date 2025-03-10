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
  import { ref, shallowRef } from 'vue'
  import { Empty } from 'ant-design-vue'
  import TreeSelector from './tree-selector.vue'
  import useCreateService from './use-create-service'
  import type { ServiceConfigReq } from '@/api/command/types'
  import type { ComponentVO } from '@/api/component/types'
  import type { Key } from 'ant-design-vue/es/_util/type'

  interface Props {
    isView?: boolean
  }

  withDefaults(defineProps<Props>(), {
    isView: false
  })

  const { selectedServices } = useCreateService()
  const searchStr = ref('')
  const currService = ref<Key>('')
  const activeKey = ref<number[]>([])
  const configs = ref<ServiceConfigReq[]>([])
  const hostPreviewList = ref<ComponentVO[]>([])
  const fieldNames = shallowRef({
    title: 'displayName',
    key: 'name'
  })
  const layout = shallowRef({
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
  })

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

  const handleChange = (expandSelectedKeyPath: string) => {
    currService.value = expandSelectedKeyPath
    const index = selectedServices.value.findIndex((v) => v.name === expandSelectedKeyPath.split('/').at(-1))
    if (index !== -1) {
      const temp = selectedServices.value[index]
      configs.value = temp.configs as ServiceConfigReq[]
      hostPreviewList.value = temp.components as ComponentVO[]
    } else {
      configs.value = []
      hostPreviewList.value = []
    }
  }
</script>

<template>
  <div class="service-configurator" :class="{ 'service-configurator-view': $props.isView }">
    <section>
      <div class="list-title">
        <div>{{ $t('service.service_list') }}</div>
      </div>
      <tree-selector :tree="selectedServices" :field-names="fieldNames" @change="handleChange" />
    </section>
    <a-divider type="vertical" class="divider" />
    <section>
      <div class="list-title">
        <div>{{ $t('service.host_preview') }}</div>
        <a-input v-model:value="searchStr" :placeholder="$t('service.please_enter_search_keyword')" />
      </div>
      <a-empty v-if="configs.length === 0" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
      <a-form v-else :disabled="$props.isView">
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
    <template v-if="isView">
      <a-divider type="vertical" class="divider" />
      <section>
        <div class="list-title">
          <div>{{ $t('service.host_preview') }}</div>
        </div>
        <tree-selector
          :tree="hostPreviewList"
          :selectable="false"
          :field-names="{ ...fieldNames, children: 'hosts' }"
        />
      </section>
    </template>
  </div>
</template>

<style lang="scss" scoped>
  .service-configurator-view {
    grid-template-columns: 1fr auto 4fr auto 1fr !important;
  }
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
