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
import request from './request'
import { AxiosRequestConfig } from 'axios'

const get = <T, U = any>(url: string, params?: U, config?: AxiosRequestConfig): Promise<T> => {
  return request.get(url, { ...config, params })
}

const post = <T, U = any>(url: string, data?: U, config?: AxiosRequestConfig): Promise<T> => {
  return request.post(url, data, config)
}

const put = <T, U = any>(url: string, data: U, config?: AxiosRequestConfig): Promise<T> => {
  return request.put(url, data, config)
}

const del = <T>(url: string, config?: AxiosRequestConfig): Promise<T> => {
  return request.delete(url, config)
}

export { get, post, put, del }
