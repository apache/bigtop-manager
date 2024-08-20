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
  import { ref, h, watch, reactive, onUnmounted, onMounted } from 'vue'
  import { Modal } from 'ant-design-vue'
  import { ExclamationCircleFilled } from '@ant-design/icons-vue'
  import { useI18n } from 'vue-i18n'
  import ChooseServices from '@/components/service-add/choose-services.vue'
  import AssignComponents from '@/components/service-add/assign-components.vue'
  import ConfigureServices from '@/components/service-add/configure-services.vue'
  import Install from '@/components/service-add/install.vue'
  import Finish from '@/components/service-add/finish.vue'
  import { useClusterStore } from '@/store/cluster'
  import { storeToRefs } from 'pinia'
  import { useServiceStore } from '@/store/service'
  import { useComponentStore } from '@/store/component'
  import { useConfigStore } from '@/store/config'
  import { HostComponentVO } from '@/api/component/types.ts'
  import { TypeConfigVO, ServiceConfigVO } from '@/api/config/types.ts'

  const open = defineModel<boolean>('open')

  const { t, locale } = useI18n()

  const clusterStore = useClusterStore()
  const serviceStore = useServiceStore()
  const componentStore = useComponentStore()
  const configStore = useConfigStore()
  const { clusterId } = storeToRefs(clusterStore)
  const { installedServices } = storeToRefs(serviceStore)
  const { hostComponents } = storeToRefs(componentStore)
  const { latestConfigs } = storeToRefs(configStore)

  const initItems = () => [
    {
      disabled: true,
      status: 'process',
      title: t('service.choose_services'),
      content: h(ChooseServices)
    },
    {
      disabled: true,
      status: 'wait',
      title: t('service.assign_components'),
      content: h(AssignComponents)
    },
    {
      disabled: true,
      status: 'wait',
      title: t('service.configure_services'),
      content: h(ConfigureServices)
    },
    {
      disabled: true,
      status: 'wait',
      title: t('common.install'),
      content: h(Install)
    },
    {
      disabled: true,
      status: 'wait',
      title: t('common.finish'),
      content: h(Finish)
    }
  ]

  const initServiceInfo = async () => {
    await serviceStore.loadServices()
    await componentStore.loadHostComponents()
    await configStore.loadLatestConfigs()

    const serviceCommands = installedServices.value.map((item: any) => {
      const serviceName = item.serviceName
      const componentHosts: any[] = hostComponents.value
        .filter((hc: HostComponentVO) => hc.serviceName === serviceName)
        .reduce((acc: any[], hc: HostComponentVO) => {
          const existingComponent = acc.find(
            (comp) => comp.componentName === hc.componentName
          )

          if (existingComponent) {
            existingComponent.hostnames.push(hc.hostname)
          } else {
            acc.push({
              componentName: hc.componentName,
              hostnames: [hc.hostname]
            })
          }

          return acc
        }, [])

      const configs = latestConfigs.value
        .filter((sc: ServiceConfigVO) => sc.serviceName === serviceName)
        .flatMap((sc: ServiceConfigVO) => sc.configs)
        .map((cd: TypeConfigVO) => ({
          typeName: cd.typeName,
          properties: cd.properties
        }))

      return {
        serviceName: serviceName,
        installed: true,
        componentHosts: componentHosts,
        configs: configs
      }
    })

    return {
      command: 'install',
      commandLevel: 'service',
      clusterId: clusterId.value,
      serviceCommands: serviceCommands,
      // Related job id
      jobId: 0,
      // Job Status
      success: false
    }
  }

  const current = ref<number>(0)
  const items = reactive(initItems())
  const serviceInfo = reactive(await initServiceInfo())
  const disableButton = ref<boolean>(false)
  const currentItemRef = ref<any>(null)
  const loadingNext = ref<boolean>(false)
  watch(locale, () => {
    Object.assign(items, initItems())
  })

  watch(clusterId, () => {
    serviceInfo.clusterId = clusterId.value
  })

  const next = async () => {
    loadingNext.value = true
    try {
      const valid = await currentItemRef.value?.onNextStep()
      if (valid) {
        console.log('serviceInfo:', JSON.stringify(serviceInfo))
        items[current.value].status = 'finish'
        current.value++
        items[current.value].status = 'process'
      }
    } finally {
      loadingNext.value = false
    }
  }

  const prev = () => {
    items[current.value].status = 'wait'
    current.value--
    items[current.value].status = 'process'
  }

  const clear = async () => {
    // Clear status
    current.value = 0
    open.value = false
    Object.assign(items, initItems())
    Object.assign(serviceInfo, await initServiceInfo())
  }

  const cancel = () => {
    Modal.confirm({
      title: t('common.exit'),
      icon: h(ExclamationCircleFilled),
      content: t('common.exit_confirm'),
      onOk() {
        clear()
      }
    })
  }

  onMounted(() => {
    componentStore.resumeIntervalFn()
  })

  onUnmounted(() => {
    componentStore.pauseIntervalFn()
  })
</script>

<template>
  <a-modal
    :open="open"
    width="95%"
    centered
    destroy-on-close
    :mask-closable="false"
    :keyboard="false"
    @update:open="cancel"
  >
    <template #footer>
      <a-button
        v-if="current > 0"
        class="footer-btn"
        type="primary"
        :disabled="disableButton"
        @click="prev"
      >
        {{ $t('common.prev') }}
      </a-button>
      <a-button
        v-if="current < items.length - 1"
        class="footer-btn"
        type="primary"
        :loading="loadingNext"
        :disabled="disableButton"
        @click="next"
      >
        {{ $t('common.next') }}
      </a-button>
      <a-button
        v-if="current === items.length - 1"
        class="footer-btn"
        type="primary"
        @click="() => clear()"
      >
        {{ $t('common.done') }}
      </a-button>
    </template>
    <div class="container">
      <a-steps
        v-model:current="current"
        class="step"
        direction="vertical"
        size="small"
        :items="items"
      />
      <div class="content">
        <component
          :is="items[current].content"
          ref="currentItemRef"
          v-model:serviceInfo="serviceInfo"
          v-model:disableButton="disableButton"
        />
      </div>
    </div>
  </a-modal>
</template>

<style scoped lang="scss">
  .container {
    @include flexbox($direction: row, $justify: center, $align: center);

    .step {
      width: 15%;
      height: 35rem;
    }

    .content {
      margin-top: 1rem;
      padding-left: 1rem;
      height: 35rem;
      width: 85%;
      text-align: center;
      border-left: 1px solid #d9d9d9;
    }
  }

  .footer-btn {
    width: 8.333333%;
  }
</style>
