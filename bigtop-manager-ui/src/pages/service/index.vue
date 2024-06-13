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
  import { onMounted, ref, watch } from 'vue'
  import { useRoute } from 'vue-router'
  import type { SelectProps, MenuProps } from 'ant-design-vue'
  import {
    QuestionCircleOutlined,
    DownOutlined,
    UserOutlined
  } from '@ant-design/icons-vue'
  import { useConfigStore } from '@/store/config'
  import { storeToRefs } from 'pinia'
  import { ServiceConfigVO, TypeConfigVO } from '@/api/config/types.ts'
  import { useComponentStore } from '@/store/component'
  import { HostComponentVO } from '@/api/component/types.ts'
  import DotState from '@/components/dot-state/index.vue'

  const menuOps = [
    {
      key: '1',
      dicText: 'Start'
    },
    {
      key: '2',
      dicText: 'Stop'
    },
    {
      key: '3',
      dicText: 'Restart'
    }
  ]

  // #52c41a   green
  // #ff4d4f   red
  // #d9d9d9   gray
  // #f0f964   yellow
  const stateColor = {
    Installed: '#52c41a',
    Started: '#52c41a',
    Maintained: '#d9d9d9',
    Uninstalled: '#f0f964',
    Stopped: 'ff4d4f'
  }

  const links = [
    'NameNode UINameNode UINameNode',
    'NameNode Logs',
    'NameNode JMX',
    'Thread Stacks'
  ]

  const route = useRoute()
  const configStore = useConfigStore()
  const { allConfigs } = storeToRefs(configStore)
  const componentStore = useComponentStore()
  const { hostComponents } = storeToRefs(componentStore)

  const serviceName = ref<string>(route.params.serviceName as string)

  // summary model start
  const currentHostComponent = ref<HostComponentVO[]>([])
  watch(hostComponents, (newVal) => {
    currentHostComponent.value = newVal.filter(
      (hc: HostComponentVO) => hc.serviceName === serviceName.value
    )
  })

  // summary model end

  // config model start
  const serviceConfigDesc = ref<SelectProps['options']>([])
  const activeSelect = ref()
  const currentConfigs = ref<TypeConfigVO[]>([])
  const currentConfigVersion = ref<number>()
  const initConfigVersion = ref<number>()
  const showConfigTip = ref<boolean>(false)

  watch(allConfigs, (newVal) => {
    allConfigs.value = newVal
    loadCurrentConfigs()
  })

  const loadCurrentConfigs = () => {
    serviceConfigDesc.value = allConfigs.value
      .filter((sc: ServiceConfigVO) => sc.serviceName === serviceName.value)
      .map((sc: ServiceConfigVO) => ({
        value: sc.version,
        label: `Version: ${sc.version}`,
        title: `${sc.configDesc}
        \n${sc.createTime}`
      }))

    currentConfigVersion.value = serviceConfigDesc.value?.[0].value as number

    currentConfigs.value = allConfigs.value
      .filter(
        (sc: ServiceConfigVO) =>
          sc.serviceName === serviceName.value &&
          currentConfigVersion.value === sc.version
      )
      .flatMap((sc: ServiceConfigVO) => sc.configs)
      .map((cd: TypeConfigVO) => ({
        typeName: cd.typeName,
        properties: cd.properties
      }))
  }

  const handleChange: SelectProps['onChange'] = (value) => {
    if (typeof value === 'object' && 'key' in value) {
      currentConfigVersion.value = Number(value.key)
    }
    showConfigTip.value = currentConfigVersion.value !== initConfigVersion.value

    loadCurrentConfigs()
  }

  const activeConfig = ref()

  watch(currentConfigs, (newVal) => {
    activeConfig.value = newVal.length > 0 ? newVal[0].typeName : null
  })
  // config model end
  const handleMenuClick: MenuProps['onClick'] = (e) => {
    console.log('click', e)
  }

  const initServiceMeta = async () => {
    await configStore.loadAllConfigs()
    showConfigTip.value = false
    activeSelect.value = serviceConfigDesc.value?.[0]
    currentConfigVersion.value = serviceConfigDesc.value?.[0].value as number
    initConfigVersion.value = serviceConfigDesc.value?.[0].value as number
    await componentStore.loadHostComponents()
  }

  onMounted(() => {
    initServiceMeta()
  })

  watch(
    () => route.params,
    (params) => {
      serviceName.value = params.serviceName as string
      initServiceMeta()
    }
  )
</script>

<template>
  <a-tabs>
    <template #rightExtra>
      <a-dropdown>
        <template #overlay>
          <a-menu @click="handleMenuClick">
            <a-menu-item v-for="item in menuOps" :key="item.key">
              <UserOutlined />
              <span>{{ item.dicText }}</span>
            </a-menu-item>
          </a-menu>
        </template>
        <a-button type="primary">
          {{ $t('common.action') }}
          <DownOutlined />
        </a-button>
      </a-dropdown>
    </template>
    <a-tab-pane key="summary">
      <template #tab>{{ $t('service.summary') }}</template>
      <a-layout-content class="summary-layout">
        <div class="left-section">
          <h2>{{ $t('service.components') }}</h2>
          <div v-if="currentHostComponent.length > 0" class="summary-ctx">
            <template v-for="item in currentHostComponent" :key="`${item.id}`">
              <div class="card">
                <div class="service-name">
                  <router-link :to="'/services/' + serviceName">
                    {{ item.displayName }}
                  </router-link>
                  <a-tag
                    :bordered="false"
                    style="color: rgb(145 134 134 / 90%)"
                  >
                    {{ item.category }}
                  </a-tag>
                </div>
                <div class="comp-info">
                  <div class="host-name">{{ item.hostname }}</div>
                </div>
                <footer>
                  <div class="comp-state">
                    <dot-state :color="stateColor[item.state]" class="dot-rest">
                      <span class="comp-state-text">{{ item.state }}</span>
                    </dot-state>
                  </div>
                </footer>
              </div>
            </template>
          </div>
        </div>
        <div class="right-section">
          <h2>{{ $t('service.quicklinks') }}</h2>
          <ul v-if="links.length > 0">
            <li v-for="link in links" :key="link">
              <a>{{ link }}</a>
            </li>
          </ul>
          <a-empty v-else />
        </div>
      </a-layout-content>
    </a-tab-pane>

    <a-tab-pane key="config" force-render>
      <template #tab>{{ $t('service.config') }}</template>
      <a-space>
        <a-select
          v-model:value="activeSelect"
          label-in-value
          :options="serviceConfigDesc"
          @change="handleChange"
        ></a-select>
        <template v-if="showConfigTip">
          <a-button type="primary">Not Current Config</a-button></template
        >
      </a-space>

      <a-divider />
      <a-collapse v-model:active-key="activeConfig" ghost accordion>
        <a-collapse-panel
          v-for="config in currentConfigs"
          :key="config.typeName"
          class="panel"
          :header="config.typeName"
        >
          <div
            v-for="property in config.properties"
            :key="property.name"
            class="config-item"
          >
            <div class="config-item-key">
              {{ property.displayName ?? property.name }}
            </div>
            <div class="config-item-value">
              <a-input v-model:value="property.value" />
            </div>
            <a-tooltip>
              <template #title>
                {{ property.desc }}
              </template>
              <question-circle-outlined class="config-item-desc" />
            </a-tooltip>
          </div>
        </a-collapse-panel>
      </a-collapse>
    </a-tab-pane>
  </a-tabs>
</template>

<style scoped lang="scss">
  .dot-rest {
    @include flex(center, center);
  }
  .summary-layout {
    display: flex;

    .left-section {
      width: 82%;
      .a-card {
        background-color: rgb(128, 171, 209);
      }
      border-right: 2px solid #eee;
    }

    .right-section {
      width: 18%;
      min-width: 13rem;
      height: 100%;
      padding: 0 1rem;
      box-sizing: border-box;
      min-height: 33.75rem;

      ul {
        list-style: none;
        margin: 0;
        padding: 0;
        li {
          a {
            display: flex;
            padding: 0.25rem;
          }
        }
      }
    }

    .summary-ctx {
      display: flex;
      flex-wrap: wrap;
      box-sizing: border-box;

      .card {
        margin: 0.75rem;
        padding: 0.75rem;
        border-radius: 0.5rem;
        position: relative;
        flex: 0 1 calc((100% / 3) - 1.5rem);
        min-width: calc((100% / 3) - 1.5rem);
        box-shadow: 0 0 2px rgba(0, 0, 0, 0.1);
        overflow: auto;
        &:hover {
          box-shadow: 0 0 6px rgba(0, 0, 0, 0.16);
          transform: scale(1.002);
          transition: all 0.3s;
        }
        .service-name {
          font-size: 1.06rem;
          font-weight: 600;
          margin-bottom: 0.375rem;
          @include flex(space-between, center);
        }
        .comp-info {
          @include flex(center);
          margin-bottom: 1.25rem;
          .host-name {
            width: 80%;
            font-size: 0.8rem;
            color: #797878d0;
            font-weight: 500;
            flex: 1;
          }
        }
        footer {
          @include flex(space-between, center);
          .comp-state {
            font-size: 1rem;
            align-items: flex-end;
            &-text {
              color: #888;
            }
          }
        }
      }
    }
  }

  .container {
    display: flex;
    flex-direction: column;
    justify-content: start;
    align-items: center;
    align-content: center;
    height: 100%;

    .title {
      font-size: 1.5rem;
      line-height: 2rem;
      margin-bottom: 1rem;
    }

    .content {
      width: 100%;
      height: 100%;
      margin-top: 1rem;
      overflow-y: auto;

      .panel {
        text-align: start;

        .config-item {
          display: flex;
          align-items: center;
          justify-content: start;
          margin-bottom: 1rem;

          .config-item-key {
            width: 20%;
            text-align: start;
            margin-left: 2rem;
          }

          .config-item-value {
            width: 60%;
          }

          .config-item-desc {
            cursor: pointer;
            margin-left: 1rem;
          }
        }
      }
    }
  }
</style>
