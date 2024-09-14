import { defineStore } from 'pinia'

import { ref } from 'vue'
import {
  createChatThread,
  getAuthorizedPlatforms,
  getChatThreads,
  getCredentialFormModelofPlatform,
  getSupportedPlatforms,
  getThreadChatHistory,
  validateAuthCredentials
} from '@/api/chatbot/index'
import type {
  AuthorizedPlatform,
  SupportedPlatForm,
  CredentialFormItem,
  ChatThread,
  ChatThreadCondition,
  Platform,
  ChatThreadHistoryItem,
  AuthCredential,
  Sender
} from '@/api/chatbot/types'
import { sendChatMessage } from '@/api/sse'
import { AxiosProgressEvent, Canceler } from 'axios'

export default defineStore(
  'chatbot',
  () => {
    const chatThreads = ref<ChatThread[]>([])
    const authorizedPlatforms = ref<AuthorizedPlatform[]>([])
    const supportedPlatForms = ref<SupportedPlatForm[]>([])
    const credentialFormModel = ref<CredentialFormItem[]>([])
    const chatThreadHistory = ref<ChatThreadHistoryItem[]>([])

    const currPlatform = ref<AuthorizedPlatform>()
    const currThread = ref<ChatThread>()
    const loading = ref(false)
    const isExpand = ref(false)
    const canceler = ref<Canceler>()
    const messageReciver = ref('')

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
    function updateThreadChatHistory(sender: Sender, message: string) {
      chatThreadHistory.value.push({
        sender,
        message,
        createTime: new Date().toISOString()
      })
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

    async function fetchThreadChatHistory() {
      try {
        const data = await getThreadChatHistory({
          platformId: currPlatform.value?.platformId as string | number,
          threadId: currThread.value?.threadId as string | number
        })
        chatThreadHistory.value = data
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    async function fetchSendChatMessage(message: string) {
      try {
        const reqparams = {
          platformId: currPlatform.value?.platformId as string | number,
          threadId: currThread.value?.threadId as string | number,
          message
        }
        const { cancel, promise } = sendChatMessage(reqparams, onMessageReceive)
        canceler.value = cancel
        promise.then(onMessageComplete)
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    const onMessageReceive = ({ event }: AxiosProgressEvent) => {
      const str = event.target.responseText.replace(/data:\s*/g, '').trim()
      messageReciver.value = str
    }

    const onMessageComplete = (res: string | undefined) => {
      messageReciver.value = ''
      cancelSseConnect()
      updateThreadChatHistory('AI', (res || '').replace(/data:\s*/g, '').trim())
    }

    const cancelSseConnect = () => {
      if (!canceler.value) {
        return
      }
      canceler.value()
    }

    return {
      messageReciver,
      loading,
      isExpand,
      currPlatform,
      currThread,
      chatThreads,
      chatThreadHistory,
      authorizedPlatforms,
      supportedPlatForms,
      credentialFormModel,
      updateCurrPlatform,
      updateCurrThread,
      updateThreadChatHistory,
      fetchSupportedPlatforms,
      fetchCredentialFormModelofPlatform,
      testAuthofPlatform,
      fetchCreateChatThread,
      fetchChatThreads,
      setWindowExpandStatus,
      fetchAuthorizedPlatforms,
      fetchThreadChatHistory,
      fetchSendChatMessage
    }
  },
  {
    persist: true // 启用持久化
  }
)
