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

const useBaseTable = <T>(
  columns: TableColumnType[],
  rows?: T[],
  pagination?: TablePaginationConfig | false | undefined
) => {
  const loading = ref(false)
  const dataSource = ref<T[]>(rows || [])
  const columnsProp = ref<TableColumnType[]>(columns)
  const paginateProp = ref<TablePaginationConfig>({
    current: 1,
    pageSize: 10,
    total: dataSource.value.length,
    size: 'small',
    showSizeChanger: true,
    pageSizeOptions: ['1', '20', '30', '40', '50']
  })

  // merge pagination config
  if (pagination) {
    paginateProp.value = Object.assign(paginateProp.value, pagination)
  }

  const onChange = (pagination: TablePaginationConfig) => {
    paginateProp.value = Object.assign(paginateProp.value, pagination)
  }

  const restState = () => {
    loading.value = false
    dataSource.value = []
    paginateProp.value = {
      current: 1,
      pageSize: 10,
      total: dataSource.value.length || 0,
      size: 'small',
      showSizeChanger: true,
      pageSizeOptions: ['10', '20', '30', '40', '50']
    }
  }

  onUnmounted(() => {
    restState()
  })

  return {
    columnsProp,
    dataSource,
    loading,
    paginateProp,
    onChange,
    restState
  }
}

export default useBaseTable
