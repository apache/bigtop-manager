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
  import { storeToRefs } from 'pinia'
  import { useRoute } from 'vue-router'
  import {
    computed,
    onMounted,
    ref,
    watch,
    createVNode,
    onUnmounted
  } from 'vue'

  import { useI18n } from 'vue-i18n'
  import { CommonState, CurrState } from '@/enums/state'
  import { type SelectProps, type MenuProps, Modal } from 'ant-design-vue'
  import { DownOutlined, QuestionCircleOutlined } from '@ant-design/icons-vue'

  import { useConfigStore } from '@/store/config'
  import { useComponentStore } from '@/store/component'
  import { useServiceStore } from '@/store/service'
  import { useClusterStore } from '@/store/cluster'

  import type { ServiceVO } from '@/api/service/types.ts'
  import { ServiceConfigVO, TypeConfigVO } from '@/api/config/types.ts'
  import { HostComponentVO } from '@/api/component/types.ts'
  import { execCommand } from '@/api/command'

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
      dicText: 'service.start_service'
    },
    {
      key: '2',
      action: 'stop',
      dicText: 'service.stop_service'
    },
    {
      key: '3',
      action: 'restart',
      dicText: 'service.restart_service'
    }
  ]

  const { t } = useI18n()
  const route = useRoute()
  const configStore = useConfigStore()
  const componentStore = useComponentStore()
  const serviceStore = useServiceStore()
  const clusterStore = useClusterStore()

  const { allConfigs } = storeToRefs(configStore)
  const { hostComponents } = storeToRefs(componentStore)
  const { installedServices } = storeToRefs(serviceStore)
  const { clusterId } = storeToRefs(clusterStore)

  const serviceConfigDesc = ref<SelectProps['options']>([])
  const activeSelect = ref()
  const currentConfigs = ref<TypeConfigVO[]>([])
  const currentHostComponent = ref<HostComponentVO[]>([])
  const serviceName = ref<string>(route.params.serviceName as string)
  const currentConfigVersion = ref<number>()
  const initConfigVersion = ref<number>()
  const showConfigTip = ref<boolean>(false)
  const activeConfig = ref()
  const menuClicked = ref<Menu>()
  const jobWindowOpened = ref(false)

  const selectedService = computed(() => {
    return installedServices.value.filter(
      (service: ServiceVO) => service.serviceName === serviceName.value
    )[0]
  })

  watch(hostComponents, (newVal) => {
    currentHostComponent.value = newVal.filter(
      (hc: HostComponentVO) => hc.serviceName === serviceName.value
    )
  })

  watch(currentConfigs, (newVal) => {
    activeConfig.value = newVal.length > 0 ? newVal[0].typeName : null
  })

  watch(allConfigs, (newVal) => {
    allConfigs.value = newVal
    loadCurrentConfigs()
  })

  watch(
    () => route.params,
    (params) => {
      serviceName.value = params.serviceName as string
      initServiceMeta()
      componentStore.resumeIntervalFn()
    }
  )

  const loadCurrentConfigs = () => {
    serviceConfigDesc.value = allConfigs.value.reduce(
      (pre, sc: ServiceConfigVO) => {
        if (sc.serviceName === serviceName.value) {
          pre?.push({
            value: sc.version,
            label: `Version: ${sc.version}`,
            title: `${sc.configDesc}
                  \n${sc.createTime}`
          })
        }
        return pre
      },
      [] as SelectProps['options']
    )
    currentConfigVersion.value = serviceConfigDesc.value?.[0].value as number
    currentConfigs.value = allConfigs.value.reduce(
      (pre, sc: ServiceConfigVO) => {
        if (
          sc.serviceName === serviceName.value &&
          currentConfigVersion.value === sc.version
        ) {
          const typeConfigs = sc.configs.map(({ typeName, properties }) => ({
            typeName,
            properties
          }))
          pre.push(...typeConfigs)
        }
        return pre
      },
      [] as TypeConfigVO[]
    )
  }

  const handleChange: SelectProps['onChange'] = (value) => {
    if (typeof value === 'object' && 'key' in value) {
      currentConfigVersion.value = Number(value.key)
    }
    showConfigTip.value = currentConfigVersion.value !== initConfigVersion.value
    loadCurrentConfigs()
  }

  const handleMenuClick: MenuProps['onClick'] = (e) => {
    const menu = menuOps.find((menu) => menu.key == e.key)
    const text = `${menu?.dicText}_warn`
    menuClicked.value = menu
    Modal.confirm({
      title: t('common.confirm'),
      icon: createVNode(QuestionCircleOutlined),
      content: t(text, [selectedService.value.displayName]),
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
        serviceCommands: [
          {
            serviceName: serviceName.value
          }
        ]
      })
      jobWindowOpened.value = true
    } catch (error) {
      console.log('error :>> ', error)
    }
  }

  const initServiceMeta = async () => {
    await configStore.loadAllConfigs()
    showConfigTip.value = false
    activeSelect.value = serviceConfigDesc.value?.[0]
    currentConfigVersion.value = serviceConfigDesc.value?.[0].value as number
    initConfigVersion.value = serviceConfigDesc.value?.[0].value as number
  }

  const stateColor = (type: keyof typeof CurrState) => {
    if ([0, 1].includes(CurrState[type])) {
      return CommonState['normal']
    } else if (CurrState[type] === 2) {
      return CommonState['maintained']
    }
    return CommonState['abnormal']
  }

  onMounted(() => {
    initServiceMeta()
  })

  onUnmounted(() => {
    componentStore.pauseIntervalFn()
  })
</script>

<template>
  <div>
    <a-tabs>
      <template #rightExtra>
        <a-dropdown :trigger="['click']" placement="bottomRight">
          <template #overlay>
            <a-menu @click="handleMenuClick">
              <a-menu-item v-for="item in menuOps" :key="item.key">
                <svg-icon :name="item.action" />
                <span>{{ $t(item.dicText) }}</span>
              </a-menu-item>
            </a-menu>
          </template>
          <a-button type="primary">
            {{ $t('common.action') }}
            <down-outlined />
          </a-button>
        </a-dropdown>
      </template>
      <a-tab-pane key="summary">
        <template #tab>{{ $t('service.summary') }}</template>
        <a-layout-content class="summary-layout">
          <div class="left-section">
            <h2>{{ $t('service.components') }}</h2>
            <div v-if="currentHostComponent.length > 0" class="summary-ctx">
              <template
                v-for="item in currentHostComponent"
                :key="`${item.id}`"
              >
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
                      <dot-state
                        :color="stateColor(item.state)"
                        class="dot-rest"
                      >
                        <span class="comp-state-text">{{ item.state }}</span>
                      </dot-state>
                    </div>
                  </footer>
                </div>
              </template>
            </div>
          </div>
          <div class="right-section">
            <h2>{{ $t('service.quick_links') }}</h2>
            <ul
              v-if="
                selectedService.quickLinks &&
                selectedService.quickLinks.length > 0
              "
            >
              <li v-for="link in selectedService.quickLinks" :key="link.url">
                <a :href="link.url" target="_blank">{{ link.displayName }}</a>
              </li>
            </ul>
            <div v-else class="no-link-text">{{ $t('service.no_link') }}</div>
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
              <a-tooltip class="config-item-value">
                <template #title>
                  {{ property.desc }}
                </template>
                <a-textarea
                  v-if="property.attrs && property.attrs.type === 'longtext'"
                  v-model:value="property.value"
                  :rows="10"
                />
                <a-input v-else v-model:value="property.value" />
              </a-tooltip>
            </div>
          </a-collapse-panel>
        </a-collapse>
      </a-tab-pane>
    </a-tabs>
    <job v-model:visible="jobWindowOpened" />
  </div>
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

      .no-link-text {
        color: #999999;
        padding: 0.25rem;
      }

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
            width: 75%;
          }
        }
      }
    }
  }
</style>
