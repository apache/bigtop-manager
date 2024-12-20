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
  import { computed, ref, shallowRef } from 'vue'
  import { useI18n } from 'vue-i18n'

  const { t } = useI18n()
  const activeKey = ref(['1', '2'])
  const autoFormRefMap = shallowRef<Map<string, Comp.AutoFormInstance>>(new Map())
  const formLayout = computed(() => ({
    labelCol: { xs: 5, sm: 5, md: 5, lg: 4, xl: 3 },
    wrapperCol: { xs: 10, sm: 10, md: 10, lg: 10, xl: { xl: 10, pull: 1 } }
  }))
  const baseInfoFormItems = computed((): FormItemState[] => [
    {
      type: 'input',
      field: 'name',
      formItemProps: {
        name: 'name',
        label: t('cluster.name'),
        rules: [{ required: true, message: t('common.enter_error'), trigger: 'blur' }]
      },
      controlProps: {
        placeholder: t('common.enter_error')
      }
    },
    {
      type: 'textarea',
      field: 'desc',
      formItemProps: {
        name: 'desc',
        label: t('cluster.description'),
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
      field: 'rootDirectory',
      formItemProps: {
        name: 'rootDirectory',
        label: t('cluster.root_directory'),
        rules: [{ required: true, message: t('common.enter_error'), trigger: 'blur' }]
      },
      controlProps: {
        placeholder: t('common.enter_error')
      }
    },
    {
      type: 'textarea',
      field: 'userGroup',
      formItemProps: {
        name: 'userGroup',
        label: t('cluster.user_group'),
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

  const collectAutoFormRefs = (el: any, title: string) => {
    if (!autoFormRefMap.value.has(title)) {
      autoFormRefMap.value.set(title, el)
    }
  }

  const check = () => {
    const autoFormRefs = [...autoFormRefMap.value.values()]
    autoFormRefs.forEach(async (formRef, idx) => {
      const res = await formRef.getFormValidation()
      if (!res) {
        !activeKey.value.includes(`${idx + 1}`) && activeKey.value.push(`${idx + 1}`)
      }
    })
  }
</script>

<template>
  <div class="cluster-base">
    <a-collapse v-model:active-key="activeKey" :bordered="false" :ghost="true">
      <a-collapse-panel v-for="(collapse, idx) in collapses" :key="`${idx + 1}`" :header="collapse.title">
        <auto-form
          :key="collapse.title"
          :ref="
            (el) => {
              collectAutoFormRefs(el, collapse.title)
            }
          "
          v-bind="formLayout"
          v-model:form-value="collapse.formValue"
          :form-items="collapse.formItems"
          :show-button="false"
        />
      </a-collapse-panel>
    </a-collapse>
    <a-button @click="check">check</a-button>
  </div>
</template>

<style lang="scss" scoped>
  .cluster-base {
    :deep(.ant-collapse-header) {
      background-color: $color-fill-quaternary;
    }
  }
</style>
