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
  import { computed, ref, shallowRef } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { message, Modal, TableColumnType } from 'ant-design-vue'
  import {
    deleteServiceConfigSnapshot,
    getServiceConfigSnapshotsList,
    recoveryServiceConfigSnapshot
  } from '@/api/service'
  import useBaseTable from '@/composables/use-base-table'
  import type { GroupItem } from '@/components/common/button-group/types'
  import type { ServiceConfigSnapshot, ServiceParams, SnapshotRecovery } from '@/api/service/types'

  const { t } = useI18n()
  const open = ref(false)
  const serviceInfo = shallowRef<ServiceParams>()
  // const emits = defineEmits(['success'])

  const columns = computed((): TableColumnType[] => [
    {
      title: t('service.snapshot_name'),
      dataIndex: 'name',
      key: 'name',
      ellipsis: true
    },
    {
      title: t('service.snapshot_notes'),
      dataIndex: 'desc',
      key: 'desc',
      ellipsis: true
    },
    {
      title: t('common.create_time'),
      dataIndex: 'createTime',
      key: 'createTime',
      ellipsis: true
    },
    {
      title: t('common.action'),
      dataIndex: 'operation',
      width: '160px',
      key: 'operation',
      ellipsis: true
    }
  ])

  const { loading, dataSource, resetState } = useBaseTable<ServiceConfigSnapshot>({
    columns: columns.value,
    rows: [],
    pagination: false
  })

  const operations = computed((): GroupItem[] => [
    {
      text: 'restore',
      clickEvent: (_item, args) => handleRestore(args)
    },
    {
      text: 'remove',
      danger: true,
      clickEvent: (_item, args) => handleDelete(args)
    }
  ])

  const handleRestore = async (payLoad: ServiceConfigSnapshot) => {
    const params = {
      ...serviceInfo.value,
      snapshotId: payLoad.id
    } as SnapshotRecovery
    Modal.confirm({
      title: t('common.restore_msg'),
      async onOk() {
        try {
          const data = await recoveryServiceConfigSnapshot(params)
          if (data) {
            message.success(t('common.update_success'))
            resetState()
            await getSnapshotList()
          }
        } catch (error) {
          console.log('error :>> ', error)
        }
      }
    })
  }

  const handleDelete = (payLoad: ServiceConfigSnapshot) => {
    const params = {
      ...serviceInfo.value,
      snapshotId: payLoad.id
    } as SnapshotRecovery
    Modal.confirm({
      title: t('common.delete_msg'),
      async onOk() {
        try {
          const data = await deleteServiceConfigSnapshot(params)
          if (data) {
            message.success(t('common.delete_success'))
            resetState()
            await getSnapshotList()
          }
        } catch (error) {
          console.log('error :>> ', error)
        }
      }
    })
  }

  const handleOpen = (data: ServiceParams) => {
    serviceInfo.value = data
    open.value = true
    getSnapshotList()
  }

  const getSnapshotList = async () => {
    if (!serviceInfo.value) {
      return
    }
    try {
      loading.value = true
      dataSource.value = await getServiceConfigSnapshotsList(serviceInfo.value)
    } catch (error) {
      console.log('error :>> ', error)
    } finally {
      loading.value = false
    }
  }

  const handleOk = () => {
    handleCancel()
  }

  const handleCancel = () => {
    open.value = false
  }

  defineExpose({
    handleOpen
  })
</script>

<template>
  <div>
    <a-modal
      v-model:open="open"
      width="800px"
      :centered="true"
      :mask="false"
      :title="$t('service.snapshot_management')"
      :mask-closable="false"
      :destroy-on-close="true"
      @ok="handleOk"
      @cancel="handleCancel"
    >
      <a-table :loading="loading" :scroll="{ y: 340 }" :data-source="dataSource" :columns="columns">
        <template #bodyCell="{ record, column }">
          <template v-if="column.key === 'operation'">
            <button-group
              i18n="common"
              :text-compact="true"
              :space="24"
              :groups="operations"
              :args="record"
              group-shape="default"
              group-type="link"
            />
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<style lang="scss" scoped></style>
