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
  import { h, ref } from 'vue'
  import { Modal } from 'ant-design-vue'
  import LlmItem, { LlmConfig, type ExtraItem } from './components/llm-item.vue'
  import addLlmItem from './components/add-llm-item.vue'
  import { ExclamationCircleOutlined } from '@ant-design/icons-vue'
  import { useI18n } from 'vue-i18n'

  const [modal, contextHolder] = Modal.useModal()
  const { t } = useI18n()
  const addLlmItemRef = ref<InstanceType<typeof addLlmItem> | null>(null)
  const llmConfigList = ref<LlmConfig[]>([
    {
      id: 1,
      name: 'Title-1',
      status: 0,
      apiKey: 'xxxxxx-1',
      platform: 'Platform-1',
      model: 'Model-1',
      remark: 'Remark-1'
    },
    {
      id: 2,
      name: 'Title-2',
      status: 1,
      apiKey: 'xxxxxx-2',
      platform: 'Platform-2',
      model: 'Model-2',
      remark: 'Remark-2'
    },
    {
      id: 3,
      name: 'Title-2',
      status: 2,
      apiKey: 'xxxxxx-3',
      platform: 'Platform-3',
      model: 'Model-3',
      remark: 'Remark-3'
    }
  ])

  const onCreate = () => {
    addLlmItemRef.value?.handleOpen({
      mode: 'add'
    })
  }

  const onExtraClick = (item: ExtraItem) => {
    if (item.action === 'edit') {
      addLlmItemRef.value?.handleOpen({
        mode: item.action,
        metaData: item.llmConfig
      })
    } else if (item.action === 'delete') {
      modal.confirm({
        title: t('llmConfig.delete_authorization'),
        icon: h(ExclamationCircleOutlined, { style: 'margin:16px' }),
        onOk() {
          console.log('OK')
        },
        onCancel() {
          console.log('Cancel')
        }
      })
    }
  }
</script>

<template>
  <div class="llm-config">
    <a-typography-title :level="5">
      {{ $t('llmConfig.llm_config') }}
    </a-typography-title>
    <div class="llm-config-content">
      <llm-item
        v-for="item in llmConfigList"
        :key="item.id"
        :llm-config="item"
        @on-extra-click="onExtraClick"
      />
      <llm-item :is-config="false" @on-create="onCreate" />
    </div>
    <add-llm-item ref="addLlmItemRef" />
    <contextHolder />
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
