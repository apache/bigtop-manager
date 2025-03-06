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
  import { type TreeProps, Empty } from 'ant-design-vue'

  interface Props {
    tree: any
    expandSelectedKeys?: string
    fieldNames?: TreeProps['fieldNames']
    selectable?: boolean
  }

  interface Emits {
    (event: 'select', ...args: any): void
    (event: 'update:expandSelectedKeys', expandSelectedKeys: string): void
  }

  withDefaults(defineProps<Props>(), {
    selectable: true,
    expandSelectedKeys: '',
    fieldNames: () => ({ children: 'children', title: 'title', key: 'key' })
  })

  const emits = defineEmits<Emits>()
  const expandedKeys = ref<string[]>([])
  const checkSelectedKeys = ref<string[]>([])

  const handleSelect: TreeProps['onSelect'] = (selectedKeys, e) => {
    const selectedKey = selectedKeys[0]
    if (!selectedKey) {
      return
    }
    const checkSelectedKey = checkSelectedKeys.value[0]
    if (selectedKey !== checkSelectedKey) {
      checkSelectedKeys.value = selectedKeys as string[]
      emits('update:expandSelectedKeys', `${e.node.parent?.key}/${selectedKeys[0]}`)
    }
  }
</script>

<template>
  <div class="sidebar">
    <a-tree
      v-if="$props.tree.length > 0"
      v-model:expandedKeys="expandedKeys"
      :selected-keys="checkSelectedKeys"
      :selectable="$props.selectable"
      :tree-data="$props.tree"
      :field-names="$props.fieldNames"
      @select="handleSelect"
    />
    <a-empty v-else :image="Empty.PRESENTED_IMAGE_SIMPLE" />
  </div>
</template>

<style lang="scss" scoped></style>
