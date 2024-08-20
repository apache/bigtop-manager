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
  import { reactive, UnwrapRef } from 'vue'
  import { CheckOutlined, EditOutlined } from '@ant-design/icons-vue'
  import { useI18n } from 'vue-i18n'
  import { useStackStore } from '@/store/stack'
  import { storeToRefs } from 'pinia'
  import _ from 'lodash'
  import { RepoVO } from '@/api/repo/types.ts'

  const clusterInfo = defineModel<any>('clusterInfo')

  const { t } = useI18n()
  const stackStore = useStackStore()

  const editableData: UnwrapRef<Record<number, RepoVO>> = reactive({})
  const { stackRepos } = storeToRefs(stackStore)

  const repositoryData: RepoVO[] = _.cloneDeep(
    stackRepos.value[clusterInfo.value.clusterCommand.fullStackName]
  )

  const repositoryColumns = [
    {
      title: t('common.os'),
      dataIndex: 'os',
      align: 'center',
      width: 150
    },
    {
      title: t('common.arch'),
      dataIndex: 'arch',
      align: 'center',
      width: 150
    },
    {
      title: t('common.base_url'),
      dataIndex: 'baseUrl',
      align: 'center',
      ellipsis: true
    }
  ]

  const edit = (index: number) => {
    editableData[index] = _.cloneDeep(repositoryData[index])
  }

  const save = (index: number) => {
    Object.assign(repositoryData[index], editableData[index])

    delete editableData[index]
  }

  const onNextStep = async () => {
    Object.assign(clusterInfo.value.clusterCommand.repoInfoList, repositoryData)

    return Promise.resolve(true)
  }

  defineExpose({
    onNextStep
  })
</script>

<template>
  <div class="container">
    <div class="title">{{ $t('cluster.set_repository') }}</div>
    <a-table
      :pagination="false"
      :scroll="{ y: 400 }"
      :columns="repositoryColumns"
      :data-source="repositoryData"
    >
      <template #bodyCell="{ text, index, column }">
        <template v-if="column.dataIndex === 'baseUrl'">
          <div class="editable-cell">
            <div v-if="editableData[index]" class="editable-cell-input-wrapper">
              <a-input
                v-model:value="editableData[index].baseUrl"
                @press-enter="save(index)"
              />
              <check-outlined
                class="editable-cell-icon-check"
                @click="save(index)"
              />
            </div>
            <div v-else class="editable-cell-text-wrapper">
              {{ text || ' ' }}
              <edit-outlined class="editable-cell-icon" @click="edit(index)" />
            </div>
          </div>
        </template>
      </template>
    </a-table>
  </div>
</template>

<style scoped lang="scss">
  .container {
    @include flexbox($direction: column, $justify: start, $align: center);
    align-content: center;
    height: 100%;

    .title {
      font-size: 1.5rem;
      line-height: 2rem;
      margin-bottom: 1rem;
    }

    .editable-cell {
      position: relative;

      .editable-cell-input-wrapper {
        @include flexbox($justify: center, $align: center);
        padding-right: 24px;

        .editable-cell-icon-check {
          position: absolute;
          line-height: 28px;
          right: 0;
          width: 20px;
          cursor: pointer;

          &:hover {
            color: #108ee9;
          }
        }
      }

      .editable-cell-text-wrapper {
        @include flexbox($justify: center, $align: center);
        padding: 5px 24px 5px 5px;

        .editable-cell-icon {
          display: none;
          position: absolute;
          margin-top: 4px;
          right: 0;
          width: 20px;
          cursor: pointer;

          &:hover {
            color: #108ee9;
          }
        }
      }

      &:hover {
        .editable-cell-icon {
          display: inline-block;
        }
      }
    }
  }
</style>
