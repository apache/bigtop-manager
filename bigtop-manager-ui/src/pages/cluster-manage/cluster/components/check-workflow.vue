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
  import { getCheckWorkflows } from './mock'

  const activeKey = ref(['1', '2'])
  const stages = ref(getCheckWorkflows(4, 5, 5))
  const status = ['', 'success', 'error', 'loading', 'loading_light', 'uninstall']
</script>

<template>
  <div class="check-workflow">
    <div class="retry">
      <a-button type="link">{{ $t('common.retry') }}</a-button>
    </div>
    <a-collapse v-model:active-key="activeKey" :bordered="false" :ghost="true">
      <a-collapse-panel v-for="(stage, idx) in stages" :key="`${idx + 1}`">
        <template #header>
          <div class="stage-item">
            <span>{{ stage.name }}</span>
            <svg-icon :name="status[stage.status]"></svg-icon>
          </div>
        </template>
        <div v-for="task in stage.tasks" :key="task.id" class="task-item">
          <span>{{ task.name }}</span>
          <a-space :size="16">
            <svg-icon :name="status[task.status]"></svg-icon>
            <a-button type="link">{{ $t('cluster.view_log') }}</a-button>
          </a-space>
        </div>
      </a-collapse-panel>
    </a-collapse>
  </div>
</template>

<style lang="scss" scoped>
  .check-workflow {
    button {
      padding: 0;
    }
    :deep(.ant-collapse-header) {
      background-color: $color-fill-quaternary;
    }
    :deep(.ant-collapse-content-box) {
      padding: 0 !important;
    }
    .retry {
      text-align: end;
      line-height: 14px;
      margin-bottom: $space-sm;
      button {
        margin: 0;
        padding: 0;
        line-height: inherit;
        height: 0;
      }
    }
  }
  .stage-item {
    @include flexbox($justify: space-between, $align: center);
    padding-right: 68px;
  }
  .task-item {
    height: 45px;
    padding: 10px;
    box-sizing: border-box;
    padding-left: $space-lg;
    border-top: 1px solid $color-border-secondary;
    @include flexbox($justify: space-between, $align: center);
    &:last-child {
      border-bottom: 1px solid $color-border-secondary;
    }
  }
</style>
