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
  import type { FormItemState } from '@/components/common/auto-form/types'
  import { computed, ref } from 'vue'
  import { useI18n } from 'vue-i18n'

  const { t } = useI18n()
  const activeKey = ref(['1'])
  const formLayout = computed(() => ({
    labelCol: { span: 4 },
    wrapperCol: { span: 6 }
  }))
  const baseInfoFormItems = computed((): FormItemState[] => [
    {
      type: 'input',
      field: 'name',
      formItemProps: {
        name: 'name',
        label: '集群名',
        rules: [{ required: true, message: t('common.enter_error'), trigger: 'blur' }]
      },
      controlProps: {
        placeholder: t('common.enter_error')
      }
    },
    {
      type: 'textarea',
      field: 'remark',
      formItemProps: {
        name: 'remark',
        label: '集群描述',
        rules: [{ required: true, message: t('common.enter_error'), trigger: 'blur' }]
      },
      controlProps: {
        placeholder: t('common.enter_error')
      }
    }
  ])
  const clusterConfigFormItems = computed((): FormItemState[] => [
    {
      type: 'input',
      field: 'name',
      formItemProps: {
        name: 'name',
        label: '集群根目录',
        rules: [{ required: true, message: t('common.enter_error'), trigger: 'blur' }]
      },
      controlProps: {
        placeholder: t('common.enter_error')
      }
    },
    {
      type: 'textarea',
      field: 'remark',
      formItemProps: {
        name: 'remark',
        label: '集群用户组',
        rules: [{ required: true, message: t('common.enter_error'), trigger: 'blur' }]
      },
      controlProps: {
        placeholder: t('common.enter_error')
      }
    }
  ])
  const collapses = ref([
    {
      title: computed(() => t('cluster.base_info')),
      formValue: {},
      formItems: baseInfoFormItems.value
    },
    {
      title: computed(() => t('cluster.cluster_config')),
      formValue: {},
      formItems: clusterConfigFormItems.value
    }
  ])
</script>

<template>
  <div class="cluster-base">
    <a-collapse v-model:active-key="activeKey" :bordered="false" :ghost="true">
      <a-collapse-panel v-for="(collapse, idx) in collapses" :key="`${idx + 1}`" :header="collapse.title">
        <auto-form
          v-bind="formLayout"
          v-model:form-value="collapse.formValue"
          :form-items="collapse.formItems"
          :show-button="false"
        />
      </a-collapse-panel>
    </a-collapse>
  </div>
</template>

<style lang="scss" scoped>
  .cluster-base {
    :deep(.ant-collapse-header) {
      background-color: $color-fill-quaternary;
    }
  }
</style>
