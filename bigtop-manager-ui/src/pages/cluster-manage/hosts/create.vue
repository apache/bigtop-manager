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
  import { computed, h, nextTick, ref, watch } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { FormItemState } from '@/components/common/auto-form/types'
  import { CheckCircleOutlined, UploadOutlined } from '@ant-design/icons-vue'
  import { parseHostNamesAsPatternExpression } from '@/utils/array'
  import { message, Modal } from 'ant-design-vue'
  import { useLocaleStore } from '@/store/locale'
  import { storeToRefs } from 'pinia'
  import { uploadFile } from '@/api/upload-file'
  import { Rule } from 'ant-design-vue/es/form'
  import { updateHost } from '@/api/hosts'
  import { useClusterStore } from '@/store/cluster'
  import type { UploadProps } from 'ant-design-vue'
  import type { HostReq } from '@/api/command/types'
  import type { HostParams, HostVO } from '@/api/hosts/types'

  enum Mode {
    EDIT = 'cluster.edit_host',
    ADD = 'cluster.add_host'
  }

  interface Emits {
    (event: 'onOk', type: keyof typeof Mode, value: HostReq | HostVO): void
  }

  const props = withDefaults(defineProps<{ isPublic?: boolean; apiEditCaller?: boolean }>(), {
    isPublic: false,
    apiEditCaller: false
  })

  const { t } = useI18n()
  const emits = defineEmits<Emits>()
  const localeStore = useLocaleStore()
  const clusterStore = useClusterStore()
  const open = ref(false)
  const loading = ref(false)
  const mode = ref<keyof typeof Mode>('ADD')
  const hiddenItems = ref<string[]>([])
  const autoFormRef = ref<Comp.AutoFormInstance | null>(null)
  const formValue = ref<HostReq & { hostname?: string }>({})
  const fileName = ref('')
  const { locale } = storeToRefs(localeStore)
  const isEdit = computed(() => mode.value === 'EDIT')

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

  const checkSshKeyPassword = async (_rule: Rule, value: string) => {
    if (value != formValue.value?.sshKeyPassword) {
      return Promise.reject(t('host.key_password_not_match'))
    } else {
      return Promise.resolve()
    }
  }

  const formItemsForSshPassword = computed((): FormItemState[] => [
    {
      type: 'inputPassword',
      field: 'sshPassword',
      formItemProps: {
        name: 'sshPassword',
        label: t('host.password_auth'),
        rules: [
          {
            required: true,
            message: t('common.enter_error', [`${t('host.password_auth')}`.toLowerCase()]),
            trigger: 'blur'
          }
        ]
      },
      controlProps: {
        placeholder: t('common.enter_error', [`${t('host.password_auth')}`.toLowerCase()])
      }
    },
    {
      type: 'inputPassword',
      field: 'sshPasswordAgain',
      formItemProps: {
        name: 'sshPasswordAgain',
        label: t('host.confirm_password'),
        rules: [
          {
            required: true,
            validator: checkSshPassword,
            trigger: 'blur'
          }
        ]
      },
      controlProps: {
        placeholder: t('common.enter_error', [`${t('host.confirm_password')}`.toLowerCase()])
      }
    }
  ])

  const formItemsForSshKeyPassword = computed((): FormItemState[] => [
    {
      type: 'radio',
      field: 'inputType',
      defaultValue: '1',
      defaultOptionsMap: [
        { value: '1', label: t('host.file') },
        { value: '2', label: t('host.text') }
      ],
      formItemProps: {
        name: 'inputType',
        label: t('host.input_method'),
        rules: [
          {
            required: true,
            message: t('common.select_error', [`${t('host.input_method')}`.toLowerCase()]),
            trigger: 'blur'
          }
        ]
      }
    },

    {
      type: 'input',
      field: 'sshKeyFilename',
      slot: 'sshKeyFilenameSlot',
      formItemProps: {
        name: 'sshKeyFilename',
        label: t('host.key_file'),
        rules: [
          {
            required: true,
            message: t('common.add_error', [`${t('host.key_file')}`.toLowerCase()]),
            trigger: 'blur'
          }
        ]
      }
    },
    {
      type: 'textarea',
      field: 'sshKeyString',
      formItemProps: {
        name: 'sshKeyString',
        label: t('host.key_text'),
        rules: [
          { required: true, message: t('common.enter_error', [`${t('host.key_text')}`.toLowerCase()]), trigger: 'blur' }
        ]
      },
      controlProps: {
        placeholder: t('common.enter_error', [`${t('host.key_text')}`.toLowerCase()])
      }
    },
    {
      type: 'inputPassword',
      field: 'sshKeyPassword',
      formItemProps: {
        name: 'sshKeyPassword',
        label: t('host.key_password')
      },
      controlProps: {
        placeholder: t('common.enter_error', [`${t('host.key_password')}`.toLowerCase()])
      }
    },
    {
      type: 'inputPassword',
      field: 'sshKeyPasswordAgain',
      formItemProps: {
        name: 'sshKeyPasswordAgain',
        label: t('host.confirm_key_password'),
        rules: [
          {
            required: false,
            validator: checkSshKeyPassword,
            trigger: 'blur'
          }
        ]
      },
      controlProps: {
        placeholder: t('common.enter_error', [`${t('host.confirm_key_password')}`.toLowerCase()])
      }
    }
  ])

  const formItemsOfPublicHost = computed((): FormItemState[] => [
    {
      type: 'select',
      field: 'clusterId',
      defaultValue: '',
      fieldMap: {
        label: 'displayName',
        value: 'clusterId'
      },
      formItemProps: {
        name: 'clusterId',
        label: t('common.cluster'),
        rules: [
          {
            required: true,
            message: t('common.select_error', [`${t('common.cluster')}`.toLowerCase()]),
            trigger: 'blur'
          }
        ]
      },
      controlProps: {
        disabled: isEdit.value,
        placeholder: t('common.select_error', [`${t('common.cluster')}`.toLowerCase()])
      }
    }
  ])

  const formItems = computed((): FormItemState[] => [
    {
      type: 'input',
      field: 'sshUser',
      formItemProps: {
        name: 'sshUser',
        label: t('host.username'),
        rules: [
          { required: true, message: t('common.enter_error', [`${t('host.username')}`.toLowerCase()]), trigger: 'blur' }
        ]
      },
      controlProps: {
        disabled: isEdit.value,
        placeholder: t('common.enter_error', [`${t('host.username')}`.toLowerCase()])
      }
    },
    {
      type: 'radio',
      field: 'authType',
      defaultValue: '1',
      defaultOptionsMap: [
        { value: '1', label: t('host.password_auth') },
        { value: '2', label: t('host.key_auth') },
        { value: '3', label: t('host.no_auth') }
      ],
      formItemProps: {
        name: 'authType',
        label: t('host.auth_method'),
        rules: [
          {
            required: true,
            message: t('common.select_error', [`${t('host.auth_method')}`.toLowerCase()]),
            trigger: 'blur'
          }
        ]
      }
    },
    {
      type: mode.value == 'ADD' ? 'textarea' : 'input',
      field: 'hostname',
      formItemProps: {
        name: 'hostname',
        label: t('host.hostname'),
        rules: [
          { required: true, message: t('common.enter_error', [`${t('host.hostname')}`.toLowerCase()]), trigger: 'blur' }
        ]
      },
      controlProps: {
        disabled: isEdit.value,
        placeholder: t('common.enter_error', [`${t('host.hostname')}`.toLowerCase()])
      }
    },

    {
      type: 'input',
      field: 'agentDir',
      formItemProps: {
        name: 'agentDir',
        label: t('host.agent_path')
      },
      controlProps: {
        disabled: isEdit.value,
        placeholder: t('host.default_agent_path')
      }
    },
    {
      type: 'input',
      field: 'sshPort',
      formItemProps: {
        name: 'sshPort',
        label: t('host.ssh_port')
      },
      controlProps: {
        placeholder: t('common.enter_error', [`${t('host.ssh_port')}`.toLowerCase()])
      }
    },
    {
      type: 'input',
      field: 'grpcPort',
      formItemProps: {
        name: 'grpcPort',
        label: t('host.grpc_port')
      },
      controlProps: {
        placeholder: t('host.default_grpc_port')
      }
    },
    {
      type: 'textarea',
      field: 'desc',
      formItemProps: {
        name: 'desc',
        label: t('host.description')
      },
      controlProps: {
        placeholder: t('common.enter_error', [`${t('host.description')}`.toLowerCase()])
      }
    }
  ])

  const filterFormItems = computed((): FormItemState[] => {
    if (formValue.value.authType === '1') {
      const data = [...formItems.value]
      data.splice(2, 0, ...formItemsForSshPassword.value)
      return props.isPublic ? [...formItemsOfPublicHost.value, ...data] : data
    } else if (formValue.value.authType === '2') {
      const data = [...formItems.value]
      data.splice(2, 0, ...formItemsForSshKeyPassword.value)
      return props.isPublic ? [...formItemsOfPublicHost.value, ...data] : data
    }
    return props.isPublic ? [...formItemsOfPublicHost.value, ...formItems.value] : [...formItems.value]
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

  const handleOpen = async (type: keyof typeof Mode, payload?: HostParams) => {
    open.value = true
    mode.value = type
    if (payload) {
      formValue.value = Object.assign(formValue.value, {
        ...payload,
        authType: `${payload?.authType ?? 1}`,
        inputType: `${payload?.inputType ?? 1}`
      })
    } else {
      Object.assign(formValue.value, {
        authType: '1',
        inputType: '1'
      })
    }

    props.isPublic && (await getClusterSelectOptions())
  }

  const getClusterSelectOptions = async () => {
    await nextTick()
    const formatClusters = clusterStore.clusters.map((v) => ({ ...v, clusterId: v.id }))
    autoFormRef.value?.setOptions('clusterId', formatClusters)
  }

  const editHost = async (hostConfig: HostReq) => {
    try {
      const data = await updateHost(hostConfig)
      if (data) {
        loading.value = false
        message.success(t('common.update_success'))
        emits('onOk', mode.value, formValue.value)
        handleCancel()
      }
    } catch (error) {
      console.log('error :>> ', error)
    } finally {
      loading.value = false
    }
  }

  const handleOk = async () => {
    const validate = await autoFormRef.value?.getFormValidation()
    if (!validate) return
    try {
      if (!isEdit.value) {
        const confirmStatus = await showHosts()
        if (confirmStatus) {
          emits('onOk', mode.value, formValue.value)
          handleCancel()
        }
      } else {
        if (props.apiEditCaller) {
          loading.value = true
          await editHost(formValue.value)
        } else {
          emits('onOk', mode.value, formValue.value)
          handleCancel()
        }
      }
    } catch (e) {
      console.log(e)
    }
  }

  const createHostNameEls = (list: string[]) => {
    const elStyles = {
      contentStyle: {
        marginInlineStart: '2.125rem',
        width: '100%',
        maxHeight: '31.25rem',
        overflow: 'auto'
      },
      itemStyle: { margin: '0.625rem' },
      iconWrpStyle: {
        fontSize: '1.375rem',
        fontWeight: 600,
        color: 'green',
        marginInlineEnd: '0.75rem'
      },
      titleStyle: {
        fontSize: '1rem',
        fontWeight: 600
      },
      redfIconStyle: {
        display: 'flex',
        alignItems: 'center'
      }
    }
    const items = list.map((item: string, idx: number) => {
      return h('li', { key: idx, style: elStyles.itemStyle }, item)
    })
    const iconWrpStyle = h(
      'span',
      {
        style: elStyles.iconWrpStyle
      },
      [h(CheckCircleOutlined)]
    )
    const replaceTitle = h('span', { style: elStyles.titleStyle }, `${t('cluster.show_hosts_resolved')}`)
    const reDefineIcon = h('div', { style: elStyles.redfIconStyle }, [iconWrpStyle, replaceTitle])
    const box = h(
      'div',
      {
        style: elStyles.contentStyle
      },
      items
    )

    return { box, reDefineIcon }
  }

  const showHosts = (): Promise<boolean> => {
    const hostList = parseHostNamesAsPatternExpression(formValue.value.hostname as string)
    const { box: content, reDefineIcon: icon } = createHostNameEls(hostList)
    return new Promise((resolve) => {
      Modal.confirm({
        title: '',
        icon,
        centered: true,
        width: '31.25rem',
        content,
        onOk() {
          formValue.value.hostnames = hostList
          resolve(true)
        },
        onCancel() {
          resolve(false)
          Modal.destroyAll()
        }
      })
    })
  }

  const handleCancel = () => {
    formValue.value = {
      authType: '1',
      inputType: '1'
    }
    fileName.value = ''
    open.value = false
  }

  const beforeUpload: UploadProps['beforeUpload'] = (file) => {
    // const isText = file.type === 'text/plain'
    const checkLimitSize = file.size / 1024 <= 10
    // if (!isText) {
    //   message.error(t('common.file_type_error'))
    //   return false
    // }
    if (!checkLimitSize) {
      message.error(t('common.file_size_error'))
      return false
    }
    return true
  }

  const customRequest = async (options: { file: any; onSuccess: any; onError: any }) => {
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

  defineExpose({
    handleOpen
  })
</script>

<template>
  <div class="add-host">
    <a-modal
      :open="open"
      :width="600"
      :title="Mode[mode] && $t(Mode[mode])"
      :mask-closable="false"
      :confirm-loading="loading"
      :centered="true"
      :destroy-on-close="true"
      @ok="handleOk"
      @cancel="handleCancel"
    >
      <auto-form
        ref="autoFormRef"
        v-model:form-value="formValue"
        :label-col="{
          span: locale === 'zh_CN' ? 5 : 7
        }"
        :hidden-items="hiddenItems"
        :show-button="false"
        :form-items="filterFormItems"
      >
        <template #sshKeyFilenameSlot="{ item }">
          <a-form-item v-bind="item.formItemProps">
            <a-upload
              accept="text/plain"
              :before-upload="beforeUpload"
              :custom-request="customRequest"
              :show-upload-list="false"
            >
              <a-button>
                <upload-outlined></upload-outlined>
                {{ $t('common.upload_file') }}
              </a-button>
            </a-upload>
            <span class="filename">{{ fileName ? fileName : mode === 'EDIT' ? formValue[item.field] : '' }}</span>
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
  .filename {
    color: $color-primary;
    padding-inline: $space-sm;
  }
  footer {
    width: 100%;
    @include flexbox($justify: flex-end);
  }
</style>
