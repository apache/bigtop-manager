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
  import { computed, ref } from 'vue'
  import { storeToRefs } from 'pinia'
  import { useStackStore } from '@/store/stack'
  import { execCommand } from '@/api/command'
  import _ from 'lodash'

  const serviceInfo = defineModel<any>('serviceInfo')

  const stackStore = useStackStore()
  const { currentStack } = storeToRefs(stackStore)

  const activeServiceTab = ref(serviceInfo.value.serviceCommands[0].serviceName)

  const serviceNameToDisplayName = _.fromPairs(
    currentStack.value.services.map((v) => [v.serviceName, v.displayName])
  )

  const services = computed(() => {
    return serviceInfo.value.serviceCommands.map((item: any) => {
      return item.serviceName
    })
  })

  const configs = computed(() => {
    return serviceInfo.value.serviceCommands
      .map((item: any) => {
        return { [item.serviceName]: item.configs }
      })
      .reduce((acc: any, curr: any) => {
        return { ...acc, ...curr }
      }, {})
  })

  const activeConfigTab = ref(configs.value[activeServiceTab.value][0].typeName)

  const onNextStep = async () => {
    try {
      const res = await execCommand(serviceInfo.value)
      serviceInfo.value.jobId = res.id
    } catch (e) {
      console.log(e)
      return Promise.resolve(false)
    }

    return Promise.resolve(true)
  }

  defineExpose({
    onNextStep
  })
</script>

<template>
  <div class="container">
    <div class="title">{{ $t('service.configure_services') }}</div>
    <a-tabs
      v-model:activeKey="activeServiceTab"
      class="content"
      @change="(key: string) => (activeConfigTab = configs[key][0].typeName)"
    >
      <a-tab-pane
        v-for="service in services"
        :key="service"
        :tab="serviceNameToDisplayName[service]"
      >
        <a-collapse v-model:activeKey="activeConfigTab" ghost>
          <a-collapse-panel
            v-for="config in configs[activeServiceTab]"
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
  </div>
</template>

<style scoped lang="scss">
  .container {
    @include flexbox($direction: column, $justify: start, $align: center);
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
      overflow-y: auto;

      .panel {
        text-align: start;

        .config-item {
          @include flexbox($justify: start, $align: center);
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
