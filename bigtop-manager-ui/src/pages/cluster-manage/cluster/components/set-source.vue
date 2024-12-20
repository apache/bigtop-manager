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
  import { ref } from 'vue'

  const open = ref(false)

  const handleOpen = () => {
    open.value = true
  }

  const handleOk = () => {
    open.value = false
  }

  const handleCancel = () => {
    open.value = false
  }

  defineExpose({
    handleOpen
  })
</script>

<template>
  <div class="set-source">
    <a-modal
      v-model:open="open"
      :width="700"
      :centered="true"
      :mask="false"
      :title="$t('cluster.source')"
      :mask-closable="false"
      :destroy-on-close="true"
      @ok="handleOk"
      @cancel="handleCancel"
    >
    </a-modal>
  </div>
</template>

<style scoped></style>
