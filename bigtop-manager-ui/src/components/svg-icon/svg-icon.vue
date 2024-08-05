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
  import { computed } from 'vue'

  interface SvgIconProps {
    prefix?: string
    name: string
    color?: string
    className?: string
  }

  const props = withDefaults(defineProps<SvgIconProps>(), {
    prefix: 'icon',
    color: '#000',
    className: ''
  })

  const symbolId = computed(() => `#${props.prefix}-${props.name}`)
  const svgClass = computed(() => {
    if (props.className) {
      return `svg-icon ${props.className}`
    }
    return 'svg-icon'
  })
</script>

<template>
  <svg :class="svgClass" aria-hidden="true">
    <use :xlink:href="symbolId" :fill="color" />
  </svg>
</template>

<style lang="scss" scoped>
  .svg-icon {
    height: 1.2em;
    width: 1.2em;
    margin-right: 6px;
    vertical-align: -0.25em;
    overflow: hidden;
  }
</style>
