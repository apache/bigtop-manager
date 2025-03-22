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

export type StatusType = keyof typeof StatusColors

export enum StatusColors {
  success = 'var(--color-success)',
  error = 'var(--color-error)',
  warning = 'var(--color-warning)',
  default = 'var(--color-primary)'
}

export enum CommonStatus {
  healthy = 'success',
  unhealthy = 'error',
  unknown = 'warning'
}

export enum CommonStatusTexts {
  'healthy',
  'unhealthy',
  'unknown'
}

export enum JobState {
  'Pending' = 'pending',
  'Processing' = 'processing',
  'Successful' = 'successful',
  'Failed' = 'failed',
  'Canceled' = 'canceled'
}
