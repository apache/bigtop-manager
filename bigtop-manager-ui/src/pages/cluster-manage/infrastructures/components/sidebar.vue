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
  import { ref, watch } from 'vue'
  import type { TreeProps } from 'ant-design-vue'
  import type { Key } from 'ant-design-vue/es/_util/type'

  interface Props {
    data: any[]
    fieldNames?: TreeProps['fieldNames']
  }

  interface Emits {
    (event: 'select', selectedKeys: Key[]): void
  }

  const props = defineProps<Props>()
  const emits = defineEmits<Emits>()
  const expandedKeys = ref<string[]>([])
  const selectedKeys = ref<string[]>([])
  const treeData = ref<TreeProps['treeData']>([])

  const handleSelect: TreeProps['onSelect'] = (selectedKeys) => {
    emits('select', selectedKeys)
  }

  watch(
    () => props.data,
    (val) => {
      treeData.value = val
    },
    {
      immediate: true,
      deep: true
    }
  )
</script>

<template>
  <div class="sidebar">
    <a-tree
      v-model:expandedKeys="expandedKeys"
      v-model:selectedKeys="selectedKeys"
      :selectable="true"
      :tree-data="treeData"
      :field-names="$props.fieldNames"
      @select="handleSelect"
    />
  </div>
</template>

<style lang="scss" scoped></style>
