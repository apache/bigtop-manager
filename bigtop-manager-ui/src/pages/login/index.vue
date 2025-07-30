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
  import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
  import { getSalt, getNonce, login } from '@/api/login'
  import { message } from 'ant-design-vue'
  import LoginLang from '@/features/login-lang/index.vue'
  import { deriveKey } from '@/utils/pbkdf2.ts'
  import { useUserStore } from '@/store/user'
  import { useMenuStore } from '@/store/menu'

  const { t } = useI18n()
  const userStore = useUserStore()
  const menuStore = useMenuStore()

  const router = useRouter()

  const formRef = shallowRef()
  const submitLoading = shallowRef(false)
  const loginModel = reactive({
    username: '',
    password: '',
    type: 'account',
    remember: true
  })

  const submit = async () => {
    submitLoading.value = true
    const hide = message.loading(t('login.logging_in'), 0)
    try {
      await formRef.value?.validate()
      const username = loginModel.username

      const salt = await getSalt(username).then(async (res: string) => {
        return res
      })

      const nonce = await getNonce(username).then(async (res: string) => {
        return res
      })

      const encryptPwd = deriveKey(loginModel.password, salt)

      const res = await login({
        username: username,
        password: encryptPwd,
        nonce: nonce
      })

      if (loginModel.remember) {
        localStorage.setItem('Token', res.token)
      } else {
        sessionStorage.setItem('Token', res.token)
      }
      userStore.getUserInfo()
      menuStore.setupMenu()

      message.success(t('login.login_success'))
      router.push('/')
    } catch (e) {
      console.warn(e)
    } finally {
      hide()
      submitLoading.value = false
    }
  }
</script>

<template>
  <div class="login-container">
    <div class="login-content">
      <div class="login-main">
        <!-- Login box header -->
        <div class="login-header">
          <div class="login-header-left">
            <img class="login-logo" src="@/assets/logo.svg" alt="logo" />
            <div class="login-title">Bigtop Manager</div>
            <div class="login-desc">{{ t('login.desc') }}</div>
          </div>
          <div class="login-header-right"><login-lang /></div>
        </div>
        <a-divider class="m-0" />
        <!-- Login box body -->
        <div class="login-body">
          <!-- On the left side of the login box -->
          <div class="login-body-left">
            <img class="login-body-left-img" src="@/assets/images/login.png" alt="login" />
          </div>
          <a-divider class="login-body-divider m-0" type="vertical" />
          <!-- Right side of the login box -->
          <div class="login-body-right">
            <div class="login-body-right-tips">{{ t('login.tips') }}</div>
            <a-form ref="formRef" class="login-body-right-form" :model="loginModel">
              <a-tabs v-model:active-key="loginModel.type" centered>
                <a-tab-pane key="account" :tab="t('login.tab_account')" />
              </a-tabs>
              <template v-if="loginModel.type === 'account'">
                <a-form-item
                  name="username"
                  :rules="[
                    {
                      required: true,
                      message: t('login.username_required')
                    }
                  ]"
                >
                  <a-input
                    v-model:value="loginModel.username"
                    allow-clear
                    :placeholder="t('login.username_placeholder')"
                    size="large"
                    @press-enter="submit"
                  >
                    <template #prefix>
                      <user-outlined />
                    </template>
                  </a-input>
                </a-form-item>
                <a-form-item
                  name="password"
                  :rules="[
                    {
                      required: true,
                      message: t('login.password_required')
                    }
                  ]"
                >
                  <a-input-password
                    v-model:value="loginModel.password"
                    allow-clear
                    :placeholder="t('login.password_placeholder')"
                    size="large"
                    @press-enter="submit"
                  >
                    <template #prefix>
                      <lock-outlined />
                    </template>
                  </a-input-password>
                </a-form-item>
              </template>
              <div class="login-body-right-form-bottom">
                <a-checkbox v-model:checked="loginModel.remember">
                  {{ t('login.remember_me') }}
                </a-checkbox>
              </div>
              <a-button type="primary" block :loading="submitLoading" size="large" @click="submit">
                {{ t('login.submit') }}
              </a-button>
            </a-form>
          </div>
        </div>
      </div>
    </div>
    <div class="copyright">
      Copyright ©2024–2025
      <a href="https://www.apache.org">The Apache Software Foundation</a>. All rights reserved.
    </div>
  </div>
</template>

<style scoped lang="scss">
  .login-container {
    @include flexbox($direction: column);
    height: 100vh;
    overflow: auto;
    background-color: var(--bg-color-container);

    .login-content {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      @include flexbox($justify: center, $align: center);

      .login-main {
        border-radius: 0.25rem;
        box-shadow: var(--c-shadow);

        @media screen and (max-width: 767px) {
          width: 350px;
        }

        @media (min-width: 768px) and (max-width: 991px) {
          width: 400px;
        }

        .login-header {
          @include flexbox($justify: space-between, $align: center);
          padding: 0.5rem 1rem;

          .login-header-left {
            @include flexbox($justify: space-between, $align: center);

            .login-title {
              font-weight: 600;
              font-size: 33px;

              @media (max-width: 991px) {
                font-size: 18px;
              }
            }

            .login-logo {
              width: 44px;
              height: 44px;
              margin-right: 1rem;
            }

            .login-desc {
              position: relative;
              top: 6px;
              color: var(--text-color-desc);
              font-size: 14px;
              margin-left: 1rem;

              @media (max-width: 991px) {
                display: none;
              }
            }
          }
        }

        .login-body {
          display: flex;
          box-sizing: border-box;
          min-height: 520px;

          @media (max-width: 991px) {
            min-height: 400px;
          }

          .login-body-left {
            @include flexbox($justify: center, $align: center);
            min-height: 520px;
            width: 700px;
            background-color: var(--bg-color-container);

            @media (max-width: 991px) {
              display: none;
            }

            .login-body-left-img {
              height: 83.333333%;
              width: 83.333333%;
            }
          }

          .login-body-divider {
            min-height: 520px;

            @media (max-width: 991px) {
              display: none;
            }
          }

          .login-body-right {
            @include flexbox($direction: column, $justify: center, $align: center);
            width: 335px;
            padding: 0 1.25rem;

            @media (max-width: 991px) {
              width: 100%;
            }

            .login-body-right-tips {
              text-align: center;
              padding: 1.5rem 0;
              font-size: 1.5rem;
              line-height: 2rem;
            }

            .login-body-right-form {
              .login-body-right-form-bottom {
                margin-bottom: 24px;
                @include flexbox($justify: space-between, $align: center);
              }
            }
          }
        }
      }
    }

    .copyright {
      padding: 24px 50px;
      width: 100vw;
      position: fixed;
      bottom: 0;
      text-align: center;
    }
  }
</style>
