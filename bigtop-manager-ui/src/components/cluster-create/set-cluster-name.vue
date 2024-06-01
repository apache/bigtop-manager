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
  import { ref } from 'vue'
  import { RocketOutlined } from '@ant-design/icons-vue'

  const clusterInfo = defineModel<any>('clusterInfo')

  const formRef = ref<any>(null)

  const onNextStep = async () => {
    try {
      await formRef.value?.validate()
      return Promise.resolve(true)
    } catch (e) {
      return Promise.resolve(false)
    }
  }

  defineExpose({
    onNextStep
  })
</script>

<template>
  <div class="container">
    <a-result
      :title="$t('cluster.set_cluster_name_title')"
      :sub-title="$t('cluster.set_cluster_name_sub_title')"
    >
      <template #icon>
        <rocket-outlined />
      </template>
      <template #extra>
        <a-form ref="formRef" :model="clusterInfo.clusterCommand">
          <a-form-item
            name="clusterName"
            :rules="[
              {
                required: true,
                message: $t('cluster.set_cluster_name_valid')
              }
            ]"
          >
            <a-input
              v-model:value="clusterInfo.clusterCommand.clusterName"
              allow-clear
              :placeholder="$t('cluster.set_cluster_name_input')"
            />
          </a-form-item>
        </a-form>
      </template>
    </a-result>
  </div>
</template>

<style scoped lang="scss">
  .container {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    align-content: center;
    height: 100%;
  }
</style>
