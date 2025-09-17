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
  import { Rule } from 'ant-design-vue/es/form'
  import { message } from 'ant-design-vue'
  import { UploadOutlined } from '@ant-design/icons-vue'
  import { useClusterStore } from '@/store/cluster'
  import { uploadFile } from '@/api/upload-file'
  import { updateHost } from '@/api/host'

  import ParsedPreview from './components/parsed-preview.vue'

  import type { UploadProps } from 'ant-design-vue'
  import type { HostReq } from '@/api/command/types'
  import type { HostParams, HostVO } from '@/api/host/types'
  import type { FormItem } from '@/components/common/form-builder/types'

  type ModeType = 'EDIT' | 'ADD'

  interface Emits {
    (
      event: 'onOk',
      type: ModeType,
      value: HostReq | HostVO,
      duplicateHosts?: HostReq & { strategy: 'override' | 'keep' }[]
    ): void
  }

  interface Props {
    isPublic?: boolean
    apiEditCaller?: boolean
    currentHosts: HostReq[]
  }

  const props = withDefaults(defineProps<Props>(), { isPublic: false, apiEditCaller: false })
  const emits = defineEmits<Emits>()

  const { t } = useI18n()
  const clusterStore = useClusterStore()

  const open = ref(false)
  const loading = ref(false)
  const fileName = ref('')
  const mode = ref<ModeType>('ADD')
  const formRef = ref<Comp.FormBuilderInstance | null>(null)
  const formValue = ref<HostReq & { hostname?: string }>({})
  const previewRef = ref<InstanceType<typeof ParsedPreview> | null>()

  const titleMap = shallowRef<Record<ModeType, string>>({
    ADD: 'cluster.add_host',
    EDIT: 'cluster.edit_host'
  })

  const isEdit = computed(() => mode.value === 'EDIT')

  /**
   * Validates SSH password confirmation.
   */
  const checkSshPassword = async (_rule: Rule, value: string) => {
    if (!value) {
      return Promise.reject(t('common.enter_error', [`${t('host.confirm_password')}`.toLowerCase()]))
    }
    if (value != formValue.value?.sshPassword) {
      return Promise.reject(t('common.password_not_match'))
    } else {
      return Promise.resolve()
    }
  }

  /**
   * Validates SSH key password confirmation.
   */
  const checkSshKeyPassword = async (_rule: Rule, value: string) => {
    if (value != formValue.value?.sshKeyPassword) {
      return Promise.reject(t('host.key_password_not_match'))
    } else {
      return Promise.resolve()
    }
  }

  const rules = computed(() => ({
    sshPasswordAgain: [
      {
        required: true,
        validator: checkSshPassword,
        trigger: 'blur'
      }
    ],
    sshKeyFilename: [
      {
        required: true,
        message: t('common.add_error', [`${t('host.key_file')}`.toLowerCase()]),
        trigger: 'blur'
      }
    ],
    sshKeyPasswordAgain: [
      {
        required: true,
        validator: checkSshKeyPassword,
        trigger: 'blur'
      }
    ]
  }))

  const formItemsForSshPassword = computed((): FormItem[] => [
    {
      type: 'password',
      field: 'sshPassword',
      label: t('host.password_auth'),
      required: true
    },
    {
      type: 'password',
      field: 'sshPasswordAgain',
      label: t('host.confirm_password'),
      required: true
    }
  ])

  const formItemsForSshKeyPassword = computed((): FormItem[] => [
    {
      type: 'radioGroup',
      field: 'inputType',
      label: t('host.input_method'),
      required: true,
      props: {
        options: [
          { value: '1', label: t('host.file') },
          { value: '2', label: t('host.text') }
        ]
      }
    },
    {
      type: 'input',
      field: 'sshKeyFilename',
      hidden: formValue.value.inputType === '2',
      label: t('host.key_file'),
      required: true
    },
    {
      type: 'textarea',
      field: 'sshKeyString',
      hidden: formValue.value.inputType === '1',
      label: t('host.key_text'),
      required: true
    },
    {
      type: 'password',
      field: 'sshKeyPassword',
      label: t('host.key_password'),
      required: true
    },
    {
      type: 'password',
      field: 'sshKeyPasswordAgain',
      label: t('host.confirm_key_password'),
      required: true
    }
  ])

  const formItemsOfPublicHost = computed((): FormItem[] => [
    {
      type: 'select',
      field: 'clusterId',
      label: t('common.cluster'),
      required: true,
      props: {
        options: Object.values(clusterStore.clusterMap).map((v) => ({ value: v.id, label: v.displayName })),
        disabled: isEdit.value
      }
    }
  ])

  const formItems = computed((): FormItem[] => [
    {
      type: 'input',
      field: 'sshUser',
      label: t('host.username'),
      required: true,
      props: {
        disabled: isEdit.value
      }
    },
    {
      type: 'radioGroup',
      field: 'authType',
      label: t('host.auth_method'),
      required: true,
      props: {
        options: [
          { value: '1', label: t('host.password_auth') },
          { value: '2', label: t('host.key_auth') },
          { value: '3', label: t('host.no_auth') }
        ]
      }
    },
    {
      type: mode.value == 'ADD' ? 'textarea' : 'input',
      field: 'hostname',
      required: true,
      label: t('host.hostname'),
      props: {
        disabled: isEdit.value
      }
    },

    {
      type: 'input',
      field: 'agentDir',
      label: t('host.agent_path'),
      placeholder: t('host.default_agent_path'),
      props: {
        disabled: isEdit.value
      }
    },
    {
      type: 'input',
      field: 'sshPort',
      label: t('host.ssh_port'),
      placeholder: t('host.default_ssh_port')
    },
    {
      type: 'input',
      field: 'grpcPort',
      name: 'grpcPort',
      label: t('host.grpc_port'),
      placeholder: t('host.default_grpc_port')
    },
    {
      type: 'textarea',
      field: 'desc',
      label: t('host.description')
    }
  ])

  const filterFormItems = computed((): FormItem[] => {
    const baseItems = [...formItems.value]

    if (formValue.value.authType === '1') {
      baseItems.splice(2, 0, ...formItemsForSshPassword.value)
    }

    if (formValue.value.authType === '2') {
      baseItems.splice(2, 0, ...formItemsForSshKeyPassword.value)
    }

    return props.isPublic ? [...formItemsOfPublicHost.value, ...baseItems] : baseItems
  })

  const editHost = async (hostConfig: HostReq) => {
    try {
      const data = await updateHost(hostConfig)
      if (data) {
        message.success(t('common.update_success'))
        emits('onOk', mode.value, formValue.value)
        handleCancel()
      }
    } catch (error) {
      console.error('Error editing host:', error)
    } finally {
      loading.value = false
    }
  }

  /**
   * Handles parsed data from preview.
   */
  const handleParsed = ({ parsedData, confirmStatus, duplicateHosts }: any) => {
    if (confirmStatus) {
      Object.assign(formValue.value, parsedData)
      emits('onOk', mode.value, formValue.value, duplicateHosts)
      handleCancel()
    }
  }

  const handleOk = async () => {
    try {
      const validate = await formRef.value?.validate()
      if (!validate) return

      if (!isEdit.value) {
        previewRef.value?.parsed(props.currentHosts)
      } else if (props.apiEditCaller) {
        loading.value = true
        await editHost(formValue.value)
      } else {
        emits('onOk', mode.value, formValue.value)
        handleCancel()
      }
    } catch (error) {
      console.log('error', error)
    }
  }

  const handleCancel = () => {
    formValue.value = { authType: '1', inputType: '1' }
    fileName.value = ''
    open.value = false
  }

  const beforeUpload: UploadProps['beforeUpload'] = (file) => {
    if (file.size / 1024 > 10) {
      message.error(t('common.file_size_error'))
      return false
    }
    return true
  }

  const customRequest = async (options: any) => {
    const { file, onSuccess, onError } = options
    try {
      const formData = new FormData()
      formData.append('file', file)
      const data = await uploadFile(formData)
      formValue.value!.sshKeyFilename = data
      fileName.value = file.name
      onSuccess(data, file)
      message.success(t('common.upload_success'))
    } catch (error) {
      onError(error)
      message.error(t('common.upload_failed'))
      fileName.value = ''
    }
  }

  const handleOpen = async (type: ModeType, payload?: HostParams) => {
    open.value = true
    mode.value = type

    formValue.value = payload
      ? { ...payload, authType: `${payload?.authType ?? 1}`, inputType: `${payload?.inputType ?? 1}` }
      : { authType: '1', inputType: '1' }
  }

  defineExpose({
    handleOpen
  })
</script>

<template>
  <div class="add-host">
    <a-modal
      :open="open"
      :width="600"
      :title="titleMap[mode] && t(titleMap[mode])"
      :mask-closable="false"
      :confirm-loading="loading"
      :centered="true"
      :destroy-on-close="true"
      :ok-text="t('common.confirm')"
      @cancel="handleCancel"
      @ok="handleOk"
    >
      <div class="add-host-content">
        <form-builder ref="formRef" v-model="formValue" :form-items="filterFormItems" :rules="rules">
          <template #sshKeyFilename="{ item }">
            <a-upload
              accept="text/plain"
              :before-upload="beforeUpload"
              :custom-request="customRequest"
              :show-upload-list="false"
            >
              <a-button>
                <upload-outlined></upload-outlined>
                {{ t('common.upload_file') }}
              </a-button>
            </a-upload>
            <span class="filename">{{ fileName ? fileName : mode === 'EDIT' ? formValue[item.field] : '' }}</span>
          </template>
        </form-builder>
      </div>
    </a-modal>
    <parsed-preview ref="previewRef" :is-public="$props.isPublic" :data="formValue" @parsed="handleParsed" />
  </div>
</template>

<style lang="scss" scoped>
  .filename {
    color: $color-primary;
    padding-inline: $space-sm;
  }
</style>
