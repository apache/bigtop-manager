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
  import { useMenuStore } from '@/store/menu'
  import { message, Modal } from 'ant-design-vue'
  import { computed, h, ref, shallowRef } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { getInstalledStatus, installDependencies } from '@/api/hosts'
  import { InstalledStatusVO, Status } from '@/api/hosts/types'
  import { execCommand } from '@/api/command'
  import { onBeforeRouteLeave } from 'vue-router'
  import SvgIcon from '@/components/common/svg-icon/index.vue'
  import useSteps from '@/composables/use-steps'
  import ClusterBase from './components/cluster-base.vue'
  import ComponentInfo from './components/component-info.vue'
  import HostManage from './components/host-manage.vue'
  import CheckWorkflow from './components/check-workflow.vue'
  import type { ClusterCommandReq, CommandRequest, CommandVO, HostReq } from '@/api/command/types'

  const { t } = useI18n()
  const menuStore = useMenuStore()
  const compRef = ref<any>()
  const installing = ref(false)
  const stepData = ref<[Partial<ClusterCommandReq>, any, HostReq[], CommandVO]>([{}, {}, [], {}])
  const commandRequest = ref<CommandRequest>({
    command: 'Add',
    commandLevel: 'cluster'
  })
  const installStatus = shallowRef<InstalledStatusVO[]>([])
  const components = shallowRef<any[]>([ClusterBase, ComponentInfo, HostManage, CheckWorkflow])
  const isInstall = computed(() => current.value === 2)
  const hasUnknownHost = computed(() => stepData.value[2].filter((v) => v.status === Status.Unknown).length == 0)
  const allInstallSuccess = computed(
    () =>
      stepData.value[2].length != 0 &&
      stepData.value[2].every((v) => v.status === Status.Success) &&
      hasUnknownHost.value
  )
  const getCompName = computed(() => components.value[current.value])
  const isDone = computed(() => ['Successful', 'Failed'].includes(stepData.value[stepData.value.length - 1].state))
  const steps = computed(() => [
    'cluster.cluster_info',
    'cluster.component_info',
    'cluster.host_config',
    'cluster.create'
  ])

  const { current, stepsLimit, previousStep, nextStep } = useSteps(steps.value)

  const updateData = (val: Partial<ClusterCommandReq> | any | HostReq[]) => {
    stepData.value[current.value] = val
  }

  const createCluster = async () => {
    try {
      commandRequest.value.clusterCommand = stepData.value[0] as ClusterCommandReq
      commandRequest.value.clusterCommand.hosts = stepData.value[2]
      stepData.value[stepData.value.length - 1] = await execCommand(commandRequest.value)
      return true
    } catch (error) {
      console.log('error :>> ', error)
      return false
    }
  }

  const prepareNextStep = async () => {
    if (current.value === 0) {
      const check = await compRef.value.check()
      if (check) {
        nextStep()
      }
    } else if (current.value === 2) {
      if (!allInstallSuccess.value) {
        await resolveInstallDependencies()
      } else {
        const isClusterExisting = await createCluster()
        isClusterExisting && nextStep()
      }
    } else {
      nextStep()
    }
  }

  const resolveInstallDependencies = async () => {
    if (stepData.value[2].length == 0) {
      message.error(t('host.uninstallable'))
      return
    }
    try {
      installing.value = true
      const data = await installDependencies(stepData.value[current.value])
      data && pollUntilInstalled()
    } catch (error) {
      installing.value = false
      console.log('error :>> ', error)
    }
  }

  const recordInstalledStatus = async () => {
    try {
      const data = await getInstalledStatus()
      installStatus.value = data
      stepData.value[current.value] = mergeByHostname(stepData.value[current.value], data)
      return data.every((item) => item.status != Status.Installing)
    } catch (error) {
      console.log('error :>> ', error)
    }
  }

  const pollUntilInstalled = (interval: number = 1000): void => {
    let isInitialized = false
    let intervalId: NodeJS.Timeout

    const poll = async () => {
      if (!isInitialized) {
        stepData.value[current.value] = stepData.value[current.value].map((item: HostReq) => ({
          ...item,
          status: Status.Installing
        }))
        isInitialized = true
      }
      const result = await recordInstalledStatus()
      if (result) {
        installing.value = false
        clearInterval(intervalId)
      }
    }

    intervalId = setInterval(poll, interval)
    poll()
  }

  const mergeByHostname = (arr1: any[], arr2: any[]): any[] => {
    const mergedMap = new Map<string, any>()
    for (const item of arr1) {
      mergedMap.set(item.hostname, { ...item })
    }
    for (const item of arr2) {
      if (mergedMap.has(item.hostname)) {
        mergedMap.set(item.hostname, { ...mergedMap.get(item.hostname), ...item })
      } else {
        mergedMap.set(item.hostname, { ...item })
      }
    }
    return Array.from(mergedMap.values())
  }

  // const changeStep = (step: number) => {
  //   if (current.value > step) {
  //     const previousCount = current.value - step
  //     Array.from({ length: previousCount }).forEach(() => previousStep())
  //   }
  //   if (current.value < step) {
  //     const nextCount = step - current.value
  //     Array.from({ length: nextCount }).forEach(() => prepareNextStep())
  //   }
  // }

  const onSave = () => {
    menuStore.updateSider()
  }

  onBeforeRouteLeave((_to, _from, next) => {
    if (current.value === stepsLimit.value && !isDone.value) {
      Modal.confirm({
        title: () =>
          h('div', { style: { display: 'flex' } }, [
            h(SvgIcon, { name: 'unknown', style: { width: '24px', height: '24px' } }),
            h('span', t('common.exit_confirm'))
          ]),
        content: h('div', { style: { paddingLeft: '36px' } }, t('common.installing_exit_confirm_content')),
        cancelText: t('common.no'),
        icon: null,
        okText: t('common.yes'),
        onOk: () => {
          next()
          Modal.destroyAll()
        }
      })
      return
    }
    next()
  })
</script>

<template>
  <div class="cluster-create">
    <header-card>
      <div class="steps-wrp">
        <a-steps :current="current">
          <a-step v-for="step in steps" :key="step" :disabled="true">
            <template #title>
              <span>{{ $t(step) }}</span>
            </template>
          </a-step>
        </a-steps>
      </div>
    </header-card>
    <main-card>
      <template v-for="stepItem in steps" :key="stepItem.title">
        <div v-show="steps[current] === stepItem" class="step-title">
          <h5>{{ $t(stepItem) }}</h5>
          <section :class="{ 'step-content': current < stepsLimit }"></section>
        </div>
      </template>
      <component :is="getCompName" ref="compRef" :step-data="stepData[current]" @update-data="updateData" />
      <div class="step-action">
        <a-space>
          <a-button v-show="current != stepsLimit" @click="() => $router.go(-1)">{{ $t('common.exit') }}</a-button>
          <a-button v-show="current > 0 && current < stepsLimit" type="primary" @click="previousStep">
            {{ $t('common.prev') }}
          </a-button>
          <template v-if="current >= 0 && current <= stepsLimit - 1">
            <a-button
              v-if="isInstall && !allInstallSuccess"
              type="primary"
              :disabled="installing"
              @click="prepareNextStep"
            >
              {{ $t('cluster.install_dependencies') }}
            </a-button>
            <a-button v-else type="primary" @click="prepareNextStep">
              {{ $t('common.next') }}
            </a-button>
          </template>
          <a-button v-show="current === stepsLimit && isDone" type="primary" @click="onSave"
            >{{ $t('common.done') }}
          </a-button>
        </a-space>
      </div>
    </main-card>
  </div>
</template>

<style lang="scss" scoped>
  .cluster-create {
    min-width: 600px;

    .header-card {
      min-height: 80px;
    }

    .steps-wrp {
      width: 100%;
      height: 100%;
      padding-inline: 6%;
    }
  }

  .step-title {
    h5 {
      margin: 0;
      font-size: 16px;
      font-weight: 500;
      letter-spacing: 0px;
      line-height: 16px;
    }
  }

  .step-content {
    padding-block: $space-md;
  }

  .step-action {
    text-align: end;
    margin-top: $space-md;
  }
</style>
