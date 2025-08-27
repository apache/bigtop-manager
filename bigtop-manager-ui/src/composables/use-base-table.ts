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

import type { PaginationProps, TableColumnType, TableProps } from 'ant-design-vue'
import type { FilterValue } from 'ant-design-vue/es/table/interface'

type Data = { [key: string]: any }
type Result = { [key: string]: any | undefined }
type PaginationType = PaginationProps | false | undefined

interface FiltersParamsType {
  pageNum?: number
  pageSize?: number
  [propName: string]: FilterValue | null | number | undefined
}

export interface UseBaseTableProps<T = any> {
  columns: TableColumnType[]
  rows?: T[]
  pagination?: PaginationType
  onChangeCallback?: () => void
}

const useBaseTable = <T>(props: UseBaseTableProps<T>) => {
  const { columns, rows, pagination } = props
  const { t } = useI18n()
  const loading = ref(false)
  const dataSource = ref<T[]>(rows || [])
  const columnsProp = ref<TableColumnType[]>(columns)
  const paginationProps = ref<PaginationType>({
    current: 1,
    pageSize: 10,
    total: dataSource.value.length,
    size: 'small',
    showSizeChanger: true,
    pageSizeOptions: ['10', '20', '30', '40', '50'],
    showTotal: (total) => `${t('common.total', [total])}`
  })

  const filtersParams = ref<FiltersParamsType>({
    pageNum: paginationProps.value ? paginationProps.value.current : undefined,
    pageSize: paginationProps.value ? paginationProps.value.pageSize : undefined
  })

  // Merge pagination config
  if (pagination === undefined && paginationProps.value) {
    Object.assign(paginationProps.value, pagination)
  } else {
    paginationProps.value = false
  }

  const processData = (data: Data): Result => {
    const result: Result = {}
    if (!data) {
      return result
    }
    for (const [key, value] of Object.entries(data)) {
      if (value === null) {
        result[key] = undefined
      } else if (Array.isArray(value)) {
        result[key] = value[0] || undefined
      } else {
        result[key] = value
      }
    }
    return result
  }

  const onChange: TableProps['onChange'] = (pagination, filters) => {
    // Collect params of filters
    Object.assign(filtersParams.value, processData(filters))
    if (!paginationProps.value) {
      return
    }
    Object.assign(paginationProps.value, pagination)
    // Update value of params about pagination
    Object.assign(filtersParams.value, {
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    })

    if (props.onChangeCallback) {
      props.onChangeCallback()
    }
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
    filtersParams,
    onChange,
    resetState
  }
}

export default useBaseTable
