import { defineStore } from 'pinia'

import { ref } from 'vue'
import {
  createChatThread,
  delAuthorizedPlatform,
  delChatThread,
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
  ChatThread,
  ChatThreadCondition,
  Platform,
  ChatThreadHistoryItem,
  AuthCredential,
  Sender,
  ChatThreadDelCondition
} from '@/api/chatbot/types'
import type { Option } from '@/components/chatbot/select-menu.vue'

import { sendChatMessage } from '@/api/sse'
import { AxiosProgressEvent, Canceler } from 'axios'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'

export default defineStore(
  'chatbot',
  () => {
    const { t } = useI18n()
    const chatThreads = ref<ChatThread[]>([])
    const authorizedPlatforms = ref<AuthorizedPlatform[]>([])
    const supportedPlatForms = ref<SupportedPlatForm[]>([])
    const chatThreadHistory = ref<ChatThreadHistoryItem[]>([])

    const currPlatform = ref<AuthorizedPlatform>()
    const currThread = ref<ChatThread>()
    const loading = ref(false)
    const isExpand = ref(false)
    const canceler = ref<Canceler>()
    const messageReciver = ref('')
    const currPage = ref<Option>({
      action: 'SUPPORTED_PLATFORM_SELECT',
      name: ''
    })
    const afterPages = ref<Option[]>([currPage.value])

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
        const authorizedPlatformNames = authorizedPlatforms.value.map(
          (v) => v.platformName
        )
        supportedPlatForms.value = data.filter((val) => {
          return !authorizedPlatformNames.includes(val.name)
        })
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
        return data
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
        const { currModel, platformId } = currPlatform.value
        const data = await getChatThreads({
          model: currModel as string,
          platformId
        })
        chatThreads.value = data
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    async function fetchCreateChatThread(platfromInfo: ChatThreadCondition) {
      try {
        chatThreadHistory.value = []
        const data = await createChatThread(platfromInfo)
        const threadInfo = {
          ...data,
          threadName: t('ai.thread_name', [chatThreads.value.length])
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
        return promise.then(onMessageComplete)
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    async function fetchDelAuthorizedPlatform(platformId: string | number) {
      try {
        const data = await delAuthorizedPlatform(platformId)
        message[`${data ? 'success' : 'error'}`](
          data ? t('common.delete_success') : t('common.delete_fail')
        )
        data && fetchAuthorizedPlatforms()
      } catch (error) {
        console.log('error :>> ', error)
      }
    }
    async function fetchDelChatThread(payload: ChatThreadDelCondition) {
      try {
        const data = await delChatThread(payload)
        message[`${data ? 'success' : 'error'}`](
          data ? t('common.delete_success') : t('common.delete_fail')
        )
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    const onMessageReceive = ({ event }: AxiosProgressEvent) => {
      messageReciver.value = event.target.responseText
        .replace(/data:\s*/g, '')
        .trim()
    }

    const onMessageComplete = (res: string | undefined) => {
      messageReciver.value = ''
      cancelSseConnect()
      updateThreadChatHistory('AI', (res || '').replace(/data:\s*/g, '').trim())
      return Promise.resolve(true)
    }

    const cancelSseConnect = () => {
      if (!canceler.value) {
        return
      }
      canceler.value()
    }

    return {
      currPage,
      afterPages,
      messageReciver,
      loading,
      isExpand,
      currPlatform,
      currThread,
      chatThreads,
      chatThreadHistory,
      authorizedPlatforms,
      supportedPlatForms,
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
      fetchSendChatMessage,
      fetchDelAuthorizedPlatform,
      fetchDelChatThread
    }
  },
  {
    persist: {
      storage: sessionStorage
    }
  }
)
