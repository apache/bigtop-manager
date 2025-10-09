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

export type TimeRangeType = '1m' | '5m' | '15m' | '30m' | '1h' | '2h'

export type MetricsData = {
  cpuUsageCur: string
  memoryUsageCur: string
  diskUsageCur: string
  fileDescriptorUsage: string
  diskReadCur: string
  diskWriteCur: string
  cpuUsage: string[]
  systemLoad1: string[]
  systemLoad5: string[]
  systemLoad15: string[]
  memoryUsage: string[]
  diskRead: string[]
  diskWrite: string[]
  timestamps: string[]
}

export type ServiceMetricType =
  | 'NUMBER' // Numeric value, unitless
  | 'PERCENT' // Percent, followed by %
  | 'BYTE' // Byte, the front end can convert it through formatFromByte in storage util, the unit is B/KB/MB, etc.
  | 'MILLISECOND' // milliseconds, followed by ms
  | 'BPS' // Bytes per second, followed by B/s
  | 'NPS' // Numeric value per second, followed by N/s

type ServiceMetricItemSeries = {
  name: string
  type: string
  data: any[]
  [propName: string]: any
}

export type ServiceMetricItem = {
  title: string
  valueType: Lowercase<ServiceMetricType>
  series: ServiceMetricItemSeries[]
}

export type ServiceMetrics = {
  charts: ServiceMetricItem[]
  timestamps: string[]
}
