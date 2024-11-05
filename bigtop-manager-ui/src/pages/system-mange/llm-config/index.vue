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
  import LlmCard from './components/llm-card.vue'
</script>

<template>
  <div class="llm-config">
    <a-typography-title :level="5">大模型配置</a-typography-title>
    <div class="llm-config-content">
      <llm-card />
      <llm-card :is-config="false" />
    </div>
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
