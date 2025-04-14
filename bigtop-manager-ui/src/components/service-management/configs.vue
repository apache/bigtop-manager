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
  import { onActivated, inject, useAttrs, shallowRef, ref, onDeactivated, ComputedRef } from 'vue'
  import { debounce } from 'lodash'
  import { useI18n } from 'vue-i18n'
  import { Empty, message } from 'ant-design-vue'
  import { updateServiceConfigs } from '@/api/service'
  import CaptureSnapshot from './components/capture-snapshot.vue'
  import SnapshotManagement from './components/snapshot-management.vue'
  import type { Property, ServiceConfig, ServiceVO } from '@/api/service/types'

  interface ServieceInfo {
    cluster: string
    id: number
    service: string
    serviceId: number
  }

  const { routeParams }: { routeParams: ComputedRef<ServieceInfo> } = inject('service') as any
  const attrs = useAttrs() as unknown as ServiceVO
  const { t } = useI18n()
  const getServiceDetail = inject('getServiceDetail') as () => any
  const searchStr = ref('')
  const loading = ref(false)
  const activeKey = ref<number[]>([])
  const configs = ref<ServiceConfig[]>([])
  const filterConfigs = ref<ServiceConfig[]>([])
  const captureRef = ref<InstanceType<typeof CaptureSnapshot>>()
  const snapshotRef = ref<InstanceType<typeof SnapshotManagement>>()
  const debouncedOnSearch = ref()
  const layout = shallowRef({
    labelCol: {
      xs: { span: 23 },
      sm: { span: 7 },
      md: { span: 7 },
      lg: { span: 5 }
    },
    wrapperCol: {
      xs: { span: 23 },
      sm: { span: 15 },
      md: { span: 15 },
      lg: { span: 17 }
    }
  })

  const createNewProperty = () => {
    return {
      name: '',
      displayName: '',
      value: '',
      isManual: true
    }
  }

  const manualAddPropertyForConfig = (config: ServiceConfig) => {
    config.properties?.push(createNewProperty())
  }

  const removeProperty = (property: Property, config: ServiceConfig) => {
    const index = (config.properties || []).findIndex((v) => v.name === property.name)
    if (index != -1) {
      config.properties?.splice(index, 1)
    }
  }

  const filterConfigurations = () => {
    if (!searchStr.value) {
      filterConfigs.value = configs.value
    }
    const lowerSearchTerm = searchStr.value.toLowerCase()
    filterConfigs.value = configs.value.filter((config) => {
      return config.properties?.some((property) => {
        return (
          (property.displayName || '').toLowerCase().includes(lowerSearchTerm) ||
          property.name.toLowerCase().includes(lowerSearchTerm) ||
          (property.value && property.value.toString().toLowerCase().includes(lowerSearchTerm))
        )
      })
    })
  }

  const saveConfigs = async () => {
    try {
      const { serviceId, id: clusterId } = routeParams.value
      loading.value = true
      const data = await updateServiceConfigs({ id: serviceId, clusterId }, [...configs.value])
      if (data) {
        message.success(t('common.update_success'))
        getServiceDetail()
      }
    } catch (error) {
      console.log('error :>> ', error)
    } finally {
      loading.value = false
    }
  }

  const onCaptureSnapshot = () => {
    const { serviceId, id: clusterId } = routeParams.value
    captureRef.value?.handleOpen({ id: serviceId, clusterId })
  }

  const openSnapshotManagement = () => {
    const { serviceId, id: clusterId } = routeParams.value
    snapshotRef.value?.handleOpen({ id: serviceId, clusterId })
  }

  onActivated(async () => {
    await getServiceDetail()
    configs.value = attrs.configs as ServiceConfig[]
    filterConfigs.value = [...configs.value]
    debouncedOnSearch.value = debounce(filterConfigurations, 300)
  })

  onDeactivated(() => {
    debouncedOnSearch.value.cancel()
  })
</script>

<template>
  <div class="service-configurator">
    <header>
      <div class="header-title">{{ $t('common.configs') }}</div>
      <div class="list-operation">
        <a-space>
          <a-button type="primary" @click="onCaptureSnapshot">{{ $t('service.capture_snapshot') }}</a-button>
          <a-button @click="openSnapshotManagement">{{ $t('service.snapshot_management') }}</a-button>
        </a-space>
        <a-input
          v-model:value="searchStr"
          :placeholder="$t('service.please_enter_search_keyword')"
          @input="debouncedOnSearch"
        />
      </div>
    </header>
    <section>
      <a-empty v-if="filterConfigs.length === 0" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
      <a-form v-else :label-wrap="true" :disabled="loading">
        <a-collapse v-model:active-key="activeKey" :bordered="false" :ghost="true">
          <a-collapse-panel v-for="config in filterConfigs" :key="config.id">
            <template #extra>
              <a-button type="text" shape="circle" @click.stop="manualAddPropertyForConfig(config)">
                <template #icon>
                  <svg-icon name="plus_dark" />
                </template>
              </a-button>
            </template>
            <template #header>
              <span>{{ config.name }}</span>
            </template>
            <a-row
              v-for="(property, idx) in config.properties"
              :key="idx"
              justify="space-between"
              :gutter="[16, 0]"
              :wrap="true"
            >
              <a-col v-bind="layout.labelCol">
                <a-form-item>
                  <a-textarea
                    v-if="property.isManual"
                    v-model:value="property.name"
                    :auto-size="{ minRows: 1, maxRows: 5 }"
                  />
                  <span v-else style="overflow-wrap: break-word" :title="property.displayName ?? property.name">
                    {{ property.displayName ?? property.name }}
                  </span>
                </a-form-item>
              </a-col>
              <a-col v-bind="layout.wrapperCol">
                <a-form-item>
                  <a-textarea v-model:value="property.value" :auto-size="{ minRows: 1, maxRows: 5 }" />
                </a-form-item>
              </a-col>
              <a-button type="text" shape="circle" @click="removeProperty(property, config)">
                <template #icon>
                  <svg-icon name="remove" />
                </template>
              </a-button>
            </a-row>
          </a-collapse-panel>
        </a-collapse>
      </a-form>
    </section>
    <div style="text-align: end">
      <a-button type="primary" :loading="loading" @click="saveConfigs">
        {{ $t('common.save') }}
      </a-button>
    </div>
    <capture-snapshot ref="captureRef" @success="getServiceDetail" />
    <snapshot-management ref="snapshotRef" />
  </div>
</template>

<style lang="scss" scoped>
  .highlight {
    background-color: rgb(255, 192, 105);
    padding: 0px;
  }

  .service-configurator {
    display: grid;
    gap: 16px;
    .list-operation {
      display: flex;
      justify-content: space-between;
      gap: 16px;
      .ant-input {
        flex: 0 1 160px;
      }
    }
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
