/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import { ref, onUnmounted } from 'vue'
import type { TablePaginationConfig, TableColumnType } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'

type PaginationProps = TablePaginationConfig | false | undefined
export interface UseBaseTableProps<T = any> {
  columns: TableColumnType[]
  rows?: T[]
  pagination?: PaginationProps
}
const useBaseTable = <T>(props: UseBaseTableProps<T>) => {
  const { columns, rows, pagination } = props
  const { t } = useI18n()
  const loading = ref(false)
  const dataSource = ref<T[]>(rows || [])
  const columnsProp = ref<TableColumnType[]>(columns)
  const paginationProps = ref<PaginationProps>({
    current: 1,
    pageSize: 10,
    total: dataSource.value.length,
    size: 'small',
    showSizeChanger: true,
    pageSizeOptions: ['10', '20', '30', '40', '50'],
    showTotal: (total) => `${t('common.total', [total])}`
  })

  // merge pagination config
  if (pagination === undefined && paginationProps.value) {
    paginationProps.value = Object.assign(paginationProps.value, pagination)
  } else {
    paginationProps.value = false
  }

  const onChange = (pagination: TablePaginationConfig) => {
    if (!paginationProps.value) {
      return
    }
    paginationProps.value = Object.assign(paginationProps.value, pagination)
  }

  const resetState = () => {
    loading.value = false
    dataSource.value = []
    paginationProps.value = {
      current: 1,
      pageSize: 10,
      total: dataSource.value.length || 0,
      size: 'small',
      showSizeChanger: true,
      pageSizeOptions: ['10', '20', '30', '40', '50'],
      showTotal: (total) => `${t('common.total', [total])}`
    }
  }

  onUnmounted(() => {
    resetState()
  })

  return {
    columnsProp,
    dataSource,
    loading,
    paginationProps,
    onChange,
    resetState
  }
}

export default useBaseTable
