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
  import { StatusType, StatusColors } from '@/enums/state'

  interface StatusDot {
    size?: number
    color?: StatusType
    content?: string
  }

  const props = withDefaults(defineProps<StatusDot>(), {
    color: 'default',
    size: 8,
    content: ''
  })
  const fontSize = computed(() => props.size * 0.6)
  const dotStyle = computed(() => ({
    width: props.size + 'px',
    height: props.size + 'px',
    fontSize: fontSize.value + 'px',
    backgroundColor: StatusColors[props.color]
  }))
</script>

<template>
  <div class="dot" :style="dotStyle">
    {{ content }}
  </div>
</template>

<style scoped>
  .dot {
    flex-shrink: 0;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    color: white;
  }
</style>
