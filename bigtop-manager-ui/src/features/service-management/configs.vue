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

  type FormStateType = { configs: ServiceConfig[] }

  interface RouteParams {
    id: number
    serviceId: number
  }

  const { t } = useI18n()
  const createStore = useCreateServiceStore()
  const attrs = useAttrs() as Partial<ServiceVO>
  const route = useRoute()

  const getServiceDetail = inject('getServiceDetail') as () => any

  const searchStr = ref('')
  const loading = ref(false)
  const activeKey = ref<number[]>([])
  const formRef = ref<FormInstance>()
  const snapshotConfigs = ref<ServiceConfig[]>([])
  const captureRef = ref<InstanceType<typeof CaptureSnapshot>>()
  const snapshotRef = ref<InstanceType<typeof SnapshotManagement>>()
  const formState = reactive<FormStateType>({
    configs: []
  })

  const { configs } = toRefs(formState)

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

  const payload = computed(() => {
    const { id: clusterId, serviceId: id } = route.params as unknown as RouteParams
    return { clusterId, id }
  })

  /**
   * Filters service configurations based on a search keyword.
   * Only includes configurations with properties matching the keyword.
   *
   * @returns A list of filtered service configurations.
   */
  const filterConfigs = computed(() => {
    if (!searchStr.value) return [...configs.value]

    const result: ServiceConfig[] = []
    for (const item of configs.value) {
      const matchedProp = item.properties?.filter((prop) => matchKeyword(searchStr.value, prop))
      if (matchedProp?.length) {
        result.push({ ...item, properties: matchedProp })
      }
    }

    return result
  })

  /**
   * Adds a new property to the given service configuration.
   * @param config
   */
  const manualAddProperty = (config: ServiceConfig) => {
    config.properties?.push(createStore.generateProperty())
  }

  /**
   * Marks a property as deleted in the given service configuration.
   * The property is identified by its name, and its action is set to 'delete'.
   *
   * @param property - The property to be marked as deleted.
   * @param config - The service configuration containing the property.
   */
  const manualDeleteProperty = (property: Property, config: ServiceConfig) => {
    const props = config.properties
    if (!Array.isArray(props)) return

    const target = props.find((p) => p.name === property.name)
    if (target) {
      target.action = 'delete'
    }
  }

  /**
   * Checks if a keyword matches a property or service configuration.
   *
   * @param keyword - The keyword to search for.
   * @param prop - The property to check.
   * @param config - Optional service configuration to check.
   * @returns True if the keyword matches, otherwise false.
   */
  const matchKeyword = (keyword: string, prop: Property, config?: ServiceConfig) => {
    const lowerKeyword = keyword.toLowerCase()
    const includesProp =
      prop.name?.toLowerCase().includes(lowerKeyword) || prop.value?.toLowerCase().includes(lowerKeyword)
    if (config != undefined) {
      return config.name?.toLowerCase().includes(lowerKeyword) || includesProp
    }

    return includesProp
  }

  const saveConfigs = async () => {
    try {
      const valid = await validate()
      if (!valid) return

      loading.value = true
      const params = createStore.getDiffConfigs(configs.value, snapshotConfigs.value)
      const data = await updateServiceConfigs({ ...payload.value }, params)
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

  const validate = async () => {
    try {
      searchStr.value = ''
      await nextTick()
      await formRef.value?.validate()
      return true
    } catch (error: any) {
      activeKey.value.push(error.errorFields[0].name[1])
      formRef.value?.scrollToField(error.errorFields[0].name)
      return false
    }
  }

  const onCaptureSnapshot = () => {
    captureRef.value?.handleOpen({ ...payload.value })
  }

  const openSnapshotManagement = () => {
    snapshotRef.value?.handleOpen({ ...payload.value })
  }

  onActivated(async () => {
    await getServiceDetail()
    configs.value = createStore.injectKeysToConfigs(attrs.configs ?? [])
    snapshotConfigs.value = JSON.parse(JSON.stringify(attrs.configs))
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
        />
      </div>
    </header>
    <section>
      <!-- configs -->
      <a-form ref="formRef" :model="formState" :label-wrap="true" :disabled="loading">
        <a-empty v-if="filterConfigs.length === 0" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
        <a-collapse v-else v-model:active-key="activeKey" :bordered="false" :ghost="true">
          <template v-for="(config, configIdx) in filterConfigs" :key="configIdx">
            <a-collapse-panel>
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
                <a-row v-if="property.action != 'delete'" justify="space-between" :gutter="[16, 0]" :wrap="true">
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
                        :title="property.name"
                        class="property-name"
                        :class="{ 'required-mark': property.attrs?.required }"
                      >
                        <span>
                          {{ property.name }}
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
                  <a-button type="text" shape="circle" @click="manualDeleteProperty(property, config)">
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
