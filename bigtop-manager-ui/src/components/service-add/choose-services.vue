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
import {storeToRefs} from 'pinia'
import {TableProps} from 'ant-design-vue'
import {useServiceStore} from '@/store/service'
import {MergedServiceVO} from '@/store/service/types.ts'
import {onMounted} from 'vue'
import {ServiceVO} from '@/api/service/types.ts'
import {useStackStore} from '@/store/stack'
import {ComponentVO, ServiceComponentVO} from '@/api/component/types.ts'
import {ServiceConfigVO, TypeConfigVO} from '@/api/config/types.ts'
import _ from 'lodash'

const serviceInfo = defineModel<any>('serviceInfo')
  const disableButton = defineModel<boolean>('disableButton')

  const stackStore = useStackStore()
  const serviceStore = useServiceStore()
  const { stackComponents, stackConfigs } = storeToRefs(stackStore)
  const { installedServices, mergedServices } = storeToRefs(serviceStore)

  const defaultSelected = serviceInfo.value.serviceCommands.map(
    (item: any) => item.serviceName
  )

  const installedServiceNames = installedServices.value.map(
    (item: ServiceVO) => item.serviceName
  )

  const serviceColumns = [
    {
      title: 'service.service',
      dataIndex: 'displayName',
      align: 'center',
      width: 150
    },
    {
      title: 'common.version',
      dataIndex: 'serviceVersion',
      align: 'center',
      width: 150
    },
    {
      title: 'common.desc',
      dataIndex: 'serviceDesc',
      align: 'center'
    }
  ]

  const newServiceCommand = (serviceName: string) => {
    const componentHosts = stackComponents.value
      .filter((item: ServiceComponentVO) => item.serviceName === serviceName)
      .flatMap((item: ServiceComponentVO) => item.components)
      .map((item: ComponentVO) => ({
        componentName: item.componentName,
        hostnames: []
      }))

    const configs = stackConfigs.value
      .filter((item: ServiceConfigVO) => item.serviceName === serviceName)
      .flatMap((item: ServiceConfigVO) => item.configs)
      .map((item: TypeConfigVO) => ({
        typeName: item.typeName,
        properties: item.properties
      }))

    return {
      serviceName: serviceName,
      installed: false,
      componentHosts: componentHosts,
      configs: configs
    }
  }

  const rowSelection: TableProps['rowSelection'] = {
    defaultSelectedRowKeys: defaultSelected,
    onChange: (v: (string | number)[]) => {
      const existingServices = serviceInfo.value.serviceCommands.map(
        (item: any) => item.serviceName
      )

      if (v.length > existingServices.length) {
        // select a new service
        v.map((item: string | number) => {
          if (!existingServices.includes(item)) {
            serviceInfo.value.serviceCommands.push(
              newServiceCommand(item as string)
            )
          }
        })
      } else {
        // unselect a service
        _.remove(
          serviceInfo.value.serviceCommands,
          (item: any) => !v.includes(item.serviceName)
        )
      }

      disableButton.value = _.isEqual(v, installedServiceNames)
    },
    getCheckboxProps: (record: MergedServiceVO) => ({
      disabled: record.installed
    })
  }

  onMounted(async () => {
    disableButton.value = _.isEqual(defaultSelected, installedServiceNames)
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
    <div class="title">{{ $t('service.choose_services') }}</div>
    <a-table
      row-key="serviceName"
      :columns="serviceColumns"
      :data-source="mergedServices"
      :row-selection="rowSelection"
      :pagination="false"
      :scroll="{ y: 400 }"
    >
      <template #headerCell="{ column }">
        <span>{{ $t(column.title) }}</span>
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
