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
  import { computed, ref } from 'vue'
  import ClusterBase from './components/cluster-base.vue'
  import ComponentInfo from './components/component-info.vue'
  import HostConfig from './components/host-config.vue'
  import CheckWorkflow from './components/check-workflow.vue'
  import { useI18n } from 'vue-i18n'
  import { ClusterCommandReq, Command, CommandLevel, CommandRequest } from '@/api/command/types'

  const { t } = useI18n()
  const menuStore = useMenuStore()
  const compRef = ref<any>()

  const commandRequest = ref<CommandRequest>({
    command: Command.Add,
    commandLevel: CommandLevel.Cluster
  })
  const current = ref(0)
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

  const updateCommandRequest = (value: unknown) => {
    if (current.value === 0) {
      commandRequest.value.clusterCommand = value as ClusterCommandReq
    }
  }

  const previousStep = () => {
    if (current.value > 0) {
      current.value = current.value - 1
    }
  }
  const nextStep = async () => {
    if (!compRef.value?.check) {
      if (current.value < stepsLimit.value) {
        current.value = current.value + 1
      }
    } else {
      const res = await compRef.value?.check()
      if (res !== -1) {
        updateCommandRequest(res)
        if (current.value < stepsLimit.value) {
          current.value = current.value + 1
        }
      }
    }
  }

  const onSave = () => {
    menuStore.updateSiderMenu()
  }
</script>

<template>
  <div class="cluster-create">
    {{ commandRequest }}
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
      <component :is="getCompName" ref="compRef" :command-request="commandRequest" />
      <div class="step-action">
        <a-space>
          <a-button v-show="current != stepsLimit" @click="() => $router.go(-1)">{{ $t('common.exit') }}</a-button>
          <a-button v-show="current > 0 && current < stepsLimit" type="primary" @click="previousStep">
            {{ $t('common.prev') }}
          </a-button>
          <a-button v-show="current >= 0 && current <= stepsLimit - 1" type="primary" @click="nextStep">
            {{ $t('common.next') }}
          </a-button>
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
