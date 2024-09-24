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
  import { CloseOutlined } from '@ant-design/icons-vue'
  import { toRefs } from 'vue'

  export type Option = {
    nextPage: string
    id?: string | number
    name?: string
    [key: string]: any
  }

  export interface SelectData {
    title?: string
    isDeletable?: boolean
    emptyOptionsText?: string
    options?: Option[]
  }

  interface SelectBoxProps {
    selectData?: SelectData[]
  }

  interface SelectBoxEmits {
    (event: 'select', option: Option): void
    (event: 'remove', option: Option): void
  }

  const props = defineProps<SelectBoxProps>()
  const emits = defineEmits<SelectBoxEmits>()
  const { selectData } = toRefs(props)

  const onRemove = (option: Option) => {
    emits('remove', option)
  }
  const onSelect = (option: Option) => {
    emits('select', option)
  }
</script>

<template>
  <ul class="select">
    <li v-for="(item, index) of selectData" :key="index" class="select-item">
      <ul>
        <label class="select-item-label">{{ item.title }}</label>
        <slot name="select-custom-content">
          <div v-if="!item.options || item.options.length == 0">
            <slot name="empty-text">
              <div class="select-item-empty">
                {{ item.emptyOptionsText || $t('common.no_options') }}
              </div>
            </slot>
          </div>
          <template v-else>
            <li
              v-for="(option, idx) of item.options"
              :key="idx"
              class="select-item-option"
              @click="onSelect(option)"
            >
              <span>
                {{ option.name }}
              </span>
              <CloseOutlined
                v-show="item.isDeletable"
                :key="option"
                class="select-item-del"
                @click.stop="onRemove(option)"
              />
            </li>
          </template>
        </slot>
      </ul>
    </li>
  </ul>
</template>

<style lang="scss" scoped>
  ul {
    list-style: none;
    margin: 0;
    padding: 0;
    box-sizing: border-box;

    li {
      padding: 4px 6px;
      margin-bottom: 6px;
      border-radius: 6px;
    }
  }

  .select {
    width: 100%;
    height: 100%;

    &-item {
      margin-bottom: 20px;

      &-label {
        display: block;
        margin-bottom: 10px;
        font-weight: 500;
      }

      &-empty {
        padding: 10px;
        text-align: center;
        color: #a9a9a9;
      }

      &-option {
        @include flexbox($justify: space-between, $align: center);
        border: 1px solid #c9c9c9;
        cursor: pointer;

        span {
          flex: 1;
          text-align: center;
        }

        .select-item-del {
          transition: opacity 0.08s ease-in-out;
          opacity: 0;
          flex: 0 0 14px;
        }

        &:hover {
          background-color: rgb(0, 0, 0, 0.06);

          .select-item-del {
            display: block;
            opacity: 1;
          }
        }
      }
    }
  }
</style>
