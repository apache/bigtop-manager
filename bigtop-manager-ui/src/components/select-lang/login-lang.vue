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
  import { useLocaleStore } from '@/store/locale'
  import { Locale } from '@/store/locale/types.ts'
  import { storeToRefs } from 'pinia'

  const localeStore = useLocaleStore()
  const { locale } = storeToRefs(localeStore)

  const handleClick = ({ key }: { key: Locale }) => {
    localeStore.setLocale(key)
  }
</script>

<template>
  <a-dropdown placement="bottom">
    <div class="icon">
      <svg-icon name="carbon-language" />
    </div>
    <template #overlay>
      <a-menu :selected-keys="[locale]" @click="handleClick">
        <a-menu-item key="en_US">
          <template #icon>
            <span>🇺🇸</span>
          </template>
          English
        </a-menu-item>
        <a-menu-item key="zh_CN">
          <template #icon>
            <span>🇨🇳</span>
          </template>
          简体中文
        </a-menu-item>
      </a-menu>
    </template>
  </a-dropdown>
</template>

<style lang="scss" scoped>
  .icon {
    @include flexbox($justify: center, $align: center);
    font-size: 16px;
    cursor: pointer;
    border-radius: 50%;
    height: 36px;
    width: 36px;

    &:hover {
      background-color: var(--hover-color);
    }
  }
</style>
