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
import { reactive, computed, onMounted } from 'vue';
import useBaseTable from '@/composables/use-base-table';
import type { TableColumnType } from 'ant-design-vue';
import { useI18n } from 'vue-i18n'
import { useStackStore } from '@/store/stack'
import { storeToRefs } from 'pinia'
  
const { t } = useI18n()
const stackStore = useStackStore()
const { stacks } = storeToRefs(stackStore)
  
const store = reactive({
  loading: false,
  stackSelected: 'Bigtop',
  stackGroup: ['Bigtop', 'Infra', 'Extra'],
})
const currentStack = computed(() => stacks.value.find((stack) => stack.stackName.toUpperCase() === store.stackSelected.toUpperCase() ))
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
      dataIndex: 'stackName',
      width: '20%',
      ellipsis: true
    },
    {
      title: t('common.version'),
      dataIndex: 'stackVersion',
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
    },
])
  
const { loading, paginationProps, onChange } = useBaseTable({
  columns: columns.value,
  rows: stacks.value
})

const handleSetSource = () => {
  // setSourceRef.value?.handleOpen()
}

onMounted(() =>{
  stackStore.loadStacks();
})
</script>

<template>
  <div class="cluster-components">
    <div>
      <div class="menu-title">{{ $t('menu.component') }}</div>
    </div>
    <div class="cluster-components-header">
      <a-radio-group v-model:value="store.stackSelected" button-style="solid">
        <a-radio-button v-for="(stack, idx) in store.stackGroup" :key="idx" :value="stack">{{ stack }}</a-radio-button>
      </a-radio-group>
      <a-button type="primary" @click="handleSetSource">{{ $t('cluster.config_source') }}</a-button>
    </div>
    <div>
      <a-table 
        :loading="loading" 
        :data-source="stacks.filter((ele) => { return ele.stackName.toUpperCase() === store.stackSelected.toUpperCase() })" 
        :columns="columns" 
        :pagination="paginationProps" 
        @change="onChange"
      >
        <template #bodyCell="{ column }">
          <template v-if="column.key === 'stack'">
            <span> {{ `${store.stackSelected}-${currentStack?.stackVersion}` }} </span>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.cluster-components {
  padding: 24px 16px;
  background-color: #fff;
  .menu-title {
    font-weight: bolder;
  }
  .cluster-components-header {
    display: flex;
    justify-content: space-between;
    margin: $space-md 0;
  }
}
</style>
