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
  import { MenuItemType } from 'ant-design-vue/es/menu/src/interface'
  import { computed, shallowRef, toRaw, toRefs } from 'vue'

  enum Actions {
    disable = '1',
    enable = '2',
    edit = '3',
    delete = '4'
  }

  type Status = 0 | 1 | 2
  type AcionsKeys = keyof typeof Actions
  export type ExtraItem = { llmConfig: LlmConfig; action: AcionsKeys }

  interface BaseConfig {
    platform: string
    model: string
    remark: string
  }

  interface LlmDescriptionItem {
    label: string
    code: keyof BaseConfig
  }

  interface LlmConfig extends BaseConfig {
    id: number | string
    title: string
    status: Status
  }

  interface LlmStatusItem {
    status: Status
    text: string
    type: string
    actionKeys: Actions[]
  }

  interface ExtraActionItem extends MenuItemType {
    key: AcionsKeys
    danger?: boolean
  }

  interface Props {
    llmConfig?: LlmConfig
    loading?: boolean
    isConfig?: boolean
  }

  interface Emits {
    (event: 'onCreate'): void
    (event: 'update:loading', value: boolean): void
    (event: 'onExtraClick', value: ExtraItem): void
  }

  const props = withDefaults(defineProps<Props>(), {
    loading: false,
    isConfig: true,
    llmConfig: () => {
      return {
        id: 0,
        title: 'defaultTitle',
        status: 0,
        platform: 'defaultPlatform',
        model: 'defaultModel',
        remark: 'defaultMark'
      }
    }
  })
  const emits = defineEmits<Emits>()
  const { llmConfig, isConfig, loading } = toRefs(props)

  const llmDescriptions = shallowRef<LlmDescriptionItem[]>([
    {
      label: 'llmConfig.platform',
      code: 'platform'
    },
    {
      label: 'llmConfig.model',
      code: 'model'
    },
    {
      label: 'llmConfig.remark',
      code: 'remark'
    }
  ])

  const llmStatus = shallowRef<LlmStatusItem[]>([
    {
      status: 0,
      text: 'llmConfig.unavailable',
      type: 'error',
      actionKeys: [Actions.enable, Actions.edit, Actions.delete]
    },
    {
      status: 1,
      text: 'llmConfig.in_use',
      type: 'success',
      actionKeys: [Actions.disable, Actions.edit, Actions.delete]
    },
    {
      status: 2,
      text: 'llmConfig.available',
      type: 'processing',
      actionKeys: [Actions.enable, Actions.edit, Actions.delete]
    }
  ])

  const menuItems = shallowRef<ExtraActionItem[]>([
    {
      key: 'disable',
      label: 'common.disable',
      disabled: false
    },
    {
      key: 'enable',
      label: 'common.enable',
      disabled: false
    },
    {
      key: 'edit',
      label: 'common.edit',
      disabled: false
    },
    {
      key: 'delete',
      disabled: false,
      danger: true,
      label: 'common.delete'
    }
  ])

  const currStatus = computed(() => llmConfig.value?.status)
  const getLlmStatus = computed(
    () => llmStatus.value.filter(({ status }) => status == currStatus.value)[0]
  )
  const getLlmActions = computed(() => {
    return menuItems.value.reduce((acc, item) => {
      if (getLlmStatus.value.actionKeys.includes(Actions[item.key])) {
        const updatedItem = { ...item }
        if (
          (currStatus.value === 1 && item.key === 'delete') ||
          (currStatus.value === 0 && item.key === 'enable')
        ) {
          updatedItem.disabled = true
        }
        acc.push(updatedItem)
      }
      return acc
    }, [] as ExtraActionItem[])
  })

  const handleCreateLlmConfig = () => {
    emits('onCreate')
  }

  const handleClickAction = ({ key }: { key: AcionsKeys }) => {
    emits('onExtraClick', {
      llmConfig: toRaw(llmConfig.value),
      action: key
    })
  }
</script>

<template>
  <div class="llm-card">
    <template v-if="isConfig">
      <a-skeleton active :loading="loading">
        <div class="llm-card-header">
          <div class="llm-card-header-title">
            <a-image
              :width="24"
              :height="24"
              src="https://www.antdv.com/#error"
              fallback="data:image/png;base64,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"
            />
            <a-typography-text
              :style="{ width: '72px' }"
              :ellipsis="{ tooltip: llmConfig?.title }"
              :content="llmConfig?.title"
            />
            <a-tag :color="getLlmStatus?.type">
              {{ $t(getLlmStatus?.text) }}
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
                  v-for="{ key, label, danger, disabled } in getLlmActions"
                  :key="key"
                  :danger="danger"
                  :disabled="disabled"
                  :title="label"
                >
                  {{ $t(label) }}
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
        <div class="llm-card-desc">
          <div
            v-for="{ label, code } in llmDescriptions"
            :key="code"
            class="llm-card-desc-item"
          >
            <div>{{ $t(label) }}</div>
            <a-typography-text
              type="secondary"
              :ellipsis="{ tooltip: llmConfig ? llmConfig[code] : '--' }"
              :content="llmConfig ? llmConfig[code] : '--'"
            />
          </div>
        </div>
      </a-skeleton>
    </template>
    <template v-else>
      <div class="llm-card-action" @click="handleCreateLlmConfig">
        <svg-icon name="plus_dark" />
        <a-typography-text
          type="secondary"
          :content="$t('llmConfig.create_authorization')"
        />
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
    :deep(.ant-typography) {
      font-size: inherit;
    }
  }

  .llm-card-header {
    @include flexbox($justify: space-between);
    margin-bottom: $space-md;
    &-title {
      display: flex;
      align-items: center;
      gap: $space-sm;
    }
    .svg-icon {
      margin: 0;
      line-height: 24px;
    }
  }

  .llm-card-desc {
    @include flexbox($direction: column, $gap: $space-sm);
    &-item {
      @include flexbox($justify: space-between, $gap: 0 $space-lg);
      flex: 0 0 26px;
      border-bottom: 1px solid $color-split;
      & div:first-child {
        flex-shrink: 0;
      }
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
