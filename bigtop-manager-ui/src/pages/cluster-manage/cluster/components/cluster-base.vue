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
  import { computed, ref, shallowRef, watch } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { pick } from '@/utils/tools'
  import type { ClusterCommandReq } from '@/api/command/types'
  import type { FormItemState } from '@/components/common/auto-form/types'

  type ClusterCommandReqKey = keyof ClusterCommandReq

  const { t } = useI18n()
  const activeKey = ref(['1', '2'])
  const autoFormRefMap = shallowRef<Map<string, Comp.AutoFormInstance>>(new Map())
  const props = defineProps<{ stepData: ClusterCommandReq }>()
  const emits = defineEmits(['updateData'])
  const collapses = ref([
    {
      title: computed(() => t('cluster.base_info')),
      formValue: pick<ClusterCommandReq, ClusterCommandReqKey>(props.stepData, ['name', 'displayName', 'desc']),
      formItems: computed(() => baseInfoFormItems.value)
    },
    {
      title: computed(() => t('cluster.cluster_config')),
      formValue: pick<ClusterCommandReq, ClusterCommandReqKey>(props.stepData, ['rootDir', 'userGroup']),
      formItems: computed(() => clusterConfigFormItems.value)
    }
  ])
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
        rules: [
          { required: true, message: t('common.enter_error', [`${t('cluster.name')}`.toLowerCase()]), trigger: 'blur' }
        ]
      },
      controlProps: {
        placeholder: t('common.enter_error', [`${t('cluster.name')}`.toLowerCase()])
      }
    },
    {
      type: 'input',
      field: 'displayName',
      formItemProps: {
        name: 'displayName',
        label: t('cluster.display_name'),
        rules: [
          {
            required: true,
            message: t('common.enter_error', [`${t('cluster.display_name')}`.toLowerCase()]),
            trigger: 'blur'
          }
        ]
      },
      controlProps: {
        placeholder: t('common.enter_error', [`${t('cluster.display_name')}`.toLowerCase()])
      }
    },
    {
      type: 'textarea',
      field: 'desc',
      formItemProps: {
        name: 'desc',
        label: t('cluster.description'),
        rules: [
          {
            required: true,
            message: t('common.enter_error', [`${t('cluster.description')}`.toLowerCase()]),
            trigger: 'blur'
          }
        ]
      },
      controlProps: {
        placeholder: t('common.enter_error', [`${t('cluster.description')}`.toLowerCase()])
      }
    }
  ])

  const clusterConfigFormItems = computed((): FormItemState[] => [
    {
      type: 'input',
      field: 'rootDir',
      formItemProps: {
        name: 'rootDir',
        label: t('cluster.root_directory'),
        rules: [
          {
            required: true,
            message: t('common.enter_error', [`${t('cluster.root_directory')}`.toLowerCase()]),
            trigger: 'blur'
          }
        ]
      },
      controlProps: {
        placeholder: t('common.enter_error', [`${t('cluster.root_directory')}`.toLowerCase()])
      }
    },
    {
      type: 'input',
      field: 'userGroup',
      formItemProps: {
        name: 'userGroup',
        label: t('cluster.user_group'),
        rules: [
          {
            required: true,
            message: t('common.enter_error', [`${t('cluster.user_group')}`.toLowerCase()]),
            trigger: 'blur'
          }
        ]
      },
      controlProps: {
        placeholder: t('common.enter_error', [`${t('cluster.user_group')}`.toLowerCase()])
      }
    }
  ])

  const getFormValues = () => {
    return collapses.value.reduce((pre, val) => {
      Object.assign(pre, val.formValue)
      return pre
    }, {})
  }

  const collectAutoFormRefs = (el: any, title: string) => {
    // Synchronous I18n switch
    el ? autoFormRefMap.value.set(title, el) : autoFormRefMap.value.delete(title)
  }

  const check = async () => {
    try {
      const autoFormRefs = [...autoFormRefMap.value.values()]
      const validation = await Promise.all(autoFormRefs.map((formRef) => formRef?.getFormValidation()))
      const allValid = validation.every((res) => res)
      autoFormRefs.forEach((_formRef, index) => {
        !validation[index] && !activeKey.value.includes(`${index + 1}`) && activeKey.value.push(`${index + 1}`)
      })
      return allValid
    } catch (error) {
      console.error('Error during form validation:', error)
      return
    }
  }

  watch(
    () => [collapses.value[0].formValue, collapses.value[1].formValue],
    () => {
      emits('updateData', getFormValues())
    },
    {
      deep: true
    }
  )

  defineExpose({
    check
  })
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
  </div>
</template>

<style lang="scss" scoped>
  .cluster-base {
    :deep(.ant-collapse-header) {
      background-color: $color-fill-quaternary;
    }
  }
</style>
