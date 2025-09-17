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
  import type { GroupItem, Props } from './types'

  const props = withDefaults(defineProps<Props>(), {
    i18n: '',
    textCompact: false,
    groupShape: 'circle',
    groupType: 'text',
    space: 8,
    groups: () => {
      return [] as any[]
    }
  })

  const { t } = useI18n()
  const { groups, groupShape, groupType, space, payload } = toRefs(props)

  const checkTitle = (item: GroupItem) => {
    if (props.i18n) {
      if (item.tip) {
        return t(`${props.i18n}.${item.tip}`)
      } else if (item.text) {
        return t(`${props.i18n}.${item.text}`)
      }
    } else {
      return item.tip ? item.tip : item.text ? item.text : undefined
    }
  }
</script>

<template>
  <a-space :size="space" :wrap="true" :class="{ 'text-compact': $props.textCompact, 'btn-auto': $props.auto }">
    <template v-for="(item, _index) in groups" :key="_index">
      <template v-if="!(typeof item.hidden === 'function' ? item.hidden(item, payload) : (item.hidden ?? false))">
        <a-dropdown v-if="item.dropdownMenu">
          <a-button
            :disabled="item.disabled"
            :shape="item.shape || groupShape || 'default'"
            :type="item.type || groupType || 'default'"
            :title="checkTitle(item)"
            @click="item.clickEvent ? item.clickEvent(item) : () => {}"
          >
            <template #icon>
              <slot name="icon" :item="item" />
            </template>
            <span v-if="item.text">
              {{ $props.i18n && item.text ? t(`${$props.i18n}.${item.text}`) : item.text }}
            </span>
          </a-button>
          <template #overlay>
            <a-menu
              v-if="!item.disabled && item.dropdownMenuClickEvent"
              @click="item.dropdownMenuClickEvent($event, payload)"
            >
              <template v-for="actionItem in item.dropdownMenu" :key="actionItem.action">
                <a-menu-divider v-if="actionItem.divider" />
                <a-menu-item v-bind="{ key: actionItem.action, ...actionItem }">
                  {{ actionItem.text }}
                </a-menu-item>
              </template>
            </a-menu>
          </template>
        </a-dropdown>
        <a-button
          v-else
          :danger="item.danger"
          :disabled="item.disabled"
          :shape="item.shape || groupShape || 'default'"
          :type="item.type || groupType || 'default'"
          :title="checkTitle(item)"
          @click="item.clickEvent ? item.clickEvent(item, payload) : () => {}"
        >
          <template #icon>
            <slot name="icon" :item="item" />
          </template>
          <span v-if="item.text">
            {{ $props.i18n && item.text ? t(`${$props.i18n}.${item.text}`) : item.text }}
          </span>
        </a-button>
      </template>
    </template>
  </a-space>
</template>

<style lang="scss" scoped>
  .text-compact {
    button {
      padding: 0;
    }
  }
  .btn-auto {
    width: 100%;
    @include flexbox($align: center, $justify: space-between);
    margin-bottom: 0 !important;
  }
</style>
