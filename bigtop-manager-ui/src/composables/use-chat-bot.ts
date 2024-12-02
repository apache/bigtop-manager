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
import { ref } from 'vue'
import {
  createChatThread,
  delAuthorizedPlatform,
  delChatThread,
  getAuthorizedPlatforms,
  getChatThreads,
  getCredentialFormModelOfPlatform,
  getSupportedPlatforms,
  getThreadChatHistory,
  validateAuthCredentials
} from '@/api/chatbot/index'
import type {
  ChatThreadCondition,
  AuthCredential,
  ChatThreadDelCondition,
  SendChatMessageCondition
} from '@/api/chatbot/types'

import { sendChatMessage } from '@/api/sse'
import { AxiosProgressEvent, Canceler } from 'axios'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'

const useChatBot = () => {
  const { t } = useI18n()
  const loading = ref(false)
  const receiving = ref(false)
  const checkLoading = ref(false)
  const canceler = ref<Canceler>()
  const messageReceiver = ref('')

  function formatAuthCredentials<T extends Object>(authFormData: T): AuthCredential[] {
    const authCredentials: AuthCredential[] = []
    for (const [key, value] of Object.entries(authFormData)) {
      authCredentials.push({ key, value } as AuthCredential)
    }
    return authCredentials
  }

  async function fetchAuthorizedPlatforms() {
    try {
      loading.value = true
      const data = await getAuthorizedPlatforms()
      return Promise.resolve(data)
    } catch (error) {
      console.log('error :>> ', error)
      return []
    } finally {
      loading.value = false
    }
  }

  async function fetchSupportedPlatforms() {
    try {
      loading.value = true
      const data = await getSupportedPlatforms()
      return Promise.resolve(data)
    } catch (error) {
      console.log('error :>> ', error)
      return []
    } finally {
      loading.value = false
    }
  }

  async function fetchCredentialFormModelOfPlatform(platformId: string | number) {
    try {
      loading.value = true
      const data = await getCredentialFormModelOfPlatform(platformId)
      return Promise.resolve(data)
    } catch (error) {
      console.log('error :>> ', error)
      return []
    } finally {
      loading.value = false
    }
  }

  async function testAuthPlatform<T extends Object>(platformId: string | number, authFormData: T) {
    try {
      checkLoading.value = true
      const data = await validateAuthCredentials({
        platformId,
        authCredentials: formatAuthCredentials<T>(authFormData)
      })
      return Promise.resolve(data)
    } catch (error) {
      checkLoading.value = false
      console.log('error :>> ', error)
    }
  }

  async function fetchChatThreads(authId: string | number, model: string) {
    try {
      loading.value = true
      const data = await getChatThreads({
        model,
        authId
      })
      return Promise.resolve(data)
    } catch (error) {
      console.log('error :>> ', error)
      return []
    } finally {
      loading.value = false
    }
  }

  async function fetchCreateChatThread(platformInfo: ChatThreadCondition) {
    try {
      loading.value = true
      const data = await createChatThread(platformInfo)
      return Promise.resolve(data)
    } catch (error) {
      console.log('error :>> ', error)
    } finally {
      loading.value = false
    }
  }

  async function fetchThreadChatHistory(authId: string | number, threadId: string | number) {
    try {
      loading.value = true
      const data = await getThreadChatHistory({ authId, threadId })
      return Promise.resolve(data)
    } catch (error) {
      console.log('error :>> ', error)
      return []
    } finally {
      loading.value = false
    }
  }

  async function fetchSendChatMessage(reqparams: SendChatMessageCondition) {
    try {
      const { cancel, promise } = sendChatMessage(reqparams, onMessageReceive)
      canceler.value = cancel
      return promise.then(onMessageComplete)
    } catch (error) {
      console.log('error :>> ', error)
    }
  }

  async function fetchDelAuthorizedPlatform(platformId: string | number) {
    try {
      loading.value = true
      const data = await delAuthorizedPlatform(platformId)
      message[`${data ? 'success' : 'error'}`](data ? t('common.delete_success') : t('common.delete_fail'))
    } catch (error) {
      console.log('error :>> ', error)
    } finally {
      loading.value = false
    }
  }
  async function fetchDelChatThread(payload: ChatThreadDelCondition) {
    try {
      loading.value = true
      const data = await delChatThread(payload)
      message[`${data ? 'success' : 'error'}`](data ? t('common.delete_success') : t('common.delete_fail'))
    } catch (error) {
      console.log('error :>> ', error)
    } finally {
      loading.value = false
    }
  }

  const onMessageReceive = ({ event }: AxiosProgressEvent) => {
    messageReceiver.value = event.target.responseText
      .split('data:')
      .map((s: string) => {
        return s.trimEnd()
      })
      .join('')
  }

  const onMessageComplete = () => {
    const formatResultMsg = messageReceiver.value
    messageReceiver.value = ''
    cancelSseConnect()
    return Promise.resolve({ message: formatResultMsg, state: true })
  }

  const cancelSseConnect = () => {
    if (!canceler.value) {
      return
    }
    canceler.value()
  }

  return {
    messageReceiver,
    loading,
    receiving,
    checkLoading,
    fetchSupportedPlatforms,
    fetchCredentialFormModelOfPlatform,
    testAuthPlatform,
    fetchCreateChatThread,
    fetchChatThreads,
    fetchAuthorizedPlatforms,
    fetchThreadChatHistory,
    fetchSendChatMessage,
    fetchDelAuthorizedPlatform,
    fetchDelChatThread
  }
}

export default useChatBot
