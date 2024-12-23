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

  const data = ref(
    Array.from({ length: 20 }, (_, i) => ({
      key: i,
      title: `title${i + 1}wwwwwwwwwww`
    }))
  )
</script>

<template>
  <div class="service">
    <div v-for="item in data" :key="item.key" class="item">
      <div class="item-header">
        <div class="item-header-title-wrp">
          <a-avatar :size="42" class="item-header-icon" />
          <div class="item-header-title">
            <a-typography-text
              class="item-header-title-tip"
              :ellipsis="item.title ? { tooltip: item.title } : false"
              :content="`${item.title}`"
            />
            <a-typography-text
              class="item-header-title-tip"
              type="secondary"
              :ellipsis="{ tooltip: 'flink-1.16.2-1' }"
              content="flink-1.16.2-1"
            />
          </div>
        </div>
        <div>
          <a-typography-text type="secondary" content="需要重启：是" />
        </div>
      </div>
      <div class="item-content"> content </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
  .service {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
  }
  .item {
    flex-shrink: 0;
    width: 280px;
    height: 150px;
    border: 1px solid $color-border-secondary;
    border-radius: $space-sm;
    &-header {
      display: grid;
      gap: 10px;
      padding: $space-md;
      border-bottom: 1px solid $color-border-secondary;
      &-title-wrp {
        display: flex;
        gap: $space-sm;
      }
      &-title {
        display: grid;
        &-tip {
          width: 200px;
        }
      }
      &-icon {
        flex-shrink: 0;
        border-radius: 0;
      }
    }

    &-content {
      padding: $space-md;
    }
  }
</style>
