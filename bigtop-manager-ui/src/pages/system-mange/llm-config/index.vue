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
  import { onMounted, ref } from 'vue'
  import LlmItem, { type ExtraItem } from './components/llm-item.vue'
  import addLlmItem from './components/add-llm-item.vue'
  import { useLlmConfig } from '@/composables/llm-config/use-llm-config'

  const addLlmItemRef = ref<InstanceType<typeof addLlmItem> | null>(null)
  const {
    loading,
    authorizedPlatforms,
    getAuthorizedPlatforms,
    deleteAuthPlatform
  } = useLlmConfig()

  const onCreate = () => {
    addLlmItemRef.value?.handleOpen({
      mode: 'ADD'
    })
  }

  const onExtraClick = (item: ExtraItem) => {
    if (item.action === 'EDIT') {
      addLlmItemRef.value?.handleOpen({
        mode: item.action,
        metaData: item.llmConfig
      })
    } else if (item.action === 'DELETE') {
      deleteAuthPlatform()
    }
  }

  onMounted(() => {
    getAuthorizedPlatforms()
  })
</script>

<template>
  <a-spin :spinning="loading">
    <div class="llm-config">
      <a-typography-title :level="5">
        {{ $t('llmConfig.llm_config') }}
      </a-typography-title>
      <div class="llm-config-content">
        <llm-item
          v-for="item in authorizedPlatforms"
          :key="item.id"
          :llm-config="item"
          @on-extra-click="onExtraClick"
        />
        <llm-item :is-config="false" @on-create="onCreate" />
      </div>
      <add-llm-item
        ref="addLlmItemRef"
        @on-test="getAuthorizedPlatforms"
        @on-ok="getAuthorizedPlatforms"
      />
    </div>
  </a-spin>
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
