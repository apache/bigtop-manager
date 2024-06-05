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
  import { SmileOutlined, FrownOutlined } from '@ant-design/icons-vue'

  const clusterInfo = defineModel<any>('clusterInfo')
</script>

<template>
  <div class="container">
    <a-result
      v-if="clusterInfo.success"
      :title="$t('cluster.create_success_title')"
      :sub-title="$t('cluster.create_success_sub_title')"
    >
      <template #icon>
        <smile-outlined />
      </template>
    </a-result>
    <a-result
      v-else
      :title="$t('cluster.create_fail_title')"
      :sub-title="$t('cluster.create_fail_sub_title')"
    >
      <template #icon>
        <frown-outlined />
      </template>
    </a-result>
  </div>
</template>

<style scoped lang="scss">
  .container {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    align-content: center;
    height: 100%;
  }
</style>
