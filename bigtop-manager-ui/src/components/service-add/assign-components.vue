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
  import { computed, onMounted, ref } from 'vue'
  import { DEFAULT_PAGE_SIZE } from '@/utils/constant.ts'
  import { useHostStore } from '@/store/host'
  import { useComponentStore } from '@/store/component'
  import { storeToRefs } from 'pinia'
  import { useStackStore } from '@/store/stack'
  import _ from 'lodash'
  import { ComponentVO, ServiceComponentVO } from '@/api/component/types.ts'
  import { arrayEquals } from '@/utils/array.ts'

  const serviceInfo = defineModel<any>('serviceInfo')

  const hostStore = useHostStore()
  const stackStore = useStackStore()
  const componentStore = useComponentStore()
  const { hosts, loading } = storeToRefs(hostStore)
  const { currentStack, stackComponents } = storeToRefs(stackStore)
  const { hostComponents } = storeToRefs(componentStore)
  const pageSize = ref<number>(DEFAULT_PAGE_SIZE)

  const serviceNameToDisplayName = _.fromPairs(
    currentStack.value.services.map((v) => [v.serviceName, v.displayName])
  )

  const hostnames = computed(() => hosts.value.map((v) => v.hostname))

  const columns = computed(() => {
    const services = serviceInfo.value.serviceCommands.map((item: any) => {
      return {
        title: serviceNameToDisplayName[item.serviceName],
        align: 'center',
        children: stackComponents.value
          .filter(
            (component: ServiceComponentVO) =>
              component.serviceName === item.serviceName
          )
          .flatMap((component: ServiceComponentVO) => component.components)
          .map((component: ComponentVO) => {
            return {
              title: component.displayName,
              dataIndex: component.componentName,
              align: 'center',
              width: 180
            }
          })
      }
    })

    return [
      {
        title: 'hosts.host',
        dataIndex: 'host',
        align: 'center',
        fixed: true,
        width: 200
      },
      ...services
    ]
  })

  const data = computed(() => hosts.value.map((v) => ({ host: v.hostname })))
  const pagination = computed(() => {
    return {
      pageSize: pageSize.value,
      showSizeChanger: true,
      onShowSizeChange: (_current: number, size: number) => {
        pageSize.value = size
      }
    }
  })

  const checkedComponents = computed(() => {
    return serviceInfo.value.serviceCommands
      .flatMap((item: any) => item.componentHosts)
      .reduce((acc: any, item: any) => {
        acc[item.componentName] = item.hostnames
        return acc
      }, {})
  })

  const disabledComponents = _.uniq(
    hostComponents.value.map((item) => item.componentName)
  )

  const checkComponent = (record: any, column: any) => {
    const host = record.host
    const componentName = column.dataIndex

    serviceInfo.value.serviceCommands.forEach((serviceCommand: any) => {
      serviceCommand.componentHosts.forEach((componentHost: any) => {
        if (componentHost.componentName === componentName) {
          if (componentHost.hostnames.includes(host)) {
            _.remove(componentHost.hostnames, (item: any) => item === host)
          } else {
            componentHost.hostnames.push(host)
          }
        }
      })
    })
  }

  const checkGroup = (column: any) => {
    const componentName = column.dataIndex
    serviceInfo.value.serviceCommands.forEach((serviceCommand: any) => {
      serviceCommand.componentHosts.forEach((componentHost: any) => {
        if (componentHost.componentName === componentName) {
          if (componentHost.hostnames.length > 0) {
            componentHost.hostnames = []
          } else {
            componentHost.hostnames = _.cloneDeep(hostnames.value)
          }
        }
      })
    })
  }

  onMounted(() => {
    hostStore.refreshHosts()
  })

  const onNextStep = async () => {
    return Promise.resolve(true)
  }

  defineExpose({
    onNextStep
  })
</script>

<template>
  <div class="container">
    <div class="title">{{ $t('service.assign_components') }}</div>
    <a-table
      :loading="loading"
      :columns="columns"
      :data-source="data"
      :scroll="{ x: 'max-content', y: 330 }"
      :pagination="pagination"
    >
      <template #headerCell="{ column }">
        <span v-if="column.dataIndex === 'host'">{{ $t(column.title) }}</span>
        <template v-if="column.dataIndex !== 'host' && !column.children">
          <a-checkbox
            :disabled="disabledComponents.includes(column.dataIndex)"
            :indeterminate="
              !arrayEquals(checkedComponents[column.dataIndex], hostnames) &&
              checkedComponents[column.dataIndex].length > 0
            "
            :checked="
              arrayEquals(checkedComponents[column.dataIndex], hostnames)
            "
            @click="checkGroup(column)"
          >
            {{ column.title }}
          </a-checkbox>
        </template>
      </template>

      <template #bodyCell="{ record, column }">
        <template v-if="column.dataIndex !== 'host'">
          <a-checkbox
            :checked="checkedComponents[column.dataIndex].includes(record.host)"
            :disabled="disabledComponents.includes(column.dataIndex)"
            @click="checkComponent(record, column)"
          />
        </template>
      </template>
    </a-table>
  </div>
</template>

<style scoped lang="scss">
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
  }
</style>
