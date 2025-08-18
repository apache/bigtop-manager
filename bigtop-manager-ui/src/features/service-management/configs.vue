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
  import { Empty, FormInstance, message } from 'ant-design-vue'
  import { updateServiceConfigs } from '@/api/service'
  import { useCreateServiceStore } from '@/store/create-service'

  import CaptureSnapshot from './components/capture-snapshot.vue'
  import SnapshotManagement from './components/snapshot-management.vue'

  import type { Property, ServiceConfig, ServiceVO } from '@/api/service/types'

  const { t } = useI18n()
  const createStore = useCreateServiceStore()
  const attrs = useAttrs() as unknown as Required<ServiceVO> & { clusterId: number }

  const getServiceDetail = inject('getServiceDetail') as () => any

  const searchStr = ref('')
  const loading = ref(false)
  const activeKey = ref<number[]>([])
  const snapshotConfigs = ref<ServiceConfig[]>([])
  const captureRef = ref<InstanceType<typeof CaptureSnapshot>>()
  const snapshotRef = ref<InstanceType<typeof SnapshotManagement>>()
  const debouncedOnSearch = ref()
  const formRef = ref<FormInstance>()
  const formState = ref<Record<string, ServiceConfig[]>>({
    configs: []
  })

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

  const manualAddProperty = (config: ServiceConfig) => {
    config.properties?.push(createStore.generateProperty())
  }

  const deleteProperty = (property: Property, config: ServiceConfig) => {
    const props = config.properties
    if (!Array.isArray(props)) return

    const target = props.find((p) => p.name === property.name)
    if (target) {
      target.action = 'delete'
    }
  }

  const matchKeyword = (config: ServiceConfig, prop: Property) => {
    if (!searchStr.value) return true
    const lowerKeyword = searchStr.value.toLowerCase()

    return (
      config.name?.toLowerCase().includes(lowerKeyword) ||
      prop.name?.toLowerCase().includes(lowerKeyword) ||
      prop.value?.toLowerCase().includes(lowerKeyword) ||
      prop.displayName?.toLowerCase().includes(lowerKeyword)
    )
  }

  const hasMatchedProps = (config: ServiceConfig) => {
    return config.properties?.some((p) => matchKeyword(config, p))
  }

  const validate = async () => {
    try {
      await formRef.value?.validate()
      return true
    } catch (error: any) {
      searchStr.value = ''
      formRef.value?.scrollToField(error.errorFields[0].name)
      return false
    }
  }

  const saveConfigs = async () => {
    try {
      const { id, clusterId } = attrs
      loading.value = true
      const valid = await validate()
      if (!valid) {
        return
      }
      const params = createStore.getDiffConfigs(formState.value.configs, snapshotConfigs.value)
      const data = await updateServiceConfigs({ id, clusterId }, params)
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
    const { id, clusterId } = attrs
    captureRef.value?.handleOpen({ id, clusterId })
  }

  const openSnapshotManagement = () => {
    const { id, clusterId } = attrs
    snapshotRef.value?.handleOpen({ id, clusterId })
  }

  onActivated(async () => {
    await getServiceDetail()
    formState.value.configs = createStore.injectKeysToConfigs(attrs.configs)
    snapshotConfigs.value = JSON.parse(JSON.stringify(attrs.configs))
  })

  onDeactivated(() => {
    debouncedOnSearch.value.cancel()
  })
</script>

<template>
  <div class="service-configurator">
    <header>
      <div class="header-title">{{ t('common.configs') }}</div>
      <div class="list-operation">
        <a-space>
          <a-button type="primary" @click="onCaptureSnapshot">{{ t('service.capture_snapshot') }}</a-button>
          <a-button @click="openSnapshotManagement">{{ t('service.snapshot_management') }}</a-button>
        </a-space>
        <a-input
          v-model:value="searchStr"
          :allow-clear="true"
          :placeholder="t('service.please_enter_search_keyword')"
          @input="debouncedOnSearch"
        />
      </div>
    </header>
    <section>
      <a-empty v-if="formState.configs.length === 0" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
      <!-- configs -->
      <a-form v-else ref="formRef" :model="formState" :label-wrap="true" :disabled="loading">
        <a-collapse v-model:active-key="activeKey" :bordered="false" :ghost="true">
          <template v-for="(config, configIdx) in formState.configs" :key="config.id">
            <a-collapse-panel v-if="hasMatchedProps(config)" :key="config.id">
              <template #extra>
                <a-button type="text" shape="circle" @click.stop="manualAddProperty(config)">
                  <template #icon>
                    <svg-icon name="plus" />
                  </template>
                </a-button>
              </template>
              <template #header>
                <span>{{ config.name }}</span>
              </template>
              <!-- properties -->
              <template v-for="(property, propertyIdx) in config.properties" :key="property.__key">
                <a-row
                  v-show="matchKeyword(config, property) && property.action != 'delete'"
                  justify="space-between"
                  :gutter="[16, 0]"
                  :wrap="true"
                >
                  <a-col v-bind="layout.labelCol">
                    <a-form-item
                      :key="property.__key"
                      :name="['configs', configIdx, 'properties', propertyIdx, 'name']"
                      :rules="{
                        required: property.attrs?.required,
                        message: t('service.required')
                      }"
                    >
                      <a-textarea
                        v-if="property.isManual"
                        v-model:value="property.name"
                        :auto-size="{ minRows: 1, maxRows: 30 }"
                      />
                      <div
                        v-else
                        :title="property.displayName ?? property.name"
                        class="property-name"
                        :class="{ 'required-mark': property.attrs?.required }"
                      >
                        <span>
                          {{ property.displayName ?? property.name }}
                        </span>
                      </div>
                    </a-form-item>
                  </a-col>
                  <a-col v-bind="layout.wrapperCol">
                    <a-form-item
                      :key="property.__key"
                      :name="['configs', configIdx, 'properties', propertyIdx, 'value']"
                      :rules="{
                        required: property.attrs?.required,
                        message: t('service.required')
                      }"
                    >
                      <a-tooltip v-if="property.desc" placement="topLeft">
                        <template #title>
                          <span>{{ property.desc }}</span>
                        </template>
                        <a-textarea
                          v-model:value="property.value"
                          :rows="property?.attrs?.type === 'longtext' ? 10 : 1"
                        />
                      </a-tooltip>
                      <a-textarea
                        v-else
                        v-model:value="property.value"
                        :rows="property?.attrs?.type === 'longtext' ? 10 : 1"
                      />
                    </a-form-item>
                  </a-col>
                  <a-button type="text" shape="circle" @click="deleteProperty(property, config)">
                    <template #icon>
                      <svg-icon name="remove" />
                    </template>
                  </a-button>
                </a-row>
              </template>
            </a-collapse-panel>
          </template>
        </a-collapse>
      </a-form>
    </section>
    <div style="text-align: end">
      <a-button type="primary" :loading="loading" @click="saveConfigs">
        {{ t('common.save') }}
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
      .ant-input-affix-wrapper {
        flex: 0 1 260px;
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

  .property-name {
    display: flex;
    span {
      flex: 1;
      min-width: 0;
      overflow-wrap: break-word;
      word-break: break-all;
    }
  }

  .required-mark {
    &::before {
      content: '*';
      color: $red;
      padding-right: 4px;
    }
  }
</style>
