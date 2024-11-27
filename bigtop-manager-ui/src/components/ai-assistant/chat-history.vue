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

  interface Props {
    visible: boolean
  }

  defineProps<Props>()

  const selectedKey = ref(0)
  const items = ref(
    Array.from({ length: 100 }, (_, i) => {
      return {
        key: `${i}`,
        label: `Chat ${i}Chat ${i}Chat ${i}Chat ${i}Chat ${i}Chat ${i}Chat ${i}Chat ${i}Chat ${i}Chat ${i}Chat ${i}Chat ${i}`,
        title: `Chat ${i}`
      }
    })
  )

  const handleClick = () => {}
</script>

<template>
  <div v-if="$props.visible" class="chat-history">
    <header>
      <a-typography-title :level="5">历史记录</a-typography-title>
    </header>
    <main>
      <a-menu v-model:selectedKeys="selectedKey" @click="handleClick">
        <a-menu-item v-for="item in items" :key="item.key" :title="item.title">
          <span>{{ item.label }}</span>
        </a-menu-item>
      </a-menu>
    </main>
    <footer>
      <a-button type="primary">新建会话</a-button>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
  .chat-history {
    position: fixed;
    left: 0;
    top: 64px;
    height: 100%;
    z-index: 10;
    width: '300px';
    background-color: #f7f9fc;

    display: grid;
    grid-template-rows: repeat(1, 56px 1fr 158px);

    header {
      padding: $space-md;
    }

    main {
      padding-inline: $space-md;
      overflow: auto;
      :deep(.ant-menu) {
        background: initial !important;
        border: none;
        .ant-menu-item {
          border-radius: 0;
        }
        .ant-menu-item-selected {
          background-color: $color-white;
        }
      }
    }

    footer {
      padding-inline: $space-md;
      text-align: center;
      button {
        height: 40px;
        width: 100%;
      }
    }
  }
</style>
