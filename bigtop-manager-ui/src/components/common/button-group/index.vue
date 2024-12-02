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
  import { toRefs } from 'vue'
  import type { Props } from './types'

  const props = withDefaults(defineProps<Props>(), {
    i18n: '',
    groupShape: 'circle',
    groupType: 'text'
  })

  const { groups, groupShape, groupType } = toRefs(props)
</script>

<template>
  <a-space>
    <a-button
      v-for="(item, idx) in groups"
      :key="idx"
      :disabled="item.disabled"
      :shape="item.shape || groupShape || 'default'"
      :type="item.type || groupType || 'default'"
      :title="(item.tip && $t(`${$props.i18n}.${item.tip}`)) || (item.text && $t(`${$props.i18n}.${item.text}`))"
      @click="item.clickEvent ? item.clickEvent(item) : () => {}"
    >
      <template #icon>
        <slot name="icon" :item="item">
          <template v-if="typeof item.icon !== 'string'">
            <component :is="item.icon"></component>
          </template>
        </slot>
      </template>
      <span v-if="item.text">
        {{ item.text && $t(`${$props.i18n}.${item.text}`) }}
      </span>
    </a-button>
  </a-space>
</template>

<style lang="scss" scoped></style>
