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

import axios, {
  AxiosError,
  AxiosResponse,
  InternalAxiosRequestConfig
} from 'axios'
import { message } from 'ant-design-vue'
import { ResponseEntity } from '@/api/types'
import router from '@/router'
import i18n from '@/locales'
import { API_EXPIRE_TIME } from '@/utils/constant.ts'
import { Locale } from '@/store/locale/types.ts'

const request = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  withCredentials: true,
  timeout: API_EXPIRE_TIME
})

request.interceptors.request.use(
  (
    config: InternalAxiosRequestConfig<any>
  ): InternalAxiosRequestConfig<any> => {
    config.headers = config.headers || {}

    const locale = i18n.global.locale.value as Locale
    config.headers['Accept-Language'] = locale.replace('_', '-')

    const token =
      localStorage.getItem('Token') ??
      sessionStorage.getItem('Token') ??
      undefined
    if (token) {
      config.headers['Token'] = token
    }

    return config
  }
)

request.interceptors.response.use(
  async (res: AxiosResponse) => {
    if (res.config.responseType === 'stream') {
      // Skip SSE api check
      return res.data
    } else {
      const responseEntity: ResponseEntity = res.data
      if (responseEntity.code !== 0) {
        message.error(responseEntity.message)
        if (responseEntity.code === 10000) {
          await router.push('/login')
        }
        return Promise.reject(responseEntity)
      } else {
        return responseEntity.data
      }
    }
  },
  async (error: AxiosError) => {
    if (error.code === AxiosError.ERR_CANCELED) {
      return
    }
    if (error.code === AxiosError.ERR_NETWORK) {
      message.error(i18n.global.t('common.error_network'))
    } else if (error.code === AxiosError.ETIMEDOUT) {
      message.error(i18n.global.t('common.error_timeout'))
    } else {
      console.log(error)
      message.error(i18n.global.t('common.error_unknown'))
    }

    console.log(error)
    return Promise.reject(error)
  }
)

export default request
