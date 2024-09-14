import { defineStore } from 'pinia'

import { ref } from 'vue'
import {
  createChatThread,
  getAuthorizedPlatforms,
  getChatThreads,
  getCredentialFormModelofPlatform,
  getSupportedPlatforms,
  validateAuthCredentials
} from '@/api/chatbot/index'
import type {
  AuthorizedPlatform,
  SupportedPlatForm,
  CredentialFormItem,
  ChatThread,
  ChatThreadCondition,
  Platform,
  ChatHistoryItem,
  AuthCredential
} from '@/api/chatbot/types'

export default defineStore(
  'chatbot',
  () => {
    const authorizedPlatforms = ref<AuthorizedPlatform[]>([])
    const supportedPlatForms = ref<SupportedPlatForm[]>([])
    const credentialFormModel = ref<CredentialFormItem[]>([])

    const chatThreads = ref<ChatThread[]>([])
    const currPlatform = ref<AuthorizedPlatform>()
    const currThread = ref<ChatThread>()
    const chatRecords = ref<ChatHistoryItem[]>([])
    const loading = ref(false)
    const isExpand = ref(false)

    function setWindowExpandStatus(status: boolean) {
      isExpand.value = status
    }

    function updateCurrPlatform(platform: Platform) {
      currPlatform.value = platform
    }

    function formatAuthCredentials<T extends Object>(
      authFormData: T
    ): AuthCredential[] {
      const authCredentials: AuthCredential[] = []
      for (const [key, value] of Object.entries(authFormData)) {
        authCredentials.push({ key, value } as AuthCredential)
      }
      return authCredentials
    }

    function updateCurrThread(thread: ChatThread) {
      currThread.value = thread
    }

    function updateThreads(thread: ChatThread) {
      const threadIds = chatThreads.value.map((v) => v.threadId)
      if (!threadIds.includes(thread.threadId)) {
        chatThreads.value.push(thread)
      }
    }

    async function fetchAuthorizedPlatforms() {
      try {
        const data = await getAuthorizedPlatforms()
        authorizedPlatforms.value = data
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    async function fetchSupportedPlatforms() {
      try {
        const data = await getSupportedPlatforms()
        supportedPlatForms.value = data
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    async function fetchCredentialFormModelofPlatform() {
      try {
        if (!currPlatform.value?.platformId) {
          return
        }
        const data = await getCredentialFormModelofPlatform(
          currPlatform.value?.platformId
        )
        credentialFormModel.value = data
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    async function testAuthofPlatform<T extends Object>(authFormData: T) {
      try {
        if (!currPlatform.value?.platformId) {
          return
        }
        loading.value = true
        const data = await validateAuthCredentials({
          platformId: currPlatform.value?.platformId,
          authCredentials: formatAuthCredentials<T>(authFormData)
        })
        updateCurrPlatform(data)
        return Promise.resolve(true)
      } catch (error) {
        return Promise.resolve(false)
      }
    }

    async function fetchChatThreads() {
      try {
        if (!currPlatform.value?.platformId) {
          return
        }
        const data = await getChatThreads({
          model: currPlatform.value.currModel as string,
          platformId: currPlatform.value.platformId
        })
        chatThreads.value = data
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    async function fetchCreateChatThread(platfromInfo: ChatThreadCondition) {
      try {
        const data = await createChatThread(platfromInfo)
        const threadInfo = {
          ...data,
          threadName: `线程${chatThreads.value.length}`
        }
        updateThreads(threadInfo)
        updateCurrThread(threadInfo)
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    return {
      loading,
      isExpand,
      currPlatform,
      currThread,
      chatThreads,
      chatRecords,
      authorizedPlatforms,
      supportedPlatForms,
      credentialFormModel,
      updateCurrPlatform,
      updateCurrThread,
      fetchSupportedPlatforms,
      fetchCredentialFormModelofPlatform,
      testAuthofPlatform,
      fetchCreateChatThread,
      fetchChatThreads,
      setWindowExpandStatus,
      fetchAuthorizedPlatforms
    }
  },
  {
    persist: true // 启用持久化
  }
)
