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
  import { ref } from 'vue'
  import LlmCard, { type ExtraItem } from './components/llm-card.vue'
  import UpdateLlmConfig from './components/update-llm-config.vue'

  const open = ref(false)
  const title = ref('')

  const onCreate = () => {
    title.value = 'llmConfig.create_authorization'
    open.value = true
  }

  const onExtraClick = (item: ExtraItem) => {
    item.action == '3' && (title.value = 'llmConfig.edit_authorization')
    open.value = true
  }
</script>

<template>
  <div class="llm-config">
    <a-typography-title :level="5">
      {{ $t('llmConfig.llm_config') }}
    </a-typography-title>
    <div class="llm-config-content">
      <llm-card @on-extra-click="onExtraClick" />
      <llm-card :is-config="false" @on-create="onCreate" />
    </div>
    <update-llm-config v-model:open="open" :title="title" />
  </div>
</template>

<style lang="scss" scoped>
  .llm-config {
    padding: $space-md;
    background-color: $color-bg-base;
    @include flexbox($direction: column, $gap: $space-sm);
    &-content {
      @include flexbox($wrap: wrap, $gap: $space-md);
    }
  }
</style>
