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

import { LoginReq, LoginVO } from '@/api/login/types.ts'
import request from '@/api/request.ts'

export const getSalt = (username: string): Promise<string> => {
  return request({
    method: 'get',
    url: '/salt',
    params: {
      username
    }
  })
}

export const getNonce = (username: string): Promise<string> => {
  return request({
    method: 'get',
    url: '/nonce',
    params: {
      username
    }
  })
}

export const login = (data: LoginReq): Promise<LoginVO> => {
  return request({
    method: 'post',
    url: '/login',
    data
  })
}

export const test = () => {
  return request({
    method: 'get',
    url: '/test'
  })
}
