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
  import { CommandRequest } from '@/api/command/types'
  import type { FormItemState } from '@/components/common/auto-form/types'
  import { computed, ref, shallowRef, toRefs } from 'vue'
  import { useI18n } from 'vue-i18n'

  const { t } = useI18n()
  const activeKey = ref(['1', '2'])
  const autoFormRefMap = shallowRef<Map<string, Comp.AutoFormInstance>>(new Map())
  const props = defineProps<{ commandRequest: CommandRequest }>()
  const { commandRequest } = toRefs(props)
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
      field: 'rootDir',
      formItemProps: {
        name: 'rootDir',
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
      formValue: { name: commandRequest.value.clusterCommand?.name, desc: commandRequest.value.clusterCommand?.desc },
      formItems: baseInfoFormItems.value
    },
    {
      title: computed(() => t('cluster.cluster_config')),
      formValue: {
        rootDir: commandRequest.value.clusterCommand?.rootDir,
        userGroup: commandRequest.value.clusterCommand?.rootDir
      },
      formItems: clusterConfigFormItems.value
    }
  ])

  const getFormValues = () => {
    return collapses.value.reduce((pre, val) => {
      Object.assign(pre, val.formValue)
      return pre
    }, {})
  }

  const collectAutoFormRefs = (el: any, title: string) => {
    if (!autoFormRefMap.value.has(title)) {
      autoFormRefMap.value.set(title, el)
    }
  }

  const check = async () => {
    try {
      const autoFormRefs = [...autoFormRefMap.value.values()]
      const validationResults = await Promise.all(
        Array.from(autoFormRefs).map((formRef) => formRef.getFormValidation())
      )
      const allValid = validationResults.every((res) => res)
      if (allValid) {
        const res = getFormValues()
        return res
      } else {
        autoFormRefs.forEach((_formRef, idx) => {
          if (!validationResults[idx]) {
            !activeKey.value.includes(`${idx + 1}`) && activeKey.value.push(`${idx + 1}`)
          }
        })
        return -1
      }
    } catch (error) {
      console.error('Error during form validation:', error)
      return -1
    }
  }

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
