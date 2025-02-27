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
  import type { TreeProps } from 'ant-design-vue'

  const expandedKeys = ref<string[]>([])
  const selectedKeys = ref<string[]>([])
  const treeData = ref<TreeProps['treeData']>([
    { title: 'ZooKeeper', key: '0', isLeaf: true },
    { title: 'ZooKeeper', key: '1', isLeaf: true },
    { title: 'ZooKeeper', key: '2', isLeaf: true }
  ])
  const onLoadData: TreeProps['loadData'] = (treeNode) => {
    return new Promise<void>((resolve) => {
      if (treeNode.dataRef?.children) {
        resolve()
        return
      }
      setTimeout(() => {
        treeNode.dataRef!.children = [
          { title: 'Child Node', key: `${treeNode.eventKey}-0` },
          { title: 'Child Node', key: `${treeNode.eventKey}-1` }
        ]
        treeData.value = [...(treeData.value || [])]
        resolve()
      }, 1000)
    })
  }
</script>

<template>
  <div class="sidebar">
    <a-tree
      v-model:expandedKeys="expandedKeys"
      v-model:selectedKeys="selectedKeys"
      :selectable="false"
      :load-data="onLoadData"
      :tree-data="treeData"
    />
  </div>
</template>

<style lang="scss" scoped></style>
