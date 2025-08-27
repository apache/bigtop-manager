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
  import { usePngImage } from '@/utils/tools'

  import type { GroupItem } from '@/components/common/button-group/types'

  interface Props {
    showAvatar?: boolean
    showStatus?: boolean
    avatar?: string
    title?: string
    desc?: string
    status?: string
    actionGroups?: GroupItem[]
  }

  interface Emits {
    (event: 'onClick', item: GroupItem): void
  }

  const props = withDefaults(defineProps<Props>(), {
    showAvatar: true,
    showStatus: true,
    avatar: '',
    title: '',
    desc: '',
    status: '',
    actionGroups: () => {
      return []
    }
  })

  const emits = defineEmits<Emits>()
  const avatarSrc = computed(() => props.avatar && usePngImage(props.avatar))

  const onActions = (item: GroupItem) => {
    emits('onClick', item)
  }
</script>

<template>
  <div class="header-card">
    <slot>
      <div class="header-card-info">
        <slot name="avatar">
          <a-avatar v-if="showAvatar" :src="avatarSrc" shape="square" :size="54" />
        </slot>
        <div class="card-info-title">
          <div class="card-info-status">
            <a-typography-title :level="5">{{ $props.title }}</a-typography-title>
            <svg-icon v-if="showStatus" :name="status" />
          </div>
          <a-typography-text type="secondary">{{ $props.desc }}</a-typography-text>
        </div>
      </div>
      <div class="header-card-action">
        <slot name="actions">
          <button-group :groups="$props.actionGroups ?? []" @on-click="onActions" />
        </slot>
      </div>
    </slot>
  </div>
</template>

<style lang="scss" scoped>
  .header-card {
    width: 100%;
    min-width: 389px;
    min-height: 102px;
    @include flexbox($justify: space-between, $align: center);
    background-color: $color-white;
    padding: $space-md;
    margin-bottom: $space-md;
    &-info {
      @include flexbox($align: center);
      gap: $space-md;
      :deep(.ant-avatar) {
        border-radius: 4px;
      }
    }
  }
  .card-info {
    &-title {
      @include flexbox($direction: column, $align: space-between, $gap: $space-sm);
    }
    &-status {
      @include flexbox($align: center, $gap: 2px);
      .ant-typography {
        margin: 0;
      }
    }
  }

  :deep(.ant-avatar) {
    flex-shrink: 0;
    border-radius: 4px;

    img {
      object-fit: contain !important;
    }
  }
</style>
