<!--
  - Licensed to the Apache Software Foundation (ASF) under one
  - or more contributor license agreements.  See the NOTICE file
  - distributed with this work for additional information
  - regarding copyright ownership.  The ASF licenses this file
  - to you under the Apache License, Version 2.0 (the
  - "License"); you may not use this file except in compliance
  - with the License.  You may obtain a copy of the License at
  -
  -    https://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing,
  - software distributed under the License is distributed on an
  - "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  - KIND, either express or implied.  See the License for the
  - specific language governing permissions and limitations
  - under the License.
  -->

<script setup lang="ts">
  import { onMounted, onBeforeUnmount, ref } from 'vue'
  import * as echarts from 'echarts'
  import { useHostStore } from '@/store/host'
  import { storeToRefs } from 'pinia'
  import {
    CheckCircleTwoTone,
    CloseCircleTwoTone,
    MinusCircleTwoTone,
    DoubleRightOutlined
  } from '@ant-design/icons-vue'
  import { RouterLink } from 'vue-router'

  const hostStore = useHostStore()
  const { hosts, loading } = storeToRefs(hostStore)

  const resourceChartRef = ref<any>(null)
  const diskChartRef = ref<any>(null)

  const hostColumns = [
    {
      title: 'hosts.hostname',
      dataIndex: 'hostname',
      align: 'center'
    },
    {
      title: 'hosts.cluster_name',
      dataIndex: 'clusterName',
      align: 'center'
    },
    {
      title: 'common.os',
      dataIndex: 'os',
      align: 'center'
    },
    {
      title: 'common.arch',
      dataIndex: 'arch',
      align: 'center'
    },
    {
      title: 'hosts.ipv4',
      dataIndex: 'ipv4',
      align: 'center'
    },
    {
      title: 'hosts.cores',
      dataIndex: 'availableProcessors',
      align: 'center'
    },
    {
      title: 'hosts.ram',
      dataIndex: 'totalMemorySize',
      align: 'center'
    },
    {
      title: 'hosts.disk',
      dataIndex: 'totalDisk',
      align: 'center'
    },
    {
      title: 'common.status',
      dataIndex: 'state',
      align: 'center'
    }
  ]

  const renderResourceChart = () => {
    let option = {
      title: {
        text: 'Resource'
      },
      legend: {
        data: ['Core Usage(%)', 'Memory Usage(%)']
      },
      grid: {
        left: '60px',
        width: '75%'
      },
      xAxis: {
        type: 'category',
        data: ['10:00', '11:00', '12:00', '13:00', '14:00', '15:00', '16:00']
      },
      yAxis: {
        type: 'value',
        scale: true,
        name: 'Percentage',
        axisLabel: {
          formatter: '{value} %'
        }
      },
      series: [
        {
          name: 'Core Usage(%)',
          data: [10, 10, 90, 30, 10, 90, 90],
          type: 'line',
          smooth: true
        },
        {
          name: 'Memory Usage(%)',
          data: [22, 19, 88, 66, 5, 90, 75],
          type: 'line',
          smooth: true
        }
      ],
      tooltip: {
        show: true,
        trigger: 'axis',
        axisPointer: {
          type: 'cross',
          axis: 'auto'
        },
        showContent: true
      }
    }

    const chart = echarts.init(resourceChartRef.value)
    chart.setOption(option)
  }

  const renderDiskChart = () => {
    let option = {
      title: {
        text: 'Disk'
      },
      legend: {
        data: ['Disk Usage(%)', 'Disk Increment(MB)']
      },
      grid: {
        left: '60px',
        width: '75%'
      },
      xAxis: {
        type: 'category',
        data: [
          '2023/08/13',
          '2023/08/14',
          '2023/08/15',
          '2023/08/16',
          '2023/08/17',
          '2023/08/18',
          '2023/08/19'
        ]
      },
      yAxis: [
        {
          type: 'value',
          scale: true,
          name: 'Increment',
          min: 0,
          max: 1000,
          interval: 100,
          axisLabel: {
            formatter: '{value} MB'
          }
        },
        {
          type: 'value',
          scale: true,
          name: 'Percentage',
          max: 100,
          min: 0,
          interval: 10,
          axisLabel: {
            formatter: '{value} %'
          }
        }
      ],
      series: [
        {
          name: 'Disk Increment(MB)',
          data: [100, 800, 500, 233, 420, 155, 333],
          type: 'bar',
          yAxisIndex: 0
        },
        {
          name: 'Disk Usage(%)',
          data: [20, 35, 40, 65, 75, 90, 97],
          type: 'line',
          smooth: true,
          yAxisIndex: 1
        }
      ],
      tooltip: {
        show: true,
        trigger: 'axis',
        axisPointer: {
          type: 'cross',
          axis: 'auto'
        },
        showContent: true
      }
    }

    const chart = echarts.init(diskChartRef.value)
    chart.setOption(option)
  }

  onMounted(async () => {
    renderResourceChart()
    renderDiskChart()

    hostStore.resumeIntervalFn()
  })

  onBeforeUnmount(() => {
    hostStore.pauseIntervalFn()
  })
</script>

<template>
  <a-watermark content="This is fake data">
    <div class="container">
      <div class="statistics">
        <a-card class="card">
          <a-statistic title="Hosts" value="4" />
          <div class="card-desc">Health: 4</div>
        </a-card>
        <a-card class="card">
          <a-statistic title="Cores" value="12 / 16" />
          <div class="card-desc">75%</div>
        </a-card>
        <a-card class="card">
          <a-statistic title="Memory" value="123 / 256 G" />
          <div class="card-desc">48.05%</div>
        </a-card>
        <a-card class="card">
          <a-statistic title="Disks" value="2 / 5 T" />
          <div class="card-desc">40%</div>
        </a-card>
      </div>
      <a-divider dashed />
      <div class="charts">
        <div ref="resourceChartRef" class="chart" />
        <div ref="diskChartRef" class="chart" />
      </div>
      <a-divider dashed />

      <div class="host-link">
        <router-link to="/hosts">
          {{ $t('common.view_all') }} <double-right-outlined />
        </router-link>
      </div>

      <a-table
        :columns="hostColumns"
        :data-source="hosts.length > 5 ? hosts.slice(0, 5) : hosts"
        :loading="loading"
        :pagination="false"
      >
        <template #headerCell="{ column }">
          <span>{{ $t(column.title) }}</span>
        </template>
        <template #bodyCell="{ column, text }">
          <template v-if="column.dataIndex === 'state'">
            <CheckCircleTwoTone
              v-if="text === 'INSTALLED'"
              two-tone-color="#52c41a"
            />
            <MinusCircleTwoTone
              v-else-if="text === 'MAINTAINED'"
              two-tone-color="orange"
            />
            <CloseCircleTwoTone v-else two-tone-color="red" />
          </template>
        </template>
      </a-table>
    </div>
  </a-watermark>
</template>

<style scoped lang="scss">
  .container {
    min-height: 540px;

    .statistics {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .card {
        width: 20%;
        text-align: center;

        .card-desc {
          font-size: 0.75rem;
          line-height: 1rem;
          color: rgb(156 163 175);
          margin-top: 0.25rem;
        }
      }
    }

    .charts {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .chart {
        width: 45%;
        height: 24rem;
      }
    }

    .host-link {
      display: flex;
      flex-direction: column;
      align-items: end;
      margin-bottom: 1.5rem;
    }
  }
</style>
