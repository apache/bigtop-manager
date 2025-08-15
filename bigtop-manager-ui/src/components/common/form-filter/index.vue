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
  import { MenuProps } from 'ant-design-vue'
  import { isEqual, cloneDeep } from 'lodash-es'
  import type { FilterFormItem } from './types'

  interface FilterFormPops {
    filterItems: FilterFormItem[]
  }

  const { t } = useI18n()
  const props = defineProps<FilterFormPops>()
  const emits = defineEmits(['filter'])
  const { filterItems } = toRefs(props)

  const tempFilterParams = shallowRef({})
  const filterParams = ref(
    filterItems.value.reduce(
      (pre, value) => {
        return Object.assign(pre, { [`${value.key}`]: undefined })
      },
      {} as Record<string, any>
    )
  )

  const formatFilterFormItems = computed(() =>
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

  const openChange = (open: boolean) => {
    if (open) {
      tempFilterParams.value = cloneDeep(toRaw(filterParams.value))
    } else {
      !isEqual(tempFilterParams.value, filterParams.value) && confirmFilterParams()
    }
  }

  const confirmFilterParams = () => {
    emits('filter', filterParams.value)
  }

  const onSelect: MenuProps['onSelect'] = ({ item, key }) => {
    filterParams.value[`${item.id}`] = key
  }

  const resetFilter = (item: any) => {
    filterParams.value[item.key] = undefined
  }
</script>

<template>
  <div class="filter-form">
    <template v-for="item in formatFilterFormItems" :key="item">
      <a-dropdown :trigger="['click']" @open-change="openChange">
        <span @click.prevent>
          <div class="filter-form-label">
            <span :style="{ color: filterParams[`${item.key}`] != undefined ? 'var(--color-primary)' : 'initial' }">
              {{ item.label }}
            </span>
            <svg-icon
              name="bottom"
              :highlight="filterParams[`${item.key}`] !== undefined"
              style="padding: 6px 4px; margin-left: 8px"
            />
          </div>
        </span>
        <template #overlay>
          <template v-if="item.type === 'status'">
            <div>
              <a-menu :selected-keys="[filterParams[item.key]]" :selectable="true" @select="onSelect">
                <a-menu-item
                  v-for="menuItem in item.options"
                  :id="item.key"
                  :key="menuItem.key as any"
                  :title="menuItem.label"
                  @click.stop
                >
                  <a-radio :checked="filterParams[item.key] === menuItem.key">
                    <span>{{ menuItem.label }}</span>
                  </a-radio>
                </a-menu-item>
                <a-space class="status-option">
                  <a-button
                    :disabled="filterParams[`${item.key}`] === undefined"
                    size="small"
                    type="link"
                    @click.stop="resetFilter(item)"
                  >
                    {{ t('common.reset') }}
                  </a-button>
                  <a-button size="small" type="primary" @click="confirmFilterParams">
                    {{ t('common.ok') }}
                  </a-button>
                </a-space>
              </a-menu>
            </div>
          </template>
          <template v-else-if="item.type === 'search'">
            <div class="search" @click.stop>
              <a-input
                v-model:value="filterParams[item.key]"
                :placeholder="`${t('common.enter_error')}${item.label.toLowerCase()}`"
                @click.stop
              />
              <a-space class="search-option">
                <a-button size="small" @click.stop="resetFilter(item)">{{ t('common.reset') }}</a-button>
                <a-button size="small" type="primary" @click="confirmFilterParams">{{ t('common.search') }}</a-button>
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

  .status-option {
    width: 100%;
    display: grid;
    grid-template-columns: 1fr 1fr;
    border-top: 1px solid #f0f0f0;
    padding: 8px 4px 4px 4px;
    button {
      width: 100%;
    }
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
    &-option {
      width: 100%;
      display: grid;
      grid-template-columns: 1fr 1fr;
      button {
        width: 100%;
      }
    }
  }

  .filter-form {
    width: 100%;
    display: flex;
    gap: $space-sm;
    margin-bottom: $space-md;
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
