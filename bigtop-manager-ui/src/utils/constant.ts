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

import { message } from 'ant-design-vue'
import i18n from '@/locales'

export const API_RETRY_TIME = 3
export const API_EXPIRE_TIME = 3 * 1000
export const JOB_SCHEDULE_INTERVAL = 1000
export const MONITOR_SCHEDULE_INTERVAL = 10 * 1000
export const DEFAULT_PAGE_SIZE = 10

export const WS_URL = 'ws://' + window.location.host + '/ws/default'

export const WS_DEFAULT_OPTIONS = {
  autoReconnect: {
    retries: API_RETRY_TIME,
    delay: API_EXPIRE_TIME,
    async onFailed() {
      message.error(i18n.global.t('common.websocket_disconnected'))
    }
  },
  heartbeat: {
    message: 'ping',
    interval: API_EXPIRE_TIME,
    pongTimeout: API_EXPIRE_TIME
  }
}
