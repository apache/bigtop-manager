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
  import { ref, toRefs } from 'vue'
  import SelectLang from '@/components/select-lang/index.vue'
  import UserAvatar from '@/components/user-avatar/index.vue'
  import AiAssistant from '@/components/ai-assistant/index.vue'

  import { RouteRecordRaw } from 'vue-router'

  interface Props {
    headerSelectedKey: string
    headerMenus: RouteRecordRaw[]
  }

  interface Emits {
    (event: 'onHeaderClick', key: string): void
  }

  const props = withDefaults(defineProps<Props>(), {
    headerSelectedKey: '',
    headerMenus: () => []
  })

  const { headerSelectedKey, headerMenus } = toRefs(props)
  const githubUrl = import.meta.env.VITE_GITHUB_URL
  const spaceSize = ref(16)
  const aiAssistantRef = ref<InstanceType<typeof AiAssistant> | null>(null)

  const emits = defineEmits<Emits>()

  const handleHeaderSelect = ({ key }: any) => {
    emits('onHeaderClick', key)
  }

  const handleCommunication = () => {
    aiAssistantRef.value?.controlVisible()
  }
</script>

<template>
  <a-layout-header class="header">
    <h1 class="header-left common-layout">
      <svg-icon name="bm_logo" />
    </h1>
    <div class="header-menu">
      <a-menu :selected-keys="[headerSelectedKey]" theme="dark" mode="horizontal" @select="handleHeaderSelect">
        <a-menu-item v-for="route of headerMenus" :key="route.path">
          {{ $t(route.meta?.title || '') }}
        </a-menu-item>
      </a-menu>
    </div>
    <div class="header-right common-layout">
      <a-space :size="spaceSize">
        <user-avatar />
        <div class="header-item" @click="handleCommunication">
          <svg-icon name="communication" />
        </div>
        <select-lang />
        <div class="header-item">
          <a :href="githubUrl" target="_blank">
            <svg-icon name="github" />
          </a>
        </div>
        <div class="header-item">
          <svg-icon name="book" />
        </div>
      </a-space>
    </div>
    <ai-assistant ref="aiAssistantRef" />
  </a-layout-header>
</template>

<style scoped lang="scss">
  .common-layout {
    @include flexbox($justify: center, $align: center);
    height: 100%;
  }
  .header {
    @include flexbox($justify: space-between, $align: center);
    padding-inline: 0 $space-md;
    height: $layout-header-height;
    .header-menu {
      flex: 1;
    }
    .header-left {
      width: $layout-sider-width;
      margin: 0;
      flex-shrink: 0;
      :deep(.svg-icon) {
        width: 180px;
        height: 30px;
      }
    }

    nav {
      color: $color-white;
    }
  }
</style>
