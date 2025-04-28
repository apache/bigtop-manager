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
  import { computed, onUnmounted, ref, shallowRef } from 'vue'
  import { message } from 'ant-design-vue'
  import { useI18n } from 'vue-i18n'
  import useCreateService from './components/use-create-service'
  import ServiceSelector from './components/service-selector.vue'
  import ComponentAssigner from './components/component-assigner.vue'
  import ServiceConfigurator from './components/service-configurator.vue'
  import ComponentInstaller from './components/component-installer.vue'

  const { t } = useI18n()
  const components = shallowRef<any[]>([ServiceSelector, ComponentAssigner, ServiceConfigurator, ServiceConfigurator])
  const {
    scope,
    current,
    stepsLimit,
    steps,
    allComps,
    allCompsMeta,
    creationModeType,
    afterCreateRes,
    selectedServices,
    setDataByCurrent,
    previousStep,
    nextStep,
    createService,
    confirmServiceDependencies,
    addComponentForService
  } = useCreateService()
  const compRef = ref<any>()
  const currComp = computed(() => components.value[current.value])
  const noUninstallComponent = computed(() => {
    return (
      current.value === 1 &&
      creationModeType.value === 'component' &&
      Array.from(allComps.value).every(
        ([compName, { hosts }]) => hosts.length === allCompsMeta.value.get(compName)?.hosts?.length
      )
    )
  })

  const validateServiceSelection = async () => {
    if (creationModeType.value === 'component') {
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
      const serviceDependencies = await confirmServiceDependencies('add', selectedService)
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
    const allComponents = Array.from(allComps.value.values())
    const isValid = allComponents.every((comp) => comp?.hosts?.length > 0)
    if (!isValid) {
      message.error(t('service.component_host_assignment'))
    }
    return isValid
  }

  const stepValidators = [validateServiceSelection, validateComponentAssignments, () => true, () => true]

  const proceedToNextStep = async () => {
    if (current.value < 3) {
      ;(await stepValidators[current.value]()) && nextStep()
    } else if (current.value === 3) {
      const state = creationModeType.value === 'component' ? await addComponentForService() : await createService()
      if (state) {
        nextStep()
      }
    }
  }

  onUnmounted(() => {
    scope.stop()
    setDataByCurrent([])
  })
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
          <section :class="{ 'step-content': current < stepsLimit }"></section>
        </div>
      </template>
      <keep-alive>
        <component
          :is="currComp"
          ref="compRef"
          :is-view="current === 3"
          v-bind="{ ...$route.params, creationMode: $route.params.creationMode || 'internal' }"
        />
      </keep-alive>
      <component-installer v-if="current == stepsLimit" :step-data="afterCreateRes" />
      <div class="step-action">
        <a-space>
          <a-button v-show="current != stepsLimit" @click="() => $router.go(-1)">{{ $t('common.exit') }}</a-button>
          <a-button v-show="current > 0 && current < stepsLimit" type="primary" @click="previousStep">
            {{ $t('common.prev') }}
          </a-button>
          <template v-if="current >= 0 && current <= stepsLimit - 1">
            <a-button :disabled="noUninstallComponent" type="primary" @click="proceedToNextStep">
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
