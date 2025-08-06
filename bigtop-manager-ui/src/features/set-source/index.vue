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
  import { getRepoList, updateRepo } from '@/api/repo'
  import UpdateAddress from './components/update-address.vue'

  import { type FormInstance, message, type TableColumnType } from 'ant-design-vue'
  import type { RepoVO } from '@/api/repo/types'

  const { t } = useI18n()
  const open = ref(false)
  const loading = ref(false)
  const formRef = ref<FormInstance>()
  const type = ref<number>(1)
  const list = ref<RepoVO[]>([])
  const updateAddressRef = ref<InstanceType<typeof UpdateAddress> | null>(null)
  const listTypeMap = shallowRef(['', 'serviceList', 'dependencyList'])

  const form = reactive<Record<string, RepoVO[]>>({
    serviceList: [],
    dependencyList: []
  })

  const { serviceList, dependencyList } = toRefs(form)

  const sourceTypeTabs = computed(() => [t('common.service'), t('component.dependency')])
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
      title: t('component.base_url'),
      dataIndex: 'baseUrl',
      key: 'baseUrl',
      ellipsis: true
    },
    {
      title: t('component.pkg_name'),
      dataIndex: 'pkgName',
      key: 'pkgName',
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
      list.value = await getRepoList()
      onTabsChange({ target: { value: type.value } })
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

  const handleOk = async () => {
    const pass = await checkAllRules()
    if (!pass) return
    updateSourceUrl()
  }

  const handleCancel = () => {
    open.value = false
    type.value = 1
    Object.assign(form, { serviceList: [], dependencyList: [] })
  }

  const checkAllRules = async () => {
    const errorMap = list.value.reduce(
      (pre, val) => {
        if (val.baseUrl === '') {
          if (!pre[val.type]) {
            pre[val.type] = []
          }
          pre[val.type].push(Number(val.id))
        }
        return pre
      },
      {} as Record<string, number[]>
    )

    const types = Object.keys(errorMap)

    if (types.length > 0) {
      onTabsChange({ target: { value: Number(types[0]) } })
      await nextTick() // wait tab rendered

      const idx = form[listTypeMap.value[type.value]].findIndex((v) => v.id === errorMap[type.value][0])
      const pathName = [listTypeMap.value[type.value], idx, 'baseUrl']

      if (idx != -1) {
        await nextTick() // wait dom rendered
        formRef.value?.scrollToField(pathName)
        await formRef.value?.validateFields([pathName]).catch((error) => console.log('error', error))
      } else {
        formRef.value?.clearValidate()
      }
    }

    return types.length === 0
  }

  const onTabsChange = ({ target }) => {
    formRef.value?.clearValidate()
    type.value = Number(target.value)
    form[listTypeMap.value[type.value]] = list.value.filter((v) => v.type === type.value)
  }

  const setNewAddress = () => {
    updateAddressRef.value?.toggleVisible(true)
  }

  const handleSetAddress = (newAddress: string) => {
    list.value.forEach((v) => (v.baseUrl = newAddress))
    message.success(t('common.update_success'))
  }

  defineExpose({
    handleOpen
  })
</script>

<template>
  <div class="set-source">
    <a-modal
      v-model:open="open"
      width="60%"
      :centered="true"
      :mask="false"
      :title="t('cluster.source')"
      :mask-closable="false"
      :destroy-on-close="true"
      :after-close="handleCancel"
    >
      <div class="set-source-operate">
        <a-radio-group v-model:value="type" button-style="solid" @change="onTabsChange">
          <a-radio-button v-for="(label, index) in sourceTypeTabs" :key="index + 1" :value="index + 1">
            {{ label }}
          </a-radio-button>
        </a-radio-group>
        <a-button type="primary" @click="setNewAddress">{{ t('component.update_all') }}</a-button>
      </div>
      <a-form ref="formRef" :model="form">
        <a-table
          v-show="type === 1"
          :loading="loading"
          :scroll="{ y: 350 }"
          :data-source="[...serviceList]"
          :columns="columns"
          :pagination="false"
        >
          <template #bodyCell="{ index, column, record }">
            <template v-if="column.key == 'baseUrl'">
              <a-form-item
                label=" "
                :colon="false"
                :name="[`serviceList`, index, 'baseUrl']"
                :rules="[
                  {
                    required: true,
                    message: t('common.enter_error', [`${t('component.base_url')}`.toLowerCase()]),
                    trigger: ['change', 'blur']
                  }
                ]"
              >
                <a-input v-model:value="record[column.key]" />
              </a-form-item>
            </template>
            <template v-if="column.key == 'pkgName'">
              <span>{{ record[column.key] || '-' }}</span>
            </template>
          </template>
        </a-table>
        <a-table
          v-show="type === 2"
          :loading="loading"
          :scroll="{ y: 350 }"
          :data-source="dependencyList"
          :columns="columns"
          :pagination="false"
        >
          <template #bodyCell="{ index, column, record }">
            <template v-if="column.key == 'baseUrl'">
              <a-form-item
                label=" "
                :colon="false"
                :name="[`dependencyList`, index, 'baseUrl']"
                :rules="[{ required: true, message: t('common.enter_error'), trigger: ['change', 'blur'] }]"
              >
                <a-input v-model:value="record[column.key]" />
              </a-form-item>
            </template>
            <template v-if="column.key == 'pkgName'">
              <span>{{ record[column.key] || '-' }}</span>
            </template>
          </template>
        </a-table>
      </a-form>
      <template #footer>
        <div class="set-source-footer">
          <a-typography-text class="set-source-tip" type="danger">{{ `*${t('component.note')}` }}</a-typography-text>
          <a-space>
            <a-button @click="handleCancel">{{ t('common.cancel') }}</a-button>
            <a-button type="primary" @click="handleOk">{{ t('common.confirm') }}</a-button>
          </a-space>
        </div>
      </template>
    </a-modal>
    <update-address ref="updateAddressRef" @on-ok="handleSetAddress" />
  </div>
</template>

<style lang="scss" scoped>
  .set-source {
    min-width: 400px;
    &-tip {
      font-size: 12px;
    }
    &-operate {
      display: flex;
      justify-content: space-between;
      margin: 16px 0;
    }
    &-footer {
      text-align: start;
      display: flex;
      justify-content: space-between;
      gap: 16px;
    }
  }
  :deep(.ant-form-item) {
    margin-bottom: 0;
  }
</style>
