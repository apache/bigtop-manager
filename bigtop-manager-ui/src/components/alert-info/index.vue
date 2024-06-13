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
  import { BellOutlined } from '@ant-design/icons-vue'
  import { computed, ref } from 'vue'
  import DotState from '@/components/dot-state/index.vue'
  import dayjs from 'dayjs'
  import customParseFormat from 'dayjs/plugin/customParseFormat'
  const visible = ref(false)
  const overlayInnerStyle = {
    padding: '12px 0',
    width: '400px'
  }

  // now Date
  const getAlertTrigger = computed(() => {
    dayjs.extend(customParseFormat)
    return dayjs(new Date())
  })
</script>

<template>
  <a-popover
    v-model:open="visible"
    placement="bottomRight"
    trigger="click"
    :arrow-point-at-center="true"
    :auto-adjust-overflow="true"
    :overlay-inner-style="overlayInnerStyle"
  >
    <template #title>
      <div class="alert-title">{{ $t('common.notification') }}</div>
    </template>
    <template #content>
      <ul class="alert-list">
        <li v-for="idx in 100" :key="idx">
          <dot-state
            width="10"
            height="10"
            style="margin-right: 10px; line-height: 28px"
            color="#f5222d"
          />
          <div>
            <div class="alert-list-ctx">
              <div>DataNode Process</div>
              <p>
                Ulimit for open files (-n)is 1048576 which is higher orequal
                than critical value of 800000
              </p>
            </div>
            <div class="alert-list-state">{{ getAlertTrigger }}</div>
          </div>
        </li>
      </ul>
      <footer>
        <a>{{ $t('common.view_all') }}</a>
      </footer>
    </template>
    <div class="alert">
      <a-badge size="small" color="red" count="100">
        <bell-outlined />
      </a-badge>
    </div>
  </a-popover>
</template>

<style lang="scss" scoped>
  .alert {
    height: 36px;
    width: 36px;
    font-size: 16px;
    cursor: pointer;
    border-radius: 50%;
    @include flex(center, center);

    &:hover {
      background-color: var(--hover-color);
    }
  }
  .alert-title {
    font-size: 16px;
    color: #333333;
    font-weight: normal;
    padding-left: 10px;
  }
  .alert-list {
    padding: 0;
    list-style: none;
    max-height: 400px;
    overflow-y: auto;
    margin-bottom: 0;

    li {
      padding: 10px;
      cursor: pointer;
      @include flex(center, null);
      &:hover {
        background-color: var(--hover-color);
      }

      &:not(:last-child) {
        border-bottom: 1px solid #eeeeee;
      }
    }

    &-ctx {
      div {
        font-weight: 700;
        color: #333333;
        font-size: 16px;
      }
      p {
        color: #666666;
      }
    }
    &-state {
      text-align: end;
      color: #999999;
      font-size: 14px;
    }
  }
  footer {
    border-top: 1px solid #eeeeee;
    text-align: end;
    padding: 10px 28px 0 10px;
  }
</style>
