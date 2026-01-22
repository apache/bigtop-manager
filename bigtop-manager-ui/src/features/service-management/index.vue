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
  import { message } from 'ant-design-vue'
  import { useServiceStore } from '@/store/service'
  import { useJobProgress } from '@/store/job-progress'
  import { Command, type CommandRequest } from '@/api/command/types'

  import Overview from './overview.vue'
  import Components from './components.vue'
  import Configs from './configs.vue'

  import type { TabItem } from '@/components/base/main-card/types'
  import type { GroupItem } from '@/components/common/button-group/types'
  import type { ServiceVO } from '@/api/service/types'

  import { enableHdfsHa, enableYarnRmHa, type EnableHdfsHaReq, type EnableYarnRmHaReq } from '@/api/service'
  import { getHosts } from '@/api/host'
  import type { HostVO } from '@/api/host/types'

  interface RouteParams {
    id: number
    serviceId: number
  }

  type Key = keyof typeof Command | 'Remove' | 'EnableHdfsHa' | 'EnableYarnHa'

  const { t } = useI18n()
  const route = useRoute()
  const router = useRouter()
  const serviceStore = useServiceStore()
  const jobProgressStore = useJobProgress()
  const { activeTab } = useTabState(route.path, '1')
  const { loading, serviceMap } = storeToRefs(serviceStore)

  const serviceDetail = shallowRef<ServiceVO>()
  const stepPages = shallowRef([Overview, Components, Configs])

  const getCompName = computed(() => stepPages.value[parseInt(activeTab.value) - 1])

  const componentPayload = computed(() => {
    const { id, serviceId } = route.params as unknown as RouteParams
    return [id, serviceId] as [number, number]
  })

  const tabs = computed((): TabItem[] => [
    { key: '1', title: t('common.overview') },
    { key: '2', title: t('common.component') },
    { key: '3', title: t('common.configs') }
  ])

  const isHadoopService = computed(() => (serviceDetail.value?.name ?? '').toLowerCase() === 'hadoop')

  const actionGroup = computed<GroupItem[]>(() => {
    const baseMenu: any[] = [
      { action: 'Start', text: t('common.start', [t('common.service')]) },
      { action: 'Restart', text: t('common.restart', [t('common.service')]) },
      { action: 'Stop', text: t('common.stop', [t('common.service')]) }
    ]

    if (isHadoopService.value) {
      baseMenu.push({ action: 'EnableHdfsHa', text: '启用 HDFS HA' }, { action: 'EnableYarnHa', text: '启用 YARN HA' })
    }

    baseMenu.push({ action: 'Remove', text: t('common.remove', [t('common.service')]), divider: true, danger: true })

    return [
      {
        shape: 'default',
        type: 'primary',
        text: t('common.operation'),
        dropdownMenu: baseMenu,
        dropdownMenuClickEvent: (info) => dropdownMenuClick!(info)
      }
    ]
  })

  const onServiceDeleted = (clusterId: number) => {
    router.replace({ path: `/cluster-manage/clusters/${clusterId}` })
  }

  const hdfsHaModalOpen = ref(false)
  const yarnHaModalOpen = ref(false)

  const hdfsHaForm = reactive<EnableHdfsHaReq>({
    activeNameNodeHost: '',
    standbyNameNodeHost: '',
    journalNodeHosts: [],
    zookeeperHosts: [],
    zkfcHosts: [],
    nameservice: 'nameservice1'
  })

  const yarnHaForm = reactive<EnableYarnRmHaReq>({
    activeResourceManagerHost: '',
    standbyResourceManagerHost: '',
    rmIds: ['rm1', 'rm2'],
    yarnClusterId: 'yarn-cluster',
    zookeeperHosts: []
  })

  const clusterHosts = ref<HostVO[]>([])

  const loadClusterHosts = async () => {
    const [clusterId] = componentPayload.value
    const res = await getHosts({ clusterId, pageNum: 1, pageSize: 1000 })
    clusterHosts.value = res.content ?? []
  }

  const allHostOptions = computed(() => clusterHosts.value.map((h) => ({ value: h.hostname })).filter((x) => !!x.value))

  const openHdfsHaModal = async () => {
    await loadClusterHosts()

    const allHosts = clusterHosts.value.map((h) => h.hostname).filter(Boolean) as string[]

    hdfsHaForm.activeNameNodeHost = allHosts[0] ?? ''
    hdfsHaForm.standbyNameNodeHost = allHosts[1] ?? ''
    hdfsHaForm.journalNodeHosts = allHosts.slice(0, 3)
    hdfsHaForm.zkfcHosts = [hdfsHaForm.activeNameNodeHost, hdfsHaForm.standbyNameNodeHost].filter(Boolean)
    hdfsHaForm.zookeeperHosts = allHosts.slice(0, 3)

    hdfsHaModalOpen.value = true
  }

  const openYarnHaModal = async () => {
    await loadClusterHosts()

    const allHosts = clusterHosts.value.map((h) => h.hostname).filter(Boolean) as string[]

    yarnHaForm.activeResourceManagerHost = allHosts[0] ?? ''
    yarnHaForm.standbyResourceManagerHost = allHosts[1] ?? ''
    yarnHaForm.zookeeperHosts = allHosts.slice(0, 3)

    yarnHaModalOpen.value = true
  }

  const submitHdfsHa = async () => {
    if (!hdfsHaForm.activeNameNodeHost || !hdfsHaForm.standbyNameNodeHost) {
      message.warning('Active/Standby NameNode 不能为空')
      return
    }
    if (hdfsHaForm.activeNameNodeHost === hdfsHaForm.standbyNameNodeHost) {
      message.warning('Active 和 Standby NameNode 不能是同一台主机')
      return
    }
    if (!hdfsHaForm.journalNodeHosts || hdfsHaForm.journalNodeHosts.length < 3) {
      message.warning('JournalNode 至少需要选择 3 台')
      return
    }
    if (!hdfsHaForm.zookeeperHosts || hdfsHaForm.zookeeperHosts.length === 0) {
      message.warning('ZooKeeper Hosts 不能为空')
      return
    }

    const [clusterId, serviceId] = componentPayload.value
    const res: any = await enableHdfsHa(clusterId, serviceId, hdfsHaForm)
    if (res?.id) {
      jobProgressStore.trackJob(clusterId, res.id, res.name ?? 'Enable HDFS HA', getServiceDetail)
    }
    hdfsHaModalOpen.value = false
  }

  const submitYarnHa = async () => {
    if (!yarnHaForm.activeResourceManagerHost || !yarnHaForm.standbyResourceManagerHost) {
      message.warning('Active/Standby ResourceManager 不能为空')
      return
    }
    if (yarnHaForm.activeResourceManagerHost === yarnHaForm.standbyResourceManagerHost) {
      message.warning('Active 和 Standby ResourceManager 不能是同一台主机')
      return
    }
    if (!yarnHaForm.zookeeperHosts || yarnHaForm.zookeeperHosts.length === 0) {
      message.warning('ZooKeeper Hosts 不能为空')
      return
    }

    const [clusterId, serviceId] = componentPayload.value
    const res: any = await enableYarnRmHa(clusterId, serviceId, yarnHaForm)
    if (res?.id) {
      jobProgressStore.trackJob(clusterId, res.id, res.name ?? 'Enable YARN HA', getServiceDetail)
    }
    yarnHaModalOpen.value = false
  }

  const dropdownMenuClick: GroupItem['dropdownMenuClickEvent'] = async ({ key }) => {
    const [clusterId, serviceId] = componentPayload.value
    const service = serviceMap.value[clusterId].filter((s) => Number(serviceId) == s.id)[0]
    const { name: serviceName, displayName } = service

    if (key === 'EnableHdfsHa') {
      await openHdfsHaModal()
      return
    }
    if (key === 'EnableYarnHa') {
      await openYarnHaModal()
      return
    }

    const processParams = {
      command: key as Key,
      clusterId,
      commandLevel: 'service',
      serviceCommands: [{ serviceName, installed: true }]
    } as CommandRequest

    if (key === 'Remove') {
      serviceStore.removeService(service, clusterId, () => onServiceDeleted(clusterId))
    } else {
      jobProgressStore.processCommand(processParams, getServiceDetail, { displayName })
    }
  }

  const getServiceDetail = async () => {
    try {
      loading.value = true
      serviceDetail.value = await serviceStore.getServiceDetail(...componentPayload.value)
    } catch (error) {
      console.log('error :>> ', error)
    } finally {
      loading.value = false
    }
  }

  provide('getServiceDetail', getServiceDetail)

  onMounted(() => {
    getServiceDetail()
  })
</script>

<template>
  <a-spin :spinning="loading">
    <header-card
      :title="serviceDetail?.displayName || serviceDetail?.name"
      :avatar="serviceDetail?.name"
      :desc="serviceDetail?.desc"
      :action-groups="actionGroup"
    />

    <a-modal
      v-model:open="hdfsHaModalOpen"
      title="启用 HDFS HA"
      :ok-text="'确定'"
      :cancel-text="'取消'"
      @ok="submitHdfsHa"
    >
      <a-form layout="vertical">
        <a-form-item label="Nameservice">
          <a-input v-model:value="hdfsHaForm.nameservice" />
        </a-form-item>

        <a-form-item label="Active NameNode">
          <a-select v-model:value="hdfsHaForm.activeNameNodeHost" :options="allHostOptions" />
        </a-form-item>

        <a-form-item label="Standby NameNode">
          <a-select v-model:value="hdfsHaForm.standbyNameNodeHost" :options="allHostOptions" />
        </a-form-item>

        <a-form-item label="JournalNode Hosts">
          <a-select v-model:value="hdfsHaForm.journalNodeHosts" mode="multiple" :options="allHostOptions" />
        </a-form-item>

        <a-form-item label="ZKFC Hosts">
          <a-select v-model:value="hdfsHaForm.zkfcHosts" mode="multiple" :options="allHostOptions" />
        </a-form-item>

        <a-form-item label="ZooKeeper Hosts">
          <a-select v-model:value="hdfsHaForm.zookeeperHosts" mode="multiple" :options="allHostOptions" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="yarnHaModalOpen"
      title="启用 YARN HA"
      :ok-text="'确定'"
      :cancel-text="'取消'"
      @ok="submitYarnHa"
    >
      <a-form layout="vertical">
        <a-form-item label="YARN ClusterId">
          <a-input v-model:value="yarnHaForm.yarnClusterId" />
        </a-form-item>

        <a-form-item label="RM IDs (默认 rm1,rm2)">
          <a-select
            v-model:value="yarnHaForm.rmIds"
            mode="multiple"
            :options="['rm1', 'rm2'].map((x) => ({ value: x }))"
          />
        </a-form-item>

        <a-form-item label="Active ResourceManager">
          <a-select v-model:value="yarnHaForm.activeResourceManagerHost" :options="allHostOptions" />
        </a-form-item>

        <a-form-item label="Standby ResourceManager">
          <a-select v-model:value="yarnHaForm.standbyResourceManagerHost" :options="allHostOptions" />
        </a-form-item>

        <a-form-item label="ZooKeeper Hosts">
          <a-select v-model:value="yarnHaForm.zookeeperHosts" mode="multiple" :options="allHostOptions" />
        </a-form-item>
      </a-form>
    </a-modal>

    <main-card v-model:active-key="activeTab" :tabs="tabs">
      <template #tab-item>
        <keep-alive>
          <component :is="getCompName" v-bind="{ ...serviceDetail }" />
        </keep-alive>
      </template>
    </main-card>
  </a-spin>
</template>

<style lang="scss" scoped></style>