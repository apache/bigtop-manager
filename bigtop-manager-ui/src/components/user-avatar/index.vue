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
  import {
    UserOutlined,
    ProfileOutlined,
    LogoutOutlined
  } from '@ant-design/icons-vue'
  import { message } from 'ant-design-vue'
  import { useUserStore } from '@/store/user'
  import { storeToRefs } from 'pinia'
  import router from '@/router'
  import { useI18n } from 'vue-i18n'

  const i18n = useI18n()

  const userStore = useUserStore()
  const { userVO } = storeToRefs(userStore)

  const logout = async () => {
    const hide = message.loading(i18n.t('login.logging_out'), 0)
    try {
      await userStore.logout()
      message.success(i18n.t('login.logout_success'))
      await router.push({ path: '/login' })
    } catch (e) {
      console.warn(e)
    } finally {
      hide()
    }
  }
</script>

<template>
  <a-dropdown placement="bottom">
    <div class="header-menu-item">
      <svg-icon name="avatar" />
      <span class="name">{{ userVO?.nickname }}</span>
    </div>
    <template #overlay>
      <a-menu>
        <a-menu-item key="about">
          <template #icon>
            <user-outlined />
          </template>
          <router-link to="/user/profile">
            {{ $t('user.profile') }}
          </router-link>
        </a-menu-item>
        <a-menu-item key="settings">
          <template #icon>
            <profile-outlined />
          </template>
          <router-link to="/user/settings">
            {{ $t('user.settings') }}
          </router-link>
        </a-menu-item>
        <a-menu-divider />
        <a-menu-item key="logout" @click="logout">
          <template #icon>
            <logout-outlined />
          </template>
          {{ $t('user.logout') }}
        </a-menu-item>
      </a-menu>
    </template>
  </a-dropdown>
</template>

<style lang="scss" scoped>
  .name {
    // margin-left: 4px;
  }
</style>
