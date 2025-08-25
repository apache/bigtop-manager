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
  import { Empty } from 'ant-design-vue'
  import type { DataNode, FieldNames, Key } from 'ant-design-vue/es/vc-tree/interface'

  interface Props {
    tree: any
    fieldNames?: FieldNames
    selectable?: boolean
  }

  interface Emits {
    (event: 'change', expandSelectedKeyPath: string): void
  }

  const props = withDefaults(defineProps<Props>(), {
    selectable: true,
    fieldNames: () => ({ children: 'children', title: 'title', key: 'key' })
  })

  const emits = defineEmits<Emits>()
  const checkSelectedKeys = ref<Key[]>([])

  const setupDefaultSelectKey = (treeNode: DataNode, fieldNames: Required<FieldNames>) => {
    const { children, key } = fieldNames
    const stack: string[] = []
    const pathParts: string[] = []

    function traverse(node: DataNode, stack: string[]): boolean {
      stack.push(node[key])
      if (!node[children] || node[children].length === 0) {
        pathParts.push(...stack)
        return true
      }
      for (const child of node[children]) {
        if (traverse(child, stack)) {
          return true
        }
      }
      stack.pop()
      return false
    }

    traverse(treeNode, stack)
    return pathParts.join('/')
  }

  const handleSelect = (selectedKeys: Key[], info?: any) => {
    const selectedKey = selectedKeys[0]
    if (!selectedKey) {
      return
    }
    const checkSelectedKey = checkSelectedKeys.value[0]
    if (selectedKey !== checkSelectedKey) {
      const keyPath = info?.node.parent?.key ? `${info.node.parent?.key}/${selectedKey}` : `${selectedKey}`
      checkSelectedKeys.value = selectedKeys
      emits('change', keyPath)
    }
  }

  watch(
    () => props.tree,
    (val) => {
      if (val[0]) {
        const findPath = setupDefaultSelectKey(val[0], props.fieldNames as Required<FieldNames>)
        const selectedKey = findPath.split('/').at(-1)
        checkSelectedKeys.value = selectedKey ? [selectedKey] : []
        emits('change', findPath)
      }
    },
    {
      immediate: true
    }
  )

  defineExpose({
    handleSelect
  })
</script>

<template>
  <div class="sidebar">
    <a-tree
      v-if="$props.tree.length > 0"
      :default-expand-all="true"
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
