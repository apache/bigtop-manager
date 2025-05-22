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
  import { computed, onUnmounted, ref, shallowRef, watch } from 'vue'
  import { message } from 'ant-design-vue'
  import { useI18n } from 'vue-i18n'
  import { storeToRefs } from 'pinia'
  import { useRoute } from 'vue-router'
  import { StepContext, useCreateServiceStore } from '@/store/create-service'
  import ServiceSelector from './components/service-selector.vue'
  import ComponentAssigner from './components/component-assigner.vue'
  import ServiceConfigurator from './components/service-configurator.vue'
  import ComponentInstaller from './components/component-installer.vue'

  const { t } = useI18n()
  const route = useRoute()
  const createStore = useCreateServiceStore()
  const { current, stepsLimit, stepContext, selectedServices, createdPayload } = storeToRefs(createStore)

  const compRef = ref<any>()
  const components = shallowRef<any[]>([ServiceSelector, ComponentAssigner, ServiceConfigurator, ServiceConfigurator])
  const currComp = computed(() => components.value[current.value])

  const validateServiceSelection = async () => {
    if (stepContext.value.type === 'component') {
      return true
    }

    if (selectedServices.value.filter((v) => !v.isInstalled).length === 0) {
      message.error(t('service.service_selection'))
      return false
    } else {
      return await validateDependenciesOfServiceSelection()
    }
  }

  const validateDependenciesOfServiceSelection = async () => {
    let selectedServiceNames = new Set(selectedServices.value.map((service) => service.name))
    for (const selectedService of selectedServices.value) {
      const serviceDependencies = await createStore.confirmServiceDependencyAction('add', selectedService)
      if (serviceDependencies.length === 0) {
        return false
      }
      for (const service of serviceDependencies) {
        if (!selectedServiceNames.has(service.name)) {
          compRef.value.modifyInstallItems('add', service)
          selectedServiceNames.add(service.name)
        }
      }
    }
    return true
  }

  const validateComponentAssignments = () => {
    let valid = true
    for (const info of createStore.allComps.values()) {
      if (!info.cardinality) {
        continue
      }
      valid = createStore.validCardinality(info.cardinality, info.hosts.length, info.displayName!)
      if (!valid) {
        return
      }
    }
    return valid
  }

  const stepValidators = [validateServiceSelection, validateComponentAssignments, () => true, () => true]

  const proceedToNextStep = async () => {
    const { type } = stepContext.value
    if (current.value < 3 && (await stepValidators[current.value]())) {
      createStore.nextStep()
    } else if (current.value === 3) {
      const action = type === 'component' ? createStore.attachComponentToService : createStore.createService
      const state = await action()
      if (state) {
        createStore.nextStep()
      }
    }
  }

  const setupStepCtx = () => {
    const { id: clusterId, serviceId, creationMode, type } = route.params as StepContext
    createStore.setStepContext({ clusterId, serviceId, creationMode, type })
  }

  watch(
    () => route,
    () => {
      createStore.$reset()
      setupStepCtx()
    },
    {
      immediate: true
    }
  )

  onUnmounted(() => {
    createStore.$reset()
  })
</script>

<template>
  <div class="infra-creation">
    <header-card>
      <div class="steps-wrp">
        <a-steps :current="current">
          <a-step v-for="step in createStore.steps" :key="step" :disabled="true">
            <template #title>
              <span>{{ $t(step) }}</span>
            </template>
          </a-step>
        </a-steps>
      </div>
    </header-card>
    <main-card>
      <template v-for="stepItem in createStore.steps" :key="stepItem.title">
        <div v-show="createStore.steps[current] === stepItem" class="step-title">
          <h5>{{ $t(stepItem) }}</h5>
          <section :class="{ 'step-content': current < stepsLimit }"></section>
        </div>
      </template>
      <keep-alive>
        <component :is="currComp" ref="compRef" :is-view="current === 3" />
      </keep-alive>
      <component-installer v-if="current == stepsLimit" :step-data="createdPayload" />
      <div class="step-action">
        <a-space>
          <a-button v-show="current != stepsLimit" @click="() => $router.go(-1)">{{ $t('common.exit') }}</a-button>
          <a-button v-show="current > 0 && current < stepsLimit" type="primary" @click="createStore.previousStep">
            {{ $t('common.prev') }}
          </a-button>
          <template v-if="current >= 0 && current <= stepsLimit - 1">
            <a-button :disabled="false" type="primary" @click="proceedToNextStep">
              {{ $t('common.next') }}
            </a-button>
          </template>
          <a-button v-show="current === stepsLimit" type="primary" @click="$router.go(-1)">
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
      line-height: 24px;
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
