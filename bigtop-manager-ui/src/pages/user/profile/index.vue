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
  import { FormInstance, message } from 'ant-design-vue'
  import { useUserStore } from '@/store/user'
  import { UserReq } from '@/api/user/types.ts'

  const { t } = useI18n()
  const userStore = useUserStore()
  const { userVO } = storeToRefs(userStore)
  const editUser = reactive<UserReq>({} as UserReq)
  watch(userVO, () => {
    resetEditUser()
  })

  const formRef = ref<FormInstance>()
  const loading = ref<boolean>(false)
  const open = ref<boolean>(false)

  const resetEditUser = async () => {
    formRef.value?.clearValidate()
    editUser.nickname = userVO.value?.nickname as string
  }

  const updateCurrentUser = async (editUser: UserReq) => {
    try {
      loading.value = true
      await formRef.value?.validate()
      await userStore.updateUserInfo(editUser)
      open.value = false
      message.success(t('common.update_success'))
    } catch (error) {
      console.log('error', error)
      message.error(t('common.update_fail'))
    } finally {
      loading.value = false
    }
  }

  const editProfile = async () => {
    open.value = true
  }

  const cancelEdit = async () => {
    open.value = false
    await resetEditUser()
  }

  onMounted(async () => {
    await resetEditUser()
  })
</script>

<template>
  <a-descriptions :title="t('user.profile')" bordered>
    <template #extra>
      <a-button type="primary" @click="editProfile">
        {{ t('common.edit') }}
      </a-button>
    </template>
    <a-descriptions-item :label="t('user.username')" :span="3">
      {{ userVO?.username }}
    </a-descriptions-item>
    <a-descriptions-item :label="t('user.nickname')" :span="3">
      {{ userVO?.nickname }}
    </a-descriptions-item>
    <a-descriptions-item :label="t('common.create_time')" :span="3">
      {{ userVO?.createTime }}
    </a-descriptions-item>
    <a-descriptions-item :label="t('common.update_time')" :span="3">
      {{ userVO?.updateTime }}
    </a-descriptions-item>
    <a-descriptions-item :label="t('common.status')" :span="3">
      {{ userVO?.status }}
    </a-descriptions-item>
  </a-descriptions>
  <div>
    <a-modal
      v-model:open="open"
      :centered="true"
      :title="t('common.edit')"
      :mask-closable="false"
      :closable="false"
      @cancel="cancelEdit"
    >
      <br />
      <a-form ref="formRef" name="profileForm" :model="editUser" layout="vertical">
        <a-form-item
          :label="t('user.nickname')"
          name="nickname"
          :rules="[
            {
              required: true,
              message: t('user.set_nickname_valid')
            }
          ]"
        >
          <a-input v-model:value="editUser.nickname" allow-clear />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-button key="cancel" @click="cancelEdit">
          {{ t('common.cancel') }}
        </a-button>
        <a-button
          key="submit"
          type="primary"
          :loading="loading"
          html-type="submit"
          @click="updateCurrentUser(editUser)"
        >
          {{ t('common.submit') }}
        </a-button>
      </template>

      <br />
    </a-modal>
  </div>
</template>

<style scoped lang="scss"></style>
