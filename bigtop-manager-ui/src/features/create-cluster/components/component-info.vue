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
  import { useStackStore } from '@/store/stack'
  import { ServiceVO } from '@/api/service/types'

  import SetSource from '@/features/set-source/index.vue'
  import type { TableColumnType } from 'ant-design-vue'

  const { t } = useI18n()
  const stackStore = useStackStore()
  const { stacks } = storeToRefs(stackStore)

  const data = ref<ServiceVO[]>([])
  const stackSelected = ref('bigtop')
  const setSourceRef = ref<InstanceType<typeof SetSource>>()
  const stackGroup = shallowRef(['bigtop', 'infra', 'extra'])
  const currentStack = computed(() => stacks.value.find((stack) => stack.stackName === stackSelected.value))
  const columns = computed((): TableColumnType[] => [
    {
      title: '#',
      width: '48px',
      key: 'index',
      customRender: ({ index }) => {
        return `${index + 1}`
      }
    },
    {
      title: t('common.name'),
      dataIndex: 'displayName',
      width: '20%',
      ellipsis: true
    },
    {
      title: t('common.version'),
      dataIndex: 'version',
      width: '15%',
      ellipsis: true
    },
    {
      key: 'stack',
      title: t('common.stack'),
      dataIndex: 'stack',
      width: '20%',
      ellipsis: true
    },
    {
      title: t('common.desc'),
      dataIndex: 'desc',
      ellipsis: true
    }
  ])

  const { loading, paginationProps, onChange, resetState } = useBaseTable({
    columns: columns.value,
    rows: data.value
  })

  watchEffect(() => {
    resetState()
    data.value = stacks.value.find((v) => v.stackName === stackSelected.value)?.services || []
  })

  const handleSetSource = () => {
    setSourceRef.value?.handleOpen()
  }
</script>

<template>
  <div class="component-info">
    <header>
      <a-radio-group v-model:value="stackSelected" button-style="solid">
        <a-radio-button v-for="(stack, idx) in stackGroup" :key="idx" :value="stack">
          {{ stackGroup[idx].replace(/^\w/, (c) => c.toUpperCase()) }}
        </a-radio-button>
      </a-radio-group>
      <a-button type="primary" @click="handleSetSource">{{ t('cluster.config_source') }}</a-button>
    </header>
    <a-table :loading="loading" :data-source="data" :columns="columns" :pagination="paginationProps" @change="onChange">
      <template #bodyCell="{ column }">
        <template v-if="column.key === 'stack'">
          <span> {{ `${stackSelected}-${currentStack?.stackVersion}` }} </span>
        </template>
      </template>
    </a-table>
    <set-source ref="setSourceRef" />
  </div>
</template>

<style lang="scss" scoped>
  .component-info {
    header {
      @include flexbox($justify: space-between);
      margin-bottom: $space-md;
    }
  }
</style>
