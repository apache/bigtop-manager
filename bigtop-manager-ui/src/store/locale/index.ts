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

import i18n from '@/locales'
import en_US from 'ant-design-vue/es/locale/en_US'
import zh_CN from 'ant-design-vue/es/locale/zh_CN'
import dayjs from 'dayjs'

import 'dayjs/locale/zh-cn'
import 'dayjs/locale/en'

import { Locale, defaultLocale } from './types'

export const useLocaleStore = defineStore(
  'locale',
  () => {
    const locale = ref(defaultLocale)
    const antd = computed(() => (locale.value === 'en_US' ? en_US : zh_CN))

    const setLocale = (newLocale: Locale) => {
      locale.value = newLocale
    }

    watch(locale, async (newLocale: Locale) => {
      i18n.global.locale.value = newLocale as Locale
      dayjs.locale(antd.value.locale)
    })

    return {
      locale,
      antd,
      setLocale
    }
  },
  {
    persist: {
      storage: localStorage
    }
  }
)
