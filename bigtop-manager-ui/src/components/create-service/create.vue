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
  import { computed, ref, shallowRef, watch } from 'vue'
  import useCreateService from './components/use-create-service'
  import ServiceSelector from './components/service-selector.vue'
  import ComponentAssigner from './components/component-assigner.vue'
  import ServiceConfigurator from './components/service-configurator.vue'
  import ComponentInstaller from './components/component-installer.vue'
  import { type CommandRequest } from '@/api/command/types'

  const components = shallowRef<any[]>([
    ServiceSelector,
    ComponentAssigner,
    ServiceConfigurator,
    ServiceConfigurator,
    ComponentInstaller
  ])

  const commandRequest = ref<CommandRequest>({
    command: 'Add',
    commandLevel: 'service'
  })
  const { current, stepsLimit, steps, previousStep, nextStep, serviceCommands } = useCreateService()
  const currComp = computed(() => components.value[current.value])

  watch(
    () => serviceCommands.value,
    (val) => {
      console.log('val :>> ', val)
    },
    {
      deep: true
    }
  )

  const createService = () => {
    console.log('commandRequest.value :>> ', commandRequest.value)
    console.log('serviceCommands :>> ', serviceCommands)
  }
</script>

<template>
  <div class="infra-creation">
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
          <section :class="{ 'step-content': current < stepsLimit }"> </section>
        </div>
      </template>
      <keep-alive>
        <component :is="currComp" :is-view="current === 3" />
      </keep-alive>
      <div class="step-action">
        <a-space>
          <a-button v-show="current != stepsLimit" @click="() => $router.go(-1)">{{ $t('common.exit') }}</a-button>
          <a-button v-show="current > 0 && current < stepsLimit" type="primary" @click="previousStep">
            {{ $t('common.prev') }}
          </a-button>
          <template v-if="current >= 0 && current <= stepsLimit - 1">
            <a-button type="primary" @click="nextStep">
              {{ $t('common.next') }}
            </a-button>
          </template>
          <a-button v-show="current === stepsLimit" type="primary" @click="createService">
            {{ $t('common.done') }}
          </a-button>
        </a-space>
      </div>
    </main-card>
  </div>
</template>

<style lang="scss" scoped>
  .infra-creation {
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
