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
  import {
    AuthPlatformStatus,
    LlmLogo,
    type AuthorizedPlatform,
    type AuthorizedPlatformDesc,
    type AuthPlatformStatusType
  } from '@/api/llm-config/types'
  import type { MenuItemType } from 'ant-design-vue/es/menu/src/interface'
  import { usePngImage } from '@/utils/tools'

  enum Actions {
    DISABLE = '1',
    ENABLE = '2',
    EDIT = '3',
    DELETE = '4'
  }

  export type ActionKeys = keyof typeof Actions
  export type ExtraItem = { llmConfig: AuthorizedPlatform; action: ActionKeys }

  interface LlmDescriptionItem {
    label: string
    code: keyof AuthorizedPlatformDesc
  }

  interface LlmStatusItem {
    text: string
    type: 'success' | 'processing' | 'error'
    actionKeys: ActionKeys[]
    status: AuthPlatformStatusType
  }

  interface ExtraActionItem extends MenuItemType {
    key: ActionKeys
    danger?: boolean
  }

  interface Props {
    llmConfig?: AuthorizedPlatform | Record<string, string>
    loading?: boolean
    isConfig?: boolean
  }

  interface Emits {
    (event: 'createLlmConfig'): void
    (event: 'update:loading', value: boolean): void
    (event: 'extraActionClick', value: ExtraItem): void
  }

  const props = withDefaults(defineProps<Props>(), {
    loading: false,
    isConfig: true,
    llmConfig: () => ({})
  })

  const { t } = useI18n()
  const emits = defineEmits<Emits>()
  const { llmConfig, isConfig, loading } = toRefs(props)

  const llmDescriptions = shallowRef<LlmDescriptionItem[]>([
    {
      label: 'llmConfig.platform_name',
      code: 'platformName'
    },
    {
      label: 'llmConfig.model',
      code: 'model'
    },
    {
      label: 'llmConfig.desc',
      code: 'desc'
    }
  ])

  const extraActions = shallowRef<ExtraActionItem[]>([
    {
      key: 'DISABLE',
      label: 'common.disable',
      disabled: false
    },
    {
      key: 'ENABLE',
      label: 'common.enable',
      disabled: false
    },
    {
      key: 'EDIT',
      label: 'common.edit',
      disabled: false
    },
    {
      key: 'DELETE',
      disabled: false,
      danger: true,
      label: 'common.delete'
    }
  ])

  const llmStatusConfig = shallowRef<LlmStatusItem[]>([
    {
      status: AuthPlatformStatus.ACTIVE,
      text: 'llmConfig.active',
      type: 'success',
      actionKeys: ['DISABLE', 'EDIT', 'DELETE']
    },
    {
      status: AuthPlatformStatus.AVAILABLE,
      text: 'llmConfig.available',
      type: 'processing',
      actionKeys: ['ENABLE', 'EDIT', 'DELETE']
    },
    {
      status: AuthPlatformStatus.UNAVAILABLE,
      text: 'llmConfig.unavailable',
      type: 'error',
      actionKeys: ['ENABLE', 'EDIT', 'DELETE']
    }
  ])

  const currStatus = computed(() => llmConfig.value?.status)
  const actionKeysSet = computed(() => new Set(llmStatus.value?.actionKeys))
  const ellipsis = computed(() => (llmConfig.value ? true : false))
  const llmStatus = computed(() => llmStatusConfig.value.find(({ status }) => status === currStatus.value))
  const llmActions = computed(() => {
    return extraActions.value
      .filter((item) => actionKeysSet.value.has(item.key))
      .map((item) => ({
        ...item,
        disabled: getActionDisabled(item.key)
      }))
  })

  const getActionDisabled = (key: ActionKeys): boolean => isDisable(key) || isEnable(key)

  const isDisable = (key: ActionKeys): boolean =>
    currStatus.value === AuthPlatformStatus.UNAVAILABLE && key === 'ENABLE'

  const isEnable = (key: ActionKeys): boolean => currStatus.value === AuthPlatformStatus.ACTIVE && key === 'DELETE'

  const handleCreateLlmConfig = () => {
    emits('createLlmConfig')
  }

  const handleClickAction = ({ key }) => {
    emits('extraActionClick', {
      llmConfig: llmConfig.value as AuthorizedPlatform,
      action: key
    })
  }
</script>

<template>
  <div class="llm-card">
    <template v-if="isConfig">
      <a-skeleton active :loading="loading">
        <div class="llm-card-header">
          <div class="llm-card-header-left">
            <a-image :width="24" :height="24" :preview="false" :src="usePngImage(LlmLogo[llmConfig.platformId])" />
            <a-typography-text
              class="llm-card-header-left-text"
              :ellipsis="llmConfig?.name ? { tooltip: llmConfig?.name } : false"
              :content="`${llmConfig?.name}`"
            />
            <a-tag :color="llmStatus?.type">
              {{ t(llmStatus?.text || '--') }}
            </a-tag>
          </div>
          <a-dropdown :trigger="['click']">
            <a-button type="text" size="small" shape="circle">
              <template #icon>
                <svg-icon name="more" />
              </template>
            </a-button>
            <template #overlay>
              <a-menu @click="handleClickAction">
                <a-menu-item
                  v-for="{ key, label, danger, disabled } in llmActions"
                  :key="key"
                  :danger="danger"
                  :disabled="disabled"
                  :title="t(label)"
                >
                  {{ t(label) }}
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
        <div class="llm-card-body">
          <a-typography-paragraph v-for="{ label, code } in llmDescriptions" :key="code" class="llm-card-desc">
            <a-typography-text>{{ t(label) }}</a-typography-text>
            <a-typography-paragraph
              type="secondary"
              class="llm-card-desc-text"
              :ellipsis="ellipsis ? { tooltip: llmConfig[code] } : false"
              :content="`${llmConfig ? llmConfig[code] : '--'}`"
            />
          </a-typography-paragraph>
        </div>
      </a-skeleton>
    </template>
    <template v-else>
      <div class="llm-card-action" @click="handleCreateLlmConfig">
        <svg-icon name="plus" />
        <a-typography-text type="secondary" :content="t('llmConfig.add_authorization')" />
      </div>
    </template>
  </div>
</template>

<style lang="scss" scoped>
  .llm-card {
    position: relative;
    flex-shrink: 0;
    width: 260px;
    height: 166px;
    border: 1px solid $color-border;
    border-radius: $border-radius-sm;
    padding: $space-md;
    font-size: 12px;
    overflow: hidden;
    :deep(.ant-typography) {
      font-size: inherit;
      margin-bottom: 0px !important;
    }
  }

  .llm-card-header {
    @include flexbox($justify: space-between);
    margin-bottom: $space-md;
    &-left {
      display: flex;
      align-items: center;
      gap: $space-sm;
      &-text {
        max-width: 100px;
        flex: 1;
      }
    }

    .svg-icon {
      margin: 0;
      line-height: 24px;
    }
  }

  .llm-card-body {
    display: grid;
    gap: $space-sm;
  }

  .llm-card-desc {
    width: 100%;
    border-bottom: 1px solid $color-split;
    padding-bottom: 6px;

    @include flexbox($justify: space-between, $gap: $space-lg);
    &-text {
      width: 0;
      flex: 1;
      text-align: end;
    }
  }

  .llm-card-action {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    .svg-icon {
      width: 36px;
      height: 36px;
    }
  }
</style>
