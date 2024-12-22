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
  import type { FormInstance, TableColumnType } from 'ant-design-vue'
  import { ref, watch } from 'vue'

  const open = ref(false)
  const loading = ref(false)
  const columns: TableColumnType[] = [
    {
      title: '名称',
      dataIndex: 'name',
      ellipsis: true
    },
    {
      title: '架构',
      dataIndex: 'architecture',
      ellipsis: true
    },
    {
      title: '地址',
      dataIndex: 'address',
      ellipsis: true
    }
  ]
  const formRef = ref<FormInstance>()
  const formRule = ref({
    list: [] as any[]
  })

  const handleOpen = () => {
    open.value = true
  }

  const handleOk = () => {
    formRef.value
      ?.validateFields()
      .then((values) => {
        console.log('Received values of form: ', values)
        formRef.value?.resetFields()
        open.value = false
      })
      .catch((info) => {
        console.log('Validate Failed:', info)
      })
  }

  const handleCancel = () => {
    open.value = false
  }

  function generateMockData(rowCount = 10) {
    return Array.from({ length: rowCount }, (_, i) => ({
      id: i + 1,
      name: `名称_${i + 1}`,
      architecture: `架构_${i + 1}`,
      address: `https://example.com/${i + 1}`
    }))
  }

  watch(open, (val) => {
    if (val) {
      formRule.value.list = generateMockData(12)
    }
  })

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
      <a-form ref="formRef" :model="formRule">
        <a-table
          :loading="loading"
          :scroll="{ y: 340 }"
          :data-source="formRule.list"
          :columns="columns"
          :pagination="false"
        >
          <template #bodyCell="{ column, record, index }">
            <template v-if="column.dataIndex == 'address'">
              <a-form-item
                class="custom-form-item"
                label=" "
                :colon="false"
                :name="`list[${index}].address`"
                :rules="[{ required: true, message: '请输入地址', trigger: 'blur' }]"
              >
                <a-input v-model:value="record[column.dataIndex]" style="width: 100%"></a-input>
              </a-form-item>
            </template>
          </template>
        </a-table>
      </a-form>
      <a-typography-text class="set-source-tip" type="danger">*注: 源地址的改动对所有集群生效</a-typography-text>
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
</style>
