<!--
  - Licensed to the Apache Software Foundation (ASF) under one
  - or more contributor license agreements.  See the NOTICE file
  - distributed with this work for additional information
  - regarding copyright ownership.  The ASF licenses this file
  - to you under the Apache License, Version 2.0 (the
  - "License"); you may not use this file except in compliance
  - with the License.  You may obtain a copy of the License at
  -
  -    https://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing,
  - software distributed under the License is distributed on an
  - "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  - KIND, either express or implied.  See the License for the
  - specific language governing permissions and limitations
  - under the License.
  -->

<script setup lang="ts">
  import { SettingOutlined } from '@ant-design/icons-vue'
  import Job from './job.vue'
  import { ref } from 'vue'

  const jobWindowOpened = ref(false)
  const processJobNum = ref(0)

  const handleJobOpen = () => {
    jobWindowOpened.value = true
  }
</script>

<template>
  <div class="icon" @click="handleJobOpen">
    <a-badge size="small" color="blue" :count="processJobNum">
      <setting-outlined />
    </a-badge>
  </div>
  <job v-model:visible="jobWindowOpened" />
</template>

<style lang="scss" scoped>
  .icon {
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 16px;
    cursor: pointer;
    border-radius: 50%;
    height: 36px;
    width: 36px;

    &:hover {
      background-color: var(--hover-color);
    }
  }
</style>
