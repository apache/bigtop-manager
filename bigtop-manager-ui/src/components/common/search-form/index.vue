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
  import type { SearchFormItem } from './types'
  import { SearchOutlined } from '@ant-design/icons-vue'
  import { ref } from 'vue'

  interface SearchFormPops {
    searchItems: SearchFormItem[]
  }

  const props = defineProps<SearchFormPops>()

  const filterParams = ref(
    props.searchItems.map((v) => {
      return {
        [`${v.key}`]: ''
      }
    })
  )

  const checkSelected = ({ item, itemIdx }: any) => {
    return filterParams.value[itemIdx][item.key] === ''
  }

  const resetFilter = ({ item, itemIdx }: any) => {
    filterParams.value[itemIdx][item.key] = ''
    confirmFilterParams()
  }

  const onSelect = ({ item, itemIdx, option }: any) => {
    filterParams.value[itemIdx][item.key] = option.value
    confirmFilterParams()
  }

  const confirmFilterParams = () => {
    const filters = filterParams.value.reduce((pre, val) => {
      Object.assign(pre, val)
      return pre
    }, {} as any)
    console.log('filters :>> ', filters)
  }
</script>

<template>
  <div class="search-form">
    <template v-for="(item, itemIdx) in searchItems" :key="item">
      <a-dropdown>
        <span @click.prevent>
          <div class="search-form-label">
            <span :style="{ color: !checkSelected({ item, itemIdx }) ? 'var(--color-primary)' : 'initial' }">
              {{ item.label }}
            </span>
            <svg-icon name="bottom" style="padding: 6px 4px; margin-left: 8px" />
          </div>
        </span>
        <template #overlay>
          <a-menu>
            <a-menu-item v-for="option in item.options" :key="option.value">
              <a href="javascript:;">{{ option.label }}</a>
            </a-menu-item>
          </a-menu>
          <!-- <a-card class="search-card" @click.stop>
            <template #extra><a href="#" @click.stop="resetFilter({ item, itemIdx })">重置</a></template>
            <template #title>
              <span style="font-size: 14px">{{ item.label }}</span>
            </template>

            <template v-if="item.type == 'search'">
              <div class="search-form-input-wrp" @click.stop>
                <div class="search-form-input">
                  <a-input v-model:value="filterParams[itemIdx][item.key]" :placeholder="`搜索${item.label}`">
                    <template #prefix><SearchOutlined /></template>
                  </a-input>
                </div>
              </div>
              <footer>
                <a-button size="small" type="primary" @click="confirmFilterParams">应用</a-button>
              </footer>
            </template>

            <template v-else-if="item.type == 'status'">
              <div class="search-form-option-wrp">
                <div
                  v-for="(option, idx) in item.options"
                  :key="idx"
                  class="search-form-option"
                  :class="{ 'option-selected': filterParams[itemIdx][item.key] == option.value }"
                  @click="onSelect({ item, itemIdx, option })"
                >
                  <a-space :size="10">
                    <span>
                      {{ option.label }}
                    </span>
                  </a-space>
                </div>
              </div>
            </template>
          </a-card> -->
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

  .option-selected {
    font-weight: 600;
    background-color: $blue-1;
    &:hover {
      background-color: $blue-1;
    }
  }

  .search-card {
    min-width: 280px;
    footer {
      text-align: end;
    }
  }
  .search-form {
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

    &-option {
      overflow: auto;
      padding: 6px;
      border-radius: 8px;
      cursor: pointer;
      display: flex;
      align-items: center;
      &:hover {
        background-color: $color-bg-text-hover;
      }
      &-icon {
        flex-shrink: 0;
        border-radius: 0;
        :deep(.ant-avatar) {
          img {
            object-fit: contain !important;
          }
        }
      }
    }
  }
</style>
