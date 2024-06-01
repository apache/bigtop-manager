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

import request from '@/api/request.ts'
import { AxiosProgressEvent } from 'axios'

export const getLogs = (
  clusterId: number,
  id: number,
  func: Function
): Promise<any> => {
  return request({
    method: 'get',
    url: `/sse/clusters/${clusterId}/tasks/${id}/log`,
    responseType: 'stream',
    timeout: 0,
    onDownloadProgress: (progressEvent: AxiosProgressEvent) =>
      func(progressEvent)
  })
}
