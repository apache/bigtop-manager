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
  import ClusterCreate from '@/components/cluster-create/index.vue'

  const open = ref(false)
</script>

<template>
  <div class="tour">
    <a-result
      :title="$t('cluster.not_exist_title')"
      :sub-title="$t('cluster.not_exist_sub_title')"
    >
      <template #extra>
        <a-button type="primary" size="large" @click="() => (open = true)">
          {{ $t('cluster.create') }}
        </a-button>
        <cluster-create v-model:open="open" />
      </template>
    </a-result>
  </div>
</template>

<style scoped lang="less">
  .tour {
    .flexbox-mixin(column, null, center,center);
    align-content: center;
    min-height: 540px;
  }
</style>
