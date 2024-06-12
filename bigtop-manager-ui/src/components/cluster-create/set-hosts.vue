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
  import { ref, h } from 'vue'
  import { execCommand } from '@/api/command'
  import { parseHostNamesAsPatternExpression } from '@/utils/array'
  import type { FormInstance } from 'ant-design-vue'
  import { Modal } from 'ant-design-vue'
  import { useI18n } from 'vue-i18n'
  import { CheckCircleOutlined } from '@ant-design/icons-vue'

  const { t } = useI18n()
  const clusterInfo = defineModel<any>('clusterInfo')
  const formRef = ref<FormInstance>()
  const { clusterCommand } = clusterInfo.value

  const onNextStep = async () => {
    // clusterInfo.value.clusterCommand.hostnames = hosts.value
    //   .split('\n')
    //   .filter((item) => item !== '')
    try {
      const checkValidate = await formRef.value?.validate()
      if (checkValidate) {
        const confirmStatus = await showHosts()
        if (confirmStatus) {
          const res = await execCommand(clusterInfo.value)
          clusterInfo.value.jobId = res.id
          return Promise.resolve(true)
        }
      } else {
        return Promise.resolve(false)
      }
    } catch (e) {
      console.log(e)
      return Promise.resolve(false)
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

    const replaceTitle = h(
      'span',
      { style: elStyles.titleStyle },
      `${t('cluster.show_hosts_resolved')}`
    )

    const reDefineIcon = h('div', { style: elStyles.redfIconStyle }, [
      iconWrpStyle,
      replaceTitle
    ])

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
    const hostList = parseHostNamesAsPatternExpression(clusterCommand.hosts)
    const { box: content, reDefineIcon: icon } = createHostNameEls(hostList)
    return new Promise((resolve) => {
      Modal.confirm({
        title: '',
        icon,
        centered: true,
        width: '31.25rem',
        content,
        onOk() {
          clusterCommand.hostnames = hostList
          resolve(true)
        },
        onCancel() {
          resolve(false)
          Modal.destroyAll()
        }
      })
    })
  }

  defineExpose({
    onNextStep
  })
</script>

<template>
  <div class="container">
    <div class="title">{{ $t('cluster.set_hosts') }}</div>
    <a-form ref="formRef" :style="{ width: '100%' }" :model="clusterCommand">
      <a-form-item
        name="hosts"
        :rules="[
          {
            required: true,
            message: `${$t('cluster.set_hosts_valid')}`
          }
        ]"
      >
        <a-textarea
          v-model:value="clusterCommand.hosts"
          :rows="18"
          :placeholder="$t('cluster.set_hosts_placeholder')"
        />
      </a-form-item>
    </a-form>
  </div>
</template>
<style scoped lang="scss">
  .container {
    display: flex;
    flex-direction: column;
    justify-content: start;
    align-items: center;
    height: 100%;

    .title {
      font-size: 1.5rem;
      line-height: 2rem;
      margin-bottom: 1rem;
    }
  }
</style>
