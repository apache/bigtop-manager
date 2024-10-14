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
  import { ref, reactive, h, watch, onMounted } from 'vue'
  import { Modal } from 'ant-design-vue'
  import { ExclamationCircleFilled } from '@ant-design/icons-vue'
  import { useI18n } from 'vue-i18n'
  import { useStackStore } from '@/store/stack'
  import SetClusterName from './set-cluster-name.vue'
  import ChooseStack from './choose-stack.vue'
  import SetRepository from './set-repository.vue'
  import SetHosts from './set-hosts.vue'
  import Install from './install.vue'
  import Finish from './finish.vue'
  import { useClusterStore } from '@/store/cluster'

  const open = defineModel<boolean>('open')
  const { t, locale } = useI18n()
  const stackStore = useStackStore()
  const clusterStore = useClusterStore()

  const initItems = () => [
    {
      disabled: true,
      status: 'process',
      title: t('cluster.set_cluster_name'),
      content: h(SetClusterName)
    },
    {
      disabled: true,
      status: 'wait',
      title: t('cluster.choose_stack'),
      content: h(ChooseStack)
    },
    {
      disabled: true,
      status: 'wait',
      title: t('cluster.set_repository'),
      content: h(SetRepository)
    },
    {
      disabled: true,
      status: 'wait',
      title: t('cluster.set_hosts'),
      content: h(SetHosts)
    },
    {
      disabled: true,
      status: 'wait',
      title: t('common.install'),
      content: h(Install)
    },
    {
      disabled: true,
      status: 'wait',
      title: t('common.finish'),
      content: h(Finish)
    }
  ]

  const initClusterInfo = () => {
    const clusterCommand = {
      clusterName: '',
      // 1-Physical Machine 2-Kubernetes
      // Only support physical machine right now
      clusterType: 1,
      stackName: '',
      stackVersion: '',
      fullStackName: '',
      repoInfoList: [],
      hosts: '', // save hostsMetaStr
      hostnames: []
    }

    return {
      command: 'create',
      commandLevel: 'cluster',
      clusterCommand: clusterCommand,
      // Related job id
      jobId: 0,
      // Job Status
      success: false
    }
  }

  const current = ref<number>(0)
  const items = reactive(initItems())
  const clusterInfo = reactive(initClusterInfo())
  const disableButton = ref<boolean>(false)
  const currentItemRef = ref<any>(null)
  const loadingNext = ref<boolean>(false)
  watch(locale, () => {
    Object.assign(items, initItems())
  })

  const next = async () => {
    loadingNext.value = true
    try {
      const valid = await currentItemRef.value?.onNextStep()
      if (valid) {
        console.log('clusterInfo:', JSON.stringify(clusterInfo))
        items[current.value].status = 'finish'
        current.value++
        items[current.value].status = 'process'
      }
    } finally {
      loadingNext.value = false
    }
  }

  const prev = () => {
    items[current.value].status = 'wait'
    current.value--
    items[current.value].status = 'process'
  }

  const clear = () => {
    // Reload clusters
    clusterStore.loadClusters()

    // Clear status
    current.value = 0
    open.value = false
    Object.assign(items, initItems())
    Object.assign(clusterInfo, initClusterInfo())
  }

  const cancel = () => {
    Modal.confirm({
      title: t('common.exit'),
      icon: h(ExclamationCircleFilled),
      content: t('common.exit_confirm'),
      onOk() {
        clear()
      }
    })
  }

  onMounted(async () => {
    await stackStore.initStacks()
  })
</script>

<template>
  <a-modal
    :open="open"
    width="95%"
    centered
    destroy-on-close
    :mask-closable="false"
    :keyboard="false"
    @update:open="cancel"
  >
    <template #footer>
      <a-button
        v-if="current > 0"
        class="footer-btn"
        type="primary"
        :disabled="disableButton"
        @click="prev"
      >
        {{ $t('common.prev') }}
      </a-button>
      <a-button
        v-if="current < items.length - 1"
        class="footer-btn"
        type="primary"
        :loading="loadingNext"
        :disabled="disableButton"
        @click="next"
      >
        {{ $t('common.next') }}
      </a-button>
      <a-button
        v-if="current === items.length - 1"
        class="footer-btn"
        type="primary"
        @click="() => clear()"
      >
        {{ $t('common.done') }}
      </a-button>
    </template>
    <div class="container">
      <a-steps
        v-model:current="current"
        class="step"
        direction="vertical"
        size="small"
        :items="items"
      />
      <div class="content">
        <component
          :is="items[current].content"
          ref="currentItemRef"
          v-model:clusterInfo="clusterInfo"
          v-model:disableButton="disableButton"
        />
      </div>
    </div>
  </a-modal>
</template>

<style scoped lang="less">
  .container {
    .flexbox-mixin(null,null,center,center);

    .step {
      width: 15%;
      height: 35rem;
    }

    .content {
      margin-top: 1rem;
      padding-left: 1rem;
      height: 35rem;
      width: 85%;
      text-align: center;
      border-left: 1px solid #d9d9d9;
    }
  }

  .footer-btn {
    width: 8.333333%;
  }
</style>
