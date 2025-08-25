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
  import { Empty, FormInstance } from 'ant-design-vue'
  import { useCreateServiceStore } from '@/store/create-service'
  import { useServiceStore } from '@/store/service'

  import TreeSelector from './tree-selector.vue'

  import type { ComponentVO } from '@/api/component/types'
  import type { Key } from 'ant-design-vue/es/_util/type'
  import type { Property, ServiceConfig } from '@/api/service/types'

  interface Props {
    isView?: boolean
  }

  type FormStateType = { configs: ServiceConfig[] }

  withDefaults(defineProps<Props>(), {
    isView: false
  })

  const { t } = useI18n()
  const createStore = useCreateServiceStore()
  const serviceStore = useServiceStore()
  const { stepContext, selectedServices } = storeToRefs(createStore)

  const searchStr = ref('')
  const currService = ref<Key>('')
  const activeKey = ref<number[]>([])
  const debouncedOnSearch = ref()
  const hostPreviewList = ref<ComponentVO[]>([])
  const formRef = ref<FormInstance>()
  const treeSelectorRef = ref<InstanceType<typeof TreeSelector> | null>(null)
  const formState = ref<FormStateType>({
    configs: []
  })

  const fieldNames = shallowRef({
    title: 'displayName',
    key: 'name'
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

  const clusterId = computed(() => stepContext.value.clusterId)
  const serviceList = computed(() => selectedServices.value)

  const disabled = computed(() =>
    serviceStore
      .getInstalledNamesOrIdsOfServiceByKey(`${clusterId.value}`)
      .includes(currService.value.toString().split('/').at(-1)!)
  )

  /**
   * Computes a map of selected services with their required configurations.
   * Only includes services with required properties that have empty values.
   *
   * @returns A map where the key is the service name and the value is its required configurations.
   */
  const selectedServiceMap = computed(() => {
    const map = new Map<string, ServiceConfig[]>()

    for (const service of selectedServices.value) {
      if (!service.name || map.has(service.name)) continue

      const requireds = extractRequireds(service.configs)
      if (requireds.length > 0) map.set(service.name, requireds)
    }

    return map
  })

  /**
   * Filters service configurations based on a search keyword.
   * Only includes configurations with properties matching the keyword.
   *
   * @returns A list of filtered service configurations.
   */
  const filterConfigs = computed(() => {
    const configs = formState.value.configs
    if (!searchStr.value) return [...configs]

    const result: ServiceConfig[] = []
    for (const item of configs) {
      const matchedProp = item.properties?.filter((prop) => matchKeyword(searchStr.value, prop, item))
      if (matchedProp?.length) {
        result.push({ ...item, properties: matchedProp })
      }
    }

    return result
  })

  const handleSelect = (expandSelectedKeyPath: string) => {
    const name = expandSelectedKeyPath.split('/').at(-1)
    const service = selectedServices.value.find((v) => v.name === name)

    currService.value = expandSelectedKeyPath
    formState.value.configs = service?.configs ?? []
    hostPreviewList.value = service?.components ?? []
  }

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
      prop.name?.toLowerCase().includes(lowerKeyword) ||
      prop.value?.toLowerCase().includes(lowerKeyword) ||
      prop.displayName?.toLowerCase().includes(lowerKeyword)

    if (config != undefined) {
      return config.name?.toLowerCase().includes(lowerKeyword) || includesProp
    }

    return includesProp
  }

  /**
   * Extracts required properties from the given service configurations.
   * Only properties marked as required and with empty values are included.
   *
   * @param configs - The service configurations to check.
   * @returns A list of configurations with their required properties.
   */
  const extractRequireds = (configs?: ServiceConfig[]) => {
    const result: ServiceConfig[] = []

    for (const config of configs ?? []) {
      const requireds = config.properties?.filter((p) => p.attrs?.required && p.value == '') ?? []
      if (requireds.length > 0) {
        result.push({ [config.name!]: requireds })
      }
    }

    return result
  }

  /**
   * Checks if there are any required configurations in the selected services.
   * If required configurations exist, it selects the key in the tree.
   *
   * @returns True if no required configurations are found, otherwise false.
   */
  const checkRequiredOfConfigs = () => {
    if (selectedServiceMap.value.size === 0) {
      return true
    }

    const key = [...selectedServiceMap.value.keys()][0]
    treeSelectorRef.value?.handleSelect([key])

    return false
  }

  const validate = async () => {
    try {
      const hasError = checkRequiredOfConfigs()
      searchStr.value = ''
      await nextTick()
      await formRef.value?.validate()
      return true && hasError
    } catch (error: any) {
      activeKey.value.push(error.errorFields[0].name[1])
      formRef.value?.scrollToField(error.errorFields[0].name)
      return false
    }
  }

  onDeactivated(() => {
    searchStr.value = ''
    activeKey.value = []
    formRef.value?.clearValidate()
  })

  defineExpose({
    validate
  })
</script>

<template>
  <div class="service-configurator" :class="{ 'service-configurator-view': $props.isView }">
    <section>
      <div class="list-title">
        <div>{{ t('service.service_list') }}</div>
      </div>
      <tree-selector ref="treeSelectorRef" :tree="serviceList" :field-names="fieldNames" @change="handleSelect" />
    </section>
    <a-divider type="vertical" class="divider" />
    <section>
      <div class="list-title">
        <div>{{ t('service.host_preview') }}</div>
        <a-input
          v-model:value="searchStr"
          :placeholder="t('service.please_enter_search_keyword')"
          @input="debouncedOnSearch"
        />
      </div>
      <!-- configs -->
      <a-form ref="formRef" :model="formState" :disabled="$props.isView || disabled" :label-wrap="true">
        <a-empty v-if="filterConfigs.length === 0" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
        <a-collapse v-else v-model:active-key="activeKey" :bordered="false" :ghost="true">
          <template v-for="(config, configIdx) in filterConfigs" :key="configIdx">
            <a-collapse-panel>
              <template #extra>
                <a-button
                  v-if="!$props.isView && !disabled"
                  type="text"
                  shape="circle"
                  @click.stop="manualAddProperty(config)"
                >
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
                <a-row v-show="property.action != 'delete'" justify="space-between" :gutter="[16, 0]" :wrap="true">
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
                        :auto-size="{ minRows: 1, maxRows: 5 }"
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
                  <a-button
                    v-if="!$props.isView && !disabled"
                    type="text"
                    shape="circle"
                    @click="manualDeleteProperty(property, config)"
                  >
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
    <template v-if="isView">
      <a-divider type="vertical" class="divider" />
      <section>
        <div class="list-title">
          <div>{{ t('service.host_preview') }}</div>
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
  .highlight {
    background-color: rgb(255, 192, 105);
    padding: 0px;
  }

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
