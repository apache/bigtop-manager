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
  import type { FilterFormItem } from './types'
  import { ref, toRefs } from 'vue'

  interface FilterFormPops {
    filterItems: FilterFormItem[]
  }

  const props = defineProps<FilterFormPops>()
  const emits = defineEmits(['filter'])
  const { filterItems } = toRefs(props)

  const formatFilterFormItems = ref(
    filterItems.value.map((v) => {
      const formatData = {
        ...v,
        options: v.options?.map((o) => ({
          ...o,
          id: v.key,
          key: o.value
        }))
      }
      return formatData
    })
  )

  const filterParams = ref(
    filterItems.value.map((v) => ({
      [`${v.key}`]: ''
    }))
  )

  const checkSelected = ({ item, itemIdx }: any) => {
    return filterParams.value[itemIdx][item.key] === ''
  }

  const resetFilter = ({ item, itemIdx }: any) => {
    filterParams.value[itemIdx][item.key] = ''
    confirmFilterParams()
  }

  const onSelect = ({ item, key }: any, payload: any) => {
    filterParams.value[payload.itemIdx][item.id] = key
    confirmFilterParams()
  }

  const confirmFilterParams = () => {
    const filters = filterParams.value.reduce((pre, val) => {
      Object.assign(pre, val)
      return pre
    }, {} as any)
    emits('filter', filters)
  }
</script>

<template>
  <div class="filter-form">
    <template v-for="(item, itemIdx) in formatFilterFormItems" :key="item">
      <a-dropdown>
        <span @click.prevent>
          <div class="filter-form-label">
            <span :style="{ color: !checkSelected({ item, itemIdx }) ? 'var(--color-primary)' : 'initial' }">
              {{ item.label }}
            </span>
            <svg-icon name="bottom" style="padding: 6px 4px; margin-left: 8px" />
          </div>
        </span>
        <template #overlay>
          <template v-if="item.type === 'status'">
            <a-menu :selectable="true" :items="item.options" @select="onSelect($event, { item, itemIdx })"> </a-menu>
          </template>
          <template v-else-if="item.type === 'search'">
            <div class="search" @click.stop>
              <a-input v-model:value="filterParams[itemIdx][item.key]" :placeholder="`搜索${item.label}`" @click.stop />
              <a-space @click.stop>
                <a-button size="small" @click="resetFilter({ item, itemIdx })">重置</a-button>
                <a-button size="small" type="primary" @click="confirmFilterParams">搜索</a-button>
              </a-space>
            </div>
          </template>
        </template>
      </a-dropdown>
    </template>
  </div>
</template>

<style lang="scss" scoped>
  :deep(.ant-dropdown-menu-body) {
    padding-inline: 8px;
    padding-block: 8px;
  }
  :deep(.ant-dropdown-menu-head) {
    padding: 8px 16px;
    min-height: 32px;
  }

  .search {
    background-color: $color-bg-base;
    box-shadow: $box-shadow-drawer-up;
    padding: $space-sm;
    border-radius: $space-sm;
    display: flex;
    flex-direction: column;
    gap: $space-sm;
    align-items: flex-end;
  }

  .filter-form {
    width: 100%;
    display: flex;
    gap: $space-sm;
    &-label {
      display: inline-block;
      cursor: pointer;
      line-height: 20px;
      .ant-tag {
        line-height: 40px;
        border-radius: 8px;
      }
    }
    &-input-wrp {
      display: flex;
      flex-direction: column;
    }
    &-input {
      flex-shrink: 0;
      margin-block: 10px;
    }
    &-option-wrp {
      max-height: 200px;
      overflow: auto;
    }
  }
</style>
