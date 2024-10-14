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

<template>
  <div class="dot-state" :style="config"><slot></slot></div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue'
  interface Dot {
    width?: string | number
    height?: string | number
    color?: string
  }

  const props = withDefaults(defineProps<Dot>(), {
    width: '16px',
    height: '16px',
    color: '#f5222d'
  })

  const checkProps = (target: string | number): string => {
    if (typeof target === 'number') {
      return `${target}px`
    } else {
      const int = parseInt(target as string)
      if (isNaN(int)) {
        throw new Error('value is not NaN')
      } else {
        return `${int}px`
      }
    }
  }

  const config = computed(() => {
    const width = checkProps(props.width)
    const height = checkProps(props.width)
    return {
      '--state-w': width,
      '--state-h': height,
      '--state-color': props.color
    }
  })
</script>

<style lang="less" scoped>
  .dot(@width, @height, @color) {
    width: @width;
    height: @height;
    background: @color;
    border-radius: 50%;
  }
  .dot-state {
    &::before {
      content: '';
      margin: 8px 0 0;
      display: inline-block;
      .dot(var(--state-w), var(--state-h), var(--state-color));
    }
  }
</style>
