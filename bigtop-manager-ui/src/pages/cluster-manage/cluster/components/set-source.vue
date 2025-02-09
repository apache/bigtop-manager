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
  import { computed, reactive, ref, toRefs } from 'vue'
  import { getRepoList, updateRepo } from '@/api/repo'
  import type { RepoVO } from '@/api/repo/types'
  import { message, type FormInstance, type TableColumnType } from 'ant-design-vue'
  import { useI18n } from 'vue-i18n'

  const { t } = useI18n()
  const open = ref(false)
  const loading = ref(false)
  const formRef = ref<FormInstance>()
  const form = reactive({
    list: [] as RepoVO[]
  })
  const { list } = toRefs(form)

  const columns = computed((): TableColumnType[] => [
    {
      title: t('common.name'),
      dataIndex: 'name',
      key: 'name',
      ellipsis: true
    },
    {
      title: t('common.arch'),
      dataIndex: 'arch',
      key: 'arch',
      ellipsis: true
    },
    {
      title: t('common.base_url'),
      dataIndex: 'baseUrl',
      key: 'baseUrl',
      ellipsis: true
    }
  ])

  const handleOpen = () => {
    open.value = true
    getSource()
  }

  const getSource = async () => {
    loading.value = true
    try {
      const data = await getRepoList()
      list.value = data
    } catch (error) {
      console.log('error :>> ', error)
    } finally {
      loading.value = false
    }
  }

  const updateSourceUrl = async () => {
    try {
      const params = list.value.map(({ id, baseUrl }) => ({ id, baseUrl }))
      await updateRepo(params)
      formRef.value?.resetFields()
      message.success(t('common.update_success'))
      open.value = false
    } catch (error) {
      message.error(t('common.update_fail'))
      console.log('error :>> ', error)
    }
  }

  const handleOk = () => {
    formRef.value
      ?.validateFields()
      .then(() => {
        updateSourceUrl()
      })
      .catch((info) => {
        console.log('Validate Failed:', info)
      })
  }

  const handleCancel = () => {
    open.value = false
  }

  defineExpose({
    handleOpen
  })
</script>

<template>
  <div class="set-source">
    <a-modal
      v-model:open="open"
      width="50%"
      :centered="true"
      :mask="false"
      :title="$t('cluster.source')"
      :mask-closable="false"
      :destroy-on-close="true"
      @ok="handleOk"
      @cancel="handleCancel"
    >
      <a-form ref="formRef" :model="form">
        <a-table :loading="loading" :scroll="{ y: 340 }" :data-source="list" :columns="columns" :pagination="false">
          <template #bodyCell="{ index, column, record }">
            <template v-if="column.dataIndex == 'baseUrl'">
              <a-form-item
                label=" "
                :colon="false"
                :name="['list', index, 'baseUrl']"
                :rules="[{ required: true, message: $t('common.enter_error'), trigger: 'blur' }]"
              >
                <a-input v-model:value="record[column.key]" />
              </a-form-item>
            </template>
          </template>
        </a-table>
      </a-form>
      <a-typography-text class="set-source-tip" type="danger">{{ `*${$t('common.note')}` }}</a-typography-text>
    </a-modal>
  </div>
</template>

<style lang="scss" scoped>
  .set-source {
    &-tip {
      margin-top: $space-sm;
      font-size: 12px;
    }
  }
  :deep(.ant-form-item) {
    margin-bottom: 0;
  }
</style>
