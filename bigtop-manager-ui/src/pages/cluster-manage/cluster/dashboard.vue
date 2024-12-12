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
  const baseConfig = {
    clusterStatus: '集群状态',
    clusterName: '集群名',
    clusterDesc: '集群备注',
    hostCount: '主机数',
    serviceCount: '服务数',
    memory: '内存',
    coreCount: '核心数',
    diskSize: '磁盘大小',
    creater: '创建人'
  } as any

  const baseInfo = {
    clusterStatus: 'succsss',
    clusterName: '集群名',
    clusterDesc: '集群备注',
    hostCount: '15个主机',
    serviceCount: '15个服务',
    memory: '内存',
    coreCount: '15个核心',
    diskSize: '磁盘大小',
    creater: '创建人'
  } as any
</script>

<template>
  <div class="dashboard">
    <div class="left">
      <a-descriptions layout="vertical" bordered>
        <template #title>
          <a-typography-text strong>基本信息</a-typography-text>
        </template>
        <a-descriptions-item>
          <template #title>
            <a-typography-text strong>详情</a-typography-text>
          </template>
          <div class="desc-sub-wrp">
            <div class="desc-sub-item">
              <a-typography-text v-for="(base, idx) in Object.keys(baseInfo)" :key="idx" type="secondary">
                {{ baseConfig[base] }}
              </a-typography-text>
            </div>
            <div class="desc-sub-item">
              <div v-for="(base, idx) in Object.keys(baseInfo)" :key="idx">{{ baseInfo[base] }}</div>
            </div>
          </div>
        </a-descriptions-item>
      </a-descriptions>

      <a-descriptions layout="vertical" bordered :column="1">
        <template #title>
          <a-typography-text strong>服务信息</a-typography-text>
        </template>
        <a-descriptions-item>
          <template #label>
            <div class="desc-sub-label">
              <a-typography-text strong>Bigtop Stack</a-typography-text>
              <a-typography-text type="secondary">bigtop-3.3.0</a-typography-text>
            </div>
          </template>
        </a-descriptions-item>
        <a-descriptions-item>
          <template #label>
            <div class="desc-sub-label">
              <a-typography-text strong>Extra Stack</a-typography-text>
              <a-typography-text type="secondary">extra-1.0.0</a-typography-text>
            </div>
          </template>
        </a-descriptions-item>
      </a-descriptions>
    </div>
  </div>
</template>

<style lang="scss" scoped>
  .dashboard {
    display: flex;
  }
  .left {
    display: grid;
    width: 357px;
    gap: 24px;
    :deep(.ant-descriptions-item-content) {
      padding: $space-md;
      span {
        @include flexbox($direction: column, $gap: $space-md);
      }
    }
    :deep(.ant-descriptions-item-label) {
      padding: $space-md;
    }
    .desc-sub-wrp {
      display: flex;
      gap: $space-md;
    }
    .desc-sub-item {
      display: flex;
      flex-direction: column;
      gap: $space-md;
    }
    .desc-sub-label {
      @include flexbox($align: center, $gap: $space-sm);
      :deep(.a-typography-text:last-child) {
        font-size: 12px;
      }
    }
  }
</style>
