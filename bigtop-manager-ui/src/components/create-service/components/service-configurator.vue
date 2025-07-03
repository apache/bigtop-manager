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
  import { computed, onActivated, onDeactivated, ref, shallowRef, watch } from 'vue'
  import { storeToRefs } from 'pinia'
  import { debounce } from 'lodash'
  import { Empty } from 'ant-design-vue'
  import TreeSelector from './tree-selector.vue'
  import { useCreateServiceStore } from '@/store/create-service'
  import { useServiceStore } from '@/store/service'
  import type { ServiceConfigReq } from '@/api/command/types'
  import type { ComponentVO } from '@/api/component/types'
  import type { Key } from 'ant-design-vue/es/_util/type'
  import type { Property } from '@/api/service/types'

  interface Props {
    isView?: boolean
  }

  const props = withDefaults(defineProps<Props>(), {
    isView: false
  })

  const createStore = useCreateServiceStore()
  const serviceStore = useServiceStore()
  const { stepContext, selectedServices } = storeToRefs(createStore)
  const searchStr = ref('')
  const currService = ref<Key>('')
  const configs = ref<ServiceConfigReq[]>([])
  const activeKey = ref<number[]>([])
  const debouncedOnSearch = ref()
  const hostPreviewList = ref<ComponentVO[]>([])
  const filterConfigs = ref<ServiceConfigReq[]>([])
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

  const serviceList = computed(() => selectedServices.value)
  const disabled = computed(() => {
    const clusterId = stepContext.value.clusterId
    return serviceStore
      .getInstalledNamesOrIdsOfServiceByKey(`${clusterId}`)
      .includes(currService.value.toString().split('/').at(-1)!)
  })

  // const disabled = computed(() => {
  //   return creationModeType.value === 'component'
  //     ? false
  //     : serviceStore
  //         .getInstalledNamesOrIdsOfServiceByKey(`${clusterId.value}`)
  //         .includes(currService.value.toString().split('/').at(-1)!)
  // })

  watch(
    () => props.isView,
    () => {
      searchStr.value = ''
      filterConfigs.value = configs.value
    }
  )

  const createNewProperty = () => {
    return {
      name: '',
      displayName: '',
      value: '',
      isManual: true
    }
  }

  const manualAddPropertyForConfig = (config: ServiceConfigReq) => {
    config.properties?.push(createNewProperty())
  }

  const removeProperty = (property: Property, config: ServiceConfigReq) => {
    const index = config.properties.findIndex((v) => v.name === property.name)
    if (index != -1) {
      config.properties.splice(index, 1)
    }
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
    filterConfigurations()
  }

  const filterConfigurations = () => {
    if (!searchStr.value) {
      filterConfigs.value = configs.value
    }
    const lowerSearchTerm = searchStr.value.toLowerCase()
    filterConfigs.value = configs.value.filter((config) => {
      return config.properties.some((property) => {
        return (
          (property.displayName || '').toLowerCase().includes(lowerSearchTerm) ||
          property.name.toLowerCase().includes(lowerSearchTerm) ||
          (property.value && property.value.toString().toLowerCase().includes(lowerSearchTerm))
        )
      })
    })
  }

  // const splitSearchStr = (splitStr: string) => {
  //   return splitStr.toString().split(new RegExp(`(?<=${searchStr.value})|(?=${searchStr.value})`, 'i'))
  // }

  onActivated(() => {
    debouncedOnSearch.value = debounce(filterConfigurations, 300)
    filterConfigs.value = [...configs.value]
  })

  onDeactivated(() => {
    debouncedOnSearch.value.cancel()
  })
</script>

<template>
  <div class="service-configurator" :class="{ 'service-configurator-view': $props.isView }">
    <section>
      <div class="list-title">
        <div>{{ $t('service.service_list') }}</div>
      </div>
      <tree-selector :tree="serviceList" :field-names="fieldNames" @change="handleChange" />
    </section>
    <a-divider type="vertical" class="divider" />
    <section>
      <div class="list-title">
        <div>{{ $t('service.host_preview') }}</div>
        <a-input
          v-model:value="searchStr"
          :placeholder="$t('service.please_enter_search_keyword')"
          @input="debouncedOnSearch"
        />
      </div>
      <a-empty v-if="filterConfigs.length === 0" :image="Empty.PRESENTED_IMAGE_SIMPLE" />
      <a-form v-else :disabled="$props.isView || disabled" :label-wrap="true">
        <a-collapse v-model:active-key="activeKey" :bordered="false" :ghost="true">
          <a-collapse-panel v-for="config in filterConfigs" :key="config.id">
            <template #extra>
              <a-button
                v-if="!$props.isView && !disabled"
                type="text"
                shape="circle"
                @click.stop="manualAddPropertyForConfig(config)"
              >
                <template #icon>
                  <svg-icon name="plus-dark" />
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

                  <!-- <template v-else>
                    <template v-for="(fragment, i) in splitSearchStr(item.displayName ?? item.name)">
                      <mark v-if="fragment.toLowerCase() === searchStr.toLowerCase()" :key="i" class="highlight">
                        {{ fragment }}
                      </mark>
                      <template v-else>
                        <span :key="i" style="overflow-wrap: break-word" :title="item.displayName ?? item.name">
                          {{ fragment }}
                        </span>
                      </template>
                    </template>
                  </template> -->
                </a-form-item>
              </a-col>
              <a-col v-bind="layout.wrapperCol">
                <a-form-item>
                  <a-textarea v-model:value="property.value" :auto-size="{ minRows: 1, maxRows: 5 }" />
                </a-form-item>
              </a-col>
              <a-button
                v-if="!$props.isView && !disabled"
                type="text"
                shape="circle"
                @click="removeProperty(property, config)"
              >
                <template #icon>
                  <svg-icon name="remove" />
                </template>
              </a-button>
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
</style>
