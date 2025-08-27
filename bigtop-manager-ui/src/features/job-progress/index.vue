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

<script lang="ts" setup>
  import { JobState } from '@/enums/state'

  interface ProgressProps {
    state: keyof typeof JobState
    progressData: number
  }

  interface ProgressConfig {
    status: 'success' | 'normal' | 'active' | 'exception' | undefined
    progress: number
    icon?: string
    strokeColor?: string
  }

  const props = withDefaults(defineProps<ProgressProps>(), {
    progressData: 1
  })

  const progressConfig = computed((): ProgressConfig => {
    if (props.state === 'Pending') {
      return {
        progress: 0,
        status: 'normal'
      }
    } else if (props.state === 'Successful') {
      return {
        progress: 100,
        status: 'success',
        icon: 'success'
      }
    } else if (props.state === 'Processing') {
      return {
        progress: props.progressData ?? 1,
        status: 'active'
      }
    } else if (props.state === 'Canceled') {
      return {
        progress: 100,
        status: 'normal',
        icon: 'canceled',
        strokeColor: '#faad14'
      }
    } else if (props.state === 'Failed') {
      return {
        progress: 100,
        status: 'exception',
        icon: 'failed'
      }
    } else {
      return {
        progress: 0,
        status: 'normal'
      }
    }
  })
</script>

<template>
  <div>
    <a-progress
      :percent="progressConfig.progress"
      :status="progressConfig.status"
      :stroke-color="progressConfig.strokeColor || ''"
    >
      <template #format="percent">
        <span v-if="['Processing', 'Pending'].includes(props.state)"> {{ percent }} % </span>
        <slot v-else-if="progressConfig.icon" name="icon">
          <svg-icon :style="{ margin: 0, 'vertical-align': '-0.5em' }" :name="progressConfig.icon" />
        </slot>
      </template>
    </a-progress>
  </div>
</template>

<style lang="scss" scoped>
  :deep(.ant-progress-line) {
    margin-bottom: 0px !important;
    .ant-progress-text {
      line-height: 0;
    }
  }
</style>
