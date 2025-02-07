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
  import { computed, ref, watch } from 'vue'
  import type { AuthorizedPlatform } from '@/api/llm-config/types'
  // import { message } from 'ant-design-vue'
  // import { useI18n } from 'vue-i18n'
  import type { HostAdd } from '@/api/hosts/types'
  import { FormItemState } from '@/components/common/auto-form/types'
  import { UploadOutlined } from '@ant-design/icons-vue'
  enum Mode {
    EDIT = 'cluster.edit_host',
    ADD = 'cluster.add_host'
  }

  // interface Emits {
  //   (event: 'onOk'): void
  // }

  // const emits = defineEmits<Emits>()
  // const { t } = useI18n()
  const open = ref(false)
  const mode = ref<keyof typeof Mode>('ADD')
  const hiddenItems = ref<string[]>([])
  const autoFormRef = ref<Comp.AutoFormInstance | null>(null)
  const formValue = ref<HostAdd>({})
  const fileList = ref()
  // const isEdit = computed(() => mode.value === 'EDIT')

  const formItemsForPassword = computed((): FormItemState[] => [
    {
      type: 'input',
      field: 'sshKeyPassword',
      formItemProps: {
        name: 'sshKeyPassword',
        label: '密码',
        rules: [{ required: true, message: '密码', trigger: 'blur' }]
      },
      controlProps: {
        placeholder: '请输入'
      }
    },
    {
      type: 'input',
      field: 'sshKeyPasswordAgain',
      formItemProps: {
        name: 'sshKeyPasswordAgain',
        label: '确认密码',
        rules: [{ required: true, message: '确认密码', trigger: 'blur' }]
      },
      controlProps: {
        placeholder: '请输入'
      }
    }
  ])

  const formItemsForKey = computed((): FormItemState[] => [
    {
      type: 'radio',
      field: 'inputType',
      defaultValue: '1',
      defaultOptionsMap: [
        { value: '1', label: '文件' },
        { value: '2', label: '文本' }
      ],
      formItemProps: {
        name: 'inputType',
        label: '输入方式',
        rules: [{ required: true, message: '输入方式', trigger: 'blur' }]
      }
    },

    {
      type: 'input',
      field: 'sshKeyFilename',
      slot: 'sshKeyFilenameSlot',
      formItemProps: {
        name: 'sshKeyFilename',
        label: '密钥文件'
      }
    },
    {
      type: 'textarea',
      field: 'sshKeyString',
      formItemProps: {
        name: 'sshKeyString',
        label: '密钥文本',
        rules: [{ required: true, message: '密钥文本', trigger: 'blur' }]
      },
      controlProps: {
        placeholder: '请输入'
      }
    },
    {
      type: 'input',
      field: 'keyPassword',
      formItemProps: {
        name: 'keyPassword',
        label: '密钥口令'
      },
      controlProps: {
        placeholder: '请输入'
      }
    },
    {
      type: 'textarea',
      field: 'keyPasswordAgain',
      formItemProps: {
        name: 'keyPasswordAgain',
        label: '确认口令'
      },
      controlProps: {
        placeholder: '请输入'
      }
    }
  ])
  const formItems = computed((): FormItemState[] => [
    {
      type: 'input',
      field: 'sshUser',
      formItemProps: {
        name: 'sshUser',
        label: '用户名',
        rules: [{ required: true, message: '用户名', trigger: 'blur' }]
      },
      controlProps: {
        placeholder: '请输入'
      }
    },
    {
      type: 'radio',
      field: 'authType',
      defaultValue: '1',
      defaultOptionsMap: [
        { value: '1', label: '密码' },
        { value: '2', label: '密钥' },
        { value: '3', label: '无认证' }
      ],
      formItemProps: {
        name: 'authType',
        label: '认证方式',
        rules: [{ required: true, message: '认证方式', trigger: 'blur' }]
      }
    },

    {
      type: 'textarea',
      field: 'hostnames',
      formItemProps: {
        name: 'hostnames',
        label: '主机名',
        rules: [{ required: true, message: '主机名', trigger: 'blur' }]
      },
      controlProps: {
        placeholder: '请输入'
      }
    },

    {
      type: 'input',
      field: 'agentDir',
      slot: 'agentDirSlot',
      formItemProps: {
        name: 'agentDir',
        label: 'Agent路径'
      },
      controlProps: {
        placeholder: '请输入'
      }
    },
    {
      type: 'input',
      field: 'sshPort',
      formItemProps: {
        name: 'sshPort',
        label: 'SSH端口'
      },
      controlProps: {
        placeholder: '请输入'
      }
    },
    {
      type: 'input',
      field: 'grpcPort',
      slot: 'grpcPortSlot',
      formItemProps: {
        name: 'grpcPort',
        label: 'gRPC端口'
      },
      controlProps: {
        placeholder: '请输入'
      }
    },
    {
      type: 'textarea',
      field: 'remark',
      formItemProps: {
        name: 'remark',
        label: '备注'
      },
      controlProps: {
        placeholder: '请输入'
      }
    }
  ])

  const filterFormItems = computed((): FormItemState[] => {
    if (formValue.value.authType === '1') {
      const data = [...formItems.value]
      data.splice(2, 0, ...formItemsForPassword.value)
      return data
    } else if (formValue.value.authType === '2') {
      const data = [...formItems.value]
      data.splice(2, 0, ...formItemsForKey.value)
      return data
    }
    return [...formItems.value]
  })

  watch(
    () => formValue.value,
    (val) => {
      if (val.inputType && val.inputType === '1') {
        hiddenItems.value = ['sshKeyString']
      } else if (val.inputType && val.inputType === '2') {
        hiddenItems.value = ['sshKeyFilename']
      } else {
        hiddenItems.value = []
      }
    },
    {
      deep: true
    }
  )

  const handleOpen = async (payload?: AuthorizedPlatform) => {
    open.value = true
    mode.value = payload ? 'EDIT' : 'ADD'
    Object.assign(formValue.value, {
      authType: '1',
      inputType: '1'
    })
  }

  const handleOk = async () => {
    const validate = await autoFormRef.value?.getFormValidation()
    if (!validate) return
    console.log('object :>> ', formValue.value)
    // const res = await addHost()
    // const api = isEdit.value ? llmConfigStore.updateAuthPlatform : llmConfigStore.addAuthorizedPlatform
    // const success = await api()
    // if (success) {
    //   const text = isEdit.value ? 'common.update_success' : 'common.created'
    //   message.success(t(text))
    //   handleCancel()
    //   emits('onOk')
    // }
  }

  const handleCancel = () => {
    autoFormRef.value?.resetForm()
    open.value = false
  }

  defineExpose({
    handleOpen
  })
</script>

<template>
  <div class="add-host">
    <a-modal
      :open="open"
      :width="500"
      :title="Mode[mode] && $t(Mode[mode])"
      :mask-closable="false"
      :centered="true"
      :destroy-on-close="true"
      @ok="handleOk"
      @cancel="handleCancel"
    >
      <auto-form
        ref="autoFormRef"
        v-model:form-value="formValue"
        :hidden-items="hiddenItems"
        :show-button="false"
        :form-items="filterFormItems"
      >
        <template #sshKeyFilenameSlot="{ item }">
          <a-form-item v-bind="item.formItemProps">
            <a-upload v-model:file-list="fileList" action="https://www.mocky.io/v2/5cc8019d300000980a055e76">
              <a-button>
                <upload-outlined></upload-outlined>
                上传文件
              </a-button>
            </a-upload>
          </a-form-item>
        </template>
        <template #agentDirSlot="{ item, state }">
          <a-form-item>
            <template #label>
              <div class="question">
                <span>
                  {{ item.formItemProps?.label }}
                </span>
                <svg-icon style="padding: 1px 0 0 0" name="question" />
              </div>
            </template>
            <a-input v-bind="item.controlProps" v-model:value="state[item.field]" />
          </a-form-item>
        </template>
        <template #grpcPortSlot="{ item, state }">
          <a-form-item>
            <template #label>
              <div class="question">
                <span>
                  {{ item.formItemProps?.label }}
                </span>
                <svg-icon style="padding: 1px 0 0 0" name="question" />
              </div>
            </template>
            <a-input v-bind="item.controlProps" v-model:value="state[item.field]" />
          </a-form-item>
        </template>
      </auto-form>
      <template #footer>
        <footer>
          <a-space size="middle">
            <a-button @click="handleCancel">
              {{ $t('common.cancel') }}
            </a-button>
            <a-button type="primary" @click="handleOk">
              {{ $t('common.confirm') }}
            </a-button>
          </a-space>
        </footer>
      </template>
    </a-modal>
  </div>
</template>

<style lang="scss" scoped>
  .question {
    cursor: pointer;
  }
  footer {
    width: 100%;
    display: flex;
    justify-content: flex-end;
  }
</style>
