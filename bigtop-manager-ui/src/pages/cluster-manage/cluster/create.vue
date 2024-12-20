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
  // import { useMenuStore } from '@/store/menu'
  import { computed, ref } from 'vue'
  import ClusterBase from './components/cluster-base.vue'
  import ComponentInfo from './components/component-info.vue'
  import HostConfig from './components/host-config.vue'
  import { useI18n } from 'vue-i18n'

  const { t } = useI18n()
  // const menuStore = useMenuStore()
  // const onSave = () => {
  //   menuStore.updateSiderMenu()
  // }
  // const onDel = () => {
  //   menuStore.updateSiderMenu(true)
  // }

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
    } else {
      return HostConfig
    }
  })

  const previousStep = () => {
    if (current.value > 0) {
      current.value = current.value - 1
    }
  }
  const nextStep = () => {
    if (current.value < stepsLimit.value) {
      current.value = current.value + 1
    }
  }
</script>

<template>
  <div class="cluster-create">
    <header-card>
      <a-steps :current="current" :items="steps"></a-steps>
    </header-card>
    <main-card>
      <template v-for="stepItem in steps" :key="stepItem.title">
        <div v-show="steps[current].title === stepItem.title">
          <a-typography-text strong :content="stepItem.title" />
          <section class="step-content">
            <component :is="getCompName" />
          </section>
        </div>
      </template>
      <div class="step-action">
        <a-space>
          <a-button>{{ $t('common.exit') }}</a-button>
          <a-button type="primary" @click="previousStep">{{ $t('common.prev') }}</a-button>
          <a-button type="primary" @click="nextStep">{{ $t('common.next') }}</a-button>
          <a-button type="primary">{{ $t('common.done') }}</a-button>
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
  }
  .step-content {
    padding-block: $space-md;
  }
  .step-action {
    text-align: end;
    margin-top: $space-md;
  }
</style>
