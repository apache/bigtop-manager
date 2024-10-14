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
  import SelectLang from '@/components/select-lang/index.vue'
  import UserAvatar from '@/components/user-avatar/index.vue'
  import { ref } from 'vue'
  const spaceSize = ref(8)
</script>

<template>
  <a-layout-header class="header">
    <div class="header-left common-layout">
      <svg-icon name="big-manager-logo" />
    </div>
    <div class="header-right common-layout">
      <a-space :size="spaceSize">
        <user-avatar />
        <select-lang />
        <div class="header-item">
          <svg-icon name="github" />
        </div>
        <div class="header-item">
          <svg-icon name="book" />
        </div>
      </a-space>
    </div>
  </a-layout-header>
</template>

<style scoped lang="less">
  .common-layout {
    .flexbox-mixin(null,null,center,center);
    height: 100%;
  }
  .header {
   .flexbox-mixin(null,null,space-between,center);
    padding: 0 24px 0 10px;
    height: @layout-header-height;

    .header-left {
      :deep(.svg-icon) {
        width: 180px;
        height: 30px;
      }
    }
  }
</style>
