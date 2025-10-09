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

import { get } from '@/api/request-util'
import type { MetricsData, ServiceMetrics, TimeRangeType } from './types'

export const getClusterMetricsInfo = (paramsPath: { id: number }, params: { interval: TimeRangeType }) => {
  return get<MetricsData>(`/metrics/clusters/${paramsPath.id}`, params)
}

export const getHostMetricsInfo = (paramsPath: { id: number }, params: { interval: TimeRangeType }) => {
  return get<MetricsData>(`/metrics/hosts/${paramsPath.id}`, params)
}

export const getServiceMetricsInfo = (paramsPath: { id: number }, params: { interval: TimeRangeType }) => {
  return get<ServiceMetrics>(`/metrics/services/${paramsPath.id}`, params)
}
