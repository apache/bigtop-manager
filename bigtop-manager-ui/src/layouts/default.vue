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
  const { t } = useI18n()
</script>

<template>
  <a-card>
    <div class="default">
      <img :src="usePngImage('default')" />
      <a-typography-text>{{ t('cluster.cluster_unavailable_message') }}</a-typography-text>
      <a-typography-link underline @click="() => $router.push({ name: 'CreateCluster' })">
        {{ t('menu.create') }}
      </a-typography-link>
    </div>
  </a-card>
</template>

<style scoped lang="scss">
  .default {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    gap: 8px;
    height: 630px;
    img {
      width: 400px;
      height: 200px;
    }
  }
</style>
