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
  import { computed, ref, shallowRef } from 'vue'
  import ClusterBase from './components/cluster-base.vue'
  import ComponentInfo from './components/component-info.vue'
  import HostConfig from './components/host-config.vue'
  import CheckWorkflow from './components/check-workflow.vue'
  import { useI18n } from 'vue-i18n'
  import { getInstalledStatus, installDependencies } from '@/api/hosts'
  import { message } from 'ant-design-vue'
  import { InstalledStatusVO, Status } from '@/api/hosts/types'
  import { Command, CommandLevel, CommandRequest } from '@/api/command/types'
  import { execCommand } from '@/api/command'

  const { t } = useI18n()
  const menuStore = useMenuStore()
  const compRef = ref<any>()

  const commandRequest = ref<CommandRequest>({
    command: Command.Add,
    commandLevel: CommandLevel.Cluster
  })
  const installing = ref(false)
  const current = ref(0)
  const stepData = ref([{}, {}, []])
  const installStatus = shallowRef<InstalledStatusVO[]>([])
  const hasUnknowHost = computed(() => (stepData.value[2] as any[]).every((v) => v.status === Status.Success))
  const allInstallSuccess = computed(
    () =>
      installStatus.value.length != 0 &&
      installStatus.value.every((v) => v.status === Status.Success) &&
      hasUnknowHost.value
  )
  const isInstall = computed(() => current.value === 2)
  const steps = computed(() => [
    {
      title: t('cluster.cluster_management')
    },
    {
      title: t('cluster.component_info')
    },
    {
      title: t('cluster.host_config')
    },
    {
      title: t('cluster.create')
    }
  ])
  const stepsLimit = computed(() => steps.value.length - 1)
  const getCompName = computed(() => {
    if (current.value === 0) {
      return ClusterBase
    } else if (current.value === 1) {
      return ComponentInfo
    } else if (current.value === 2) {
      return HostConfig
    } else if (current.value === 3) {
      return CheckWorkflow
    } else {
      return CheckWorkflow
    }
  })

  const updateData = (val: unknown) => {
    stepData.value[current.value] = val as any
  }

  const addCluster = async () => {
    try {
      commandRequest.value.clusterCommand = stepData.value[0] as any
      commandRequest.value.clusterCommand!.hosts = stepData.value[2] as any[]
      await execCommand(commandRequest.value)
    } catch (error) {
      console.log('error :>> ', error)
    }
  }

  const previousStep = () => {
    if (current.value > 0) {
      current.value = current.value - 1
    }
  }

  const nextStepPre = async () => {
    if (current.value === 0) {
      const check = await compRef.value.check()
      if (check) {
        nextStep()
      }
    } else if (current.value === 2) {
      !allInstallSuccess.value ? await resolveInstallDependencies() : nextStep()
    } else {
      nextStep()
    }
  }

  const nextStep = async () => {
    current.value === 2 && addCluster()
    if (current.value < stepsLimit.value) {
      current.value = current.value + 1
    }
  }

  const resolveInstallDependencies = async () => {
    if ((stepData.value[2] as any[]).length == 0) {
      message.error(t('host.uninstallable'))
      return
    }
    try {
      installing.value = true
      const data = await installDependencies(stepData.value[current.value])
      data && pollUntilSuccess()
    } catch (error) {
      installing.value = false
      console.log('error :>> ', error)
    }
  }

  const recordInstalledStatus = async () => {
    try {
      const data = await getInstalledStatus()
      installStatus.value = data
      stepData.value[current.value] = mergeByHostname(stepData.value[current.value] as any[], data)
      const allResolved = data.every((item) => item.status != Status.Installing)
      if (allResolved) {
        return true
      } else {
        return false
      }
    } catch (error) {
      console.log('error :>> ', error)
    }
  }

  const pollUntilSuccess = (interval: number = 2000): void => {
    const intervalId = setInterval(async () => {
      const result = await recordInstalledStatus()
      if (result) {
        installing.value = false
        clearInterval(intervalId)
      }
    }, interval)
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

  const onSave = () => {
    menuStore.updateSiderMenu()
  }
</script>

<template>
  <div class="cluster-create">
    {{ stepData }}
    <header-card>
      <div class="steps-wrp">
        <a-steps :current="current" :items="steps" />
      </div>
    </header-card>
    <main-card>
      <template v-for="stepItem in steps" :key="stepItem.title">
        <div v-show="steps[current].title === stepItem.title" class="step-title">
          <h5>{{ stepItem.title }}</h5>
          <section :class="{ 'step-content': current < stepsLimit }"> </section>
        </div>
      </template>
      <!-- <keep-alive> -->
      <component :is="getCompName" ref="compRef" :step-data="stepData[current]" @update-data="updateData" />
      <!-- </keep-alive> -->
      <div class="step-action">
        <a-space>
          <a-button v-show="current != stepsLimit" @click="() => $router.go(-1)">{{ $t('common.exit') }}</a-button>
          <a-button v-show="current > 0 && current < stepsLimit" type="primary" @click="previousStep">
            {{ $t('common.prev') }}
          </a-button>
          <template v-if="current >= 0 && current <= stepsLimit - 1">
            <a-button v-if="isInstall && !allInstallSuccess" type="primary" :disabled="installing" @click="nextStepPre">
              {{ $t('cluster.install_dependencies') }}
            </a-button>
            <a-button v-else type="primary" @click="nextStepPre">
              {{ $t('common.next') }}
            </a-button>
          </template>
          <a-button v-show="current === stepsLimit" type="primary" @click="onSave">{{ $t('common.done') }}</a-button>
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
