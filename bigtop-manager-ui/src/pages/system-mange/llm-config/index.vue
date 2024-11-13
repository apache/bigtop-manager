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
  import addLlmItem from './components/add-llm-item.vue'
  import { useLlmConfigStore } from '@/store/llm-config/index'
  import { Modal } from 'ant-design-vue'
  import { useI18n } from 'vue-i18n'
  import LlmItem, {
    type AcionsKeys,
    type ExtraItem
  } from './components/llm-item.vue'
  import type { AuthorizedPlatform } from '@/api/llm-config/types'
  import { storeToRefs } from 'pinia'

  const addLlmItemRef = ref<InstanceType<typeof addLlmItem> | null>(null)
  const { t } = useI18n()
  const llmConfigStore = useLlmConfigStore()
  const { loading, authorizedPlatforms } = storeToRefs(llmConfigStore)

  const actionsMap: Record<AcionsKeys, (config: AuthorizedPlatform) => void> = {
    EDIT: (config) => {
      addLlmItemRef.value?.handleOpen(config)
    },
    DELETE: ({ id }) => {
      handleDeleteLlmConfig(id)
    },
    ENABLE: async ({ id }) => {
      const success = await llmConfigStore.activateAuthorizedPlatform(id)
      success && llmConfigStore.getAuthorizedPlatforms()
    },
    DISABLE: async ({ id }) => {
      const success = await llmConfigStore.deactivateAuthorizedPlatform(id)
      success && llmConfigStore.getAuthorizedPlatforms()
    }
  }

  const onCreate = () => {
    addLlmItemRef.value?.handleOpen()
  }

  const onExtraClick = (item: ExtraItem) => {
    const { llmConfig, action } = item
    const actionHandler = actionsMap[action]
    if (actionHandler) {
      actionHandler(llmConfig)
    } else {
      console.warn(`Unknown action: ${action}`)
    }
  }

  const handleDeleteLlmConfig = (authId: number) => {
    Modal.confirm({
      title: t('llmConfig.delete_authorization'),
      async onOk() {
        const success = await llmConfigStore.deleteAuthPlatform(authId)
        success && llmConfigStore.getAuthorizedPlatforms()
      }
    })
  }

  onMounted(() => {
    llmConfigStore.getAuthorizedPlatforms()
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
        @on-test="llmConfigStore.getAuthorizedPlatforms"
        @on-ok="llmConfigStore.getAuthorizedPlatforms"
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
