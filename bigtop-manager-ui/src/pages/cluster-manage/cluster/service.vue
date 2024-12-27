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
  import { onMounted, ref, shallowRef } from 'vue'
  import { getServices, StatusColors, StatusTexts, type ServiceItem } from './components/mock'
  import { usePngImage } from '@/utils/tools'
  import SearchForm from '@/components/common/search-form/index.vue'
  import type { GroupItem } from '@/components/common/button-group/types'

  const data = ref<ServiceItem[]>([])
  const actionGroups = shallowRef<GroupItem[]>([
    {
      action: 'start',
      icon: 'start',
      clickEvent: (item) => {
        console.log('item :>> ', item?.action)
      }
    },
    {
      action: 'stop',
      icon: 'stop',
      clickEvent: (item) => {
        console.log('item :>> ', item?.action)
      }
    },
    {
      action: 'restart',
      icon: 'restart',
      clickEvent: (item) => {
        console.log('item :>> ', item?.action)
      }
    },
    {
      action: 'more',
      icon: 'more_line',
      clickEvent: (item) => {
        console.log('item :>> ', item?.action)
      }
    }
  ])
  onMounted(() => {
    data.value = getServices()
  })
</script>

<template>
  <div class="service">
    <search-form />
    <div v-for="item in data" :key="item.key" class="item">
      <div class="header">
        <div class="header-base-wrp">
          <a-avatar
            v-if="item.serviceName"
            :src="usePngImage(item.serviceName.toLowerCase())"
            :size="42"
            class="header-icon"
          />
          <div class="header-base-title">
            <a-typography-text :content="`${item.serviceName}`" />
            <a-typography-text class="small-font" type="secondary" :content="item.version" />
          </div>
          <div class="header-base-status">
            <a-tag :color="StatusColors[item.status]">
              <div class="header-base-status-inner">
                <status-dot :color="StatusColors[item.status]" />
                <span class="small-font">{{ $t(`common.${StatusTexts[item.status]}`) }}</span>
              </div>
            </a-tag>
          </div>
        </div>
        <div class="header-required-status">
          <a-typography-text class="small-font" type="secondary" :content="`${$t('common.restart')}:`" />
          <status-dot :color="StatusColors[item.status]" />
          <a-typography-text
            class="small-font"
            :content="`${item.restart ? $t('common.required') : $t('common.not_required')}`"
          />
        </div>
      </div>
      <div class="item-content">
        <button-group :auto="true" :space="0" :groups="actionGroups">
          <template #icon="{ item: groupItem }">
            <svg-icon :name="groupItem.icon || ''" />
          </template>
        </button-group>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
  .small-font {
    font-size: 12px;
  }

  .service {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
  }

  .item {
    width: 280px;
    height: 150px;
    border: 1px solid $color-border-secondary;
    border-radius: 8px;
    @include flexbox($direction: column);
  }

  .item-content {
    flex: 1;
    font-size: 12px;
    padding-inline: 10px;
    @include flexbox($align: center);
  }

  .header {
    @include flexbox($direction: column, $gap: 10px);
    padding: $space-md;
    border-bottom: 1px solid $color-border-secondary;
    &-base-wrp {
      @include flexbox($gap: $space-sm);
    }
    &-base-title {
      @include flexbox($direction: column);
    }
    &-base-status {
      flex: 1;
      text-align: end;
      &-inner {
        @include flexbox($align: center, $gap: 4px);
      }
      .ant-tag {
        margin-inline-end: 0;
      }
    }
    &-required-status {
      @include flexbox($align: center, $gap: 4px);
    }
    &-icon {
      flex-shrink: 0;
      border-radius: 0;
    }
    :deep(.ant-avatar) {
      img {
        object-fit: contain !important;
      }
    }
  }
</style>
