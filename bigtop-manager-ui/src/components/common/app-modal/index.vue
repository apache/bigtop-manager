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
  import { ModalProps } from 'ant-design-vue/es/modal/Modal'
  import { toRefs } from 'vue'

  type Props = ModalProps

  interface Emits {
    (event: 'update:open', value: boolean): void
    (event: 'update:loading', value: boolean): void
    (event: 'onOk'): void
    (event: 'onCancel'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    title: '',
    open: false,
    loading: false,
    destroyOnClose: true
  })

  const { open, title, loading } = toRefs(props)

  const emits = defineEmits<Emits>()

  const handleOk = () => {
    emits('onOk')
    emits('update:open', false)
  }

  const handleCancel = () => {
    emits('onCancel')
    emits('update:open', false)
  }
</script>

<template>
  <div class="update-llm-config">
    <a-modal
      v-model:open="open"
      :width="props.width"
      :title="$t(title)"
      :mask-closable="props.maskClosable"
      :destroy-on-close="props.destroyOnClose"
      :footer="props.footer"
      @ok="handleOk"
      @cancel="handleCancel"
    >
      <div v-if="props.footer == null" class="modal-body-wrp">
        <main>
          <slot name="body" />
        </main>
        <footer>
          <slot name="footer">
            <a-space size="middle">
              <slot name="footer-left" />
            </a-space>
            <a-space size="middle">
              <slot name="footer-right">
                <a-button @click="handleCancel">
                  {{ $t('common.cancel') }}
                </a-button>
                <a-button type="primary" :loading="loading" @click="handleOk">
                  {{ $t('common.confirm') }}
                </a-button>
              </slot>
            </a-space>
          </slot>
        </footer>
      </div>
    </a-modal>
  </div>
</template>

<style lang="scss" scoped>
  .modal-body-wrp {
    display: grid;
    gap: 16px;
    main {
      overflow: auto;
    }
    footer {
      width: 100%;
      display: flex;
      justify-content: space-between;
    }
  }
</style>
