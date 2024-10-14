<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements.  See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership.  The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License.  You may obtain a copy of the License at
  ~
  ~    https://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
-->

<script setup lang="ts">
import { theme as antdTheme } from 'ant-design-vue';
  import { useLocaleStore } from '@/store/locale'
  import { storeToRefs } from 'pinia'
  import { ref } from 'vue';
  
  const themes = {
    default: {
      token:{
      },
      algorithm: antdTheme.defaultAlgorithm,
    },
    dark: {
      token:{
      },
      algorithm: antdTheme.darkAlgorithm,
    },
    nature:{
      token:{
        colorPrimary: 'green',
      },
      algorithm: antdTheme.defaultAlgorithm,
    }
  };
  const localeStore = useLocaleStore()
  const { antd } = storeToRefs(localeStore)

const theme = ref(themes['default'])
const triggerTheme = (themeType: keyof typeof themes) => {
  theme.value = themes[themeType]
}

</script>

<template>
  <a-config-provider :locale="antd"  :theme="theme">
    <a-app class="app">
      <router-view />
    <div style="position: fixed; top: 16px; z-index: 10;left: 300px;">
      <a-button @click="() => triggerTheme('default')">Default</a-button>
      <a-button @click="() => triggerTheme('dark')">Dark</a-button>
      <a-button @click="() => triggerTheme('nature')">Nature</a-button>
    </div>
    </a-app>
  </a-config-provider>
</template>

<style scoped lang="scss">
  #app {
    width: 100%;
    box-sizing: border-box;
    position: fixed;
    top: 0;
    bottom: 0;
    left: 0;
  }

  .app {
    height: 100%;
    > section {
      height: 100%;
    }
  }
</style>
