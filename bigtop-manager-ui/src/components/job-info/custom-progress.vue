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
  import { computed } from 'vue'
  import { State } from '@/enums/state'
  import {
    MinusCircleFilled as Canceled,
    CheckCircleFilled as Successful,
    CloseCircleFilled as Failed
  } from '@ant-design/icons-vue'

  interface ProgressItem {
    state: keyof typeof State
    [property: string]: any
  }

  interface ProgressProps {
    state: keyof typeof State
    progressData: ProgressItem[]
  }

  const props = withDefaults(defineProps<ProgressProps>(), {})

  const progressConfig = computed(() => {
    if (props.state === 'Pending') {
      return {
        progress: 0,
        status: 'normal'
      }
    } else if (props.state === 'Successful') {
      return {
        progress: 100,
        status: 'success',
        icon: Successful
      }
    } else if (props.state === 'Processing') {
      const proportion =
        props.progressData.filter((v: ProgressItem) => v.state === 'Successful')
          .length + 1
      return {
        progress: Math.round((proportion / props.progressData.length) * 100),
        status: 'active'
      }
    } else if (props.state === 'Canceled') {
      return {
        progress: 100,
        status: 'normal',
        icon: Canceled
      }
    } else if (props.state === 'Failed') {
      return {
        progress: 100,
        status: 'exception',
        icon: Failed
      }
    } else {
      return {
        progress: 0,
        status: 'status'
      }
    }
  })
</script>

<template>
  <div>
    <a-progress
      :percent="progressConfig.progress"
      :status="progressConfig.status"
      :stroke-color="State[props.state]"
    >
      <template #format="percent">
        <span v-if="['Processing', 'Pending'].includes(props.state)">
          {{ percent }} %
        </span>
        <component
          :is="progressConfig.icon"
          v-else-if="progressConfig.icon"
          :style="{ color: State[props.state] }"
        />
      </template>
    </a-progress>
  </div>
</template>

<style lang="scss" scoped></style>
