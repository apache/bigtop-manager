import { defineStore } from 'pinia'
import {
  platformOfCredentialTest,
  newThreadCreate,
  connectAssistant
} from './request'
import type {
  AuthCredential,
  Platform,
  ThreadCondition,
  ThreadInfo,
  Record
} from './request'

import { ref } from 'vue'
import {
  authorizedPlatforms,
  credentialFormModelofPlatform,
  supportedPlatforms
} from '@/api/chatbot/index'
import type {
  AuthorizedPlatform,
  SupportedPlatForm,
  CredentialFormItem
} from '@/api/chatbot/types'

export default defineStore(
  'chatbot',
  () => {
    const authorizedPlatformList = ref<AuthorizedPlatform[]>([])
    const supportedPlatFormsList = ref<SupportedPlatForm[]>([])
    const credentialFormModel = ref<CredentialFormItem[]>([])

    const threads = ref<ThreadInfo[]>([])
    const currPlatform = ref<Platform>()
    const currThread = ref<ThreadInfo>()
    const chatRecords = ref<Record[]>([])
    const loading = ref(false)
    const isExpand = ref(false)

    function updateCurrPlatform(platform: Platform) {
      currPlatform.value = platform
    }

    function updateCurrThread(thread: ThreadInfo) {
      currThread.value = thread
    }

    function updateThreads(thread: ThreadInfo) {
      const threadIds = threads.value.map((v) => v.threadId)
      if (!threadIds.includes(thread.threadId)) {
        threads.value.push(thread)
      }
    }

    function updateChatRecords(record: Record) {
      chatRecords.value.push(record)
    }

    function setWindowExpandStatus(status: boolean) {
      isExpand.value = status
    }

    async function getAuthorizedPlatforms() {
      try {
        const data = await authorizedPlatforms()
        authorizedPlatformList.value = data
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    async function getSupportedPlatforms() {
      try {
        const data = await supportedPlatforms()
        supportedPlatFormsList.value = data
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    /**
     * 获取平台ID的认证表单字段
     */
    async function getCredentialFormModelofPlatform() {
      try {
        if (!currPlatform.value?.id) {
          return
        }
        const data = await credentialFormModelofPlatform(currPlatform.value?.id)
        credentialFormModel.value = data
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    /**
     * 测试ai平台的授权认证
     * @param authFormData
     * @returns
     */
    async function testAuthofPlatform(authFormData = {}) {
      try {
        if (!currPlatform.value?.id) {
          return
        }

        const authCredentials: AuthCredential[] = []
        const platformId = currPlatform.value?.id
        for (const [key, value] of Object.entries(authFormData)) {
          authCredentials.push({ key, value } as AuthCredential)
        }
        const { code, data } = await platformOfCredentialTest({
          platformId,
          authCredentials
        })
        if (code === 200) {
          updateCurrPlatform(data)
        }
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    /**
     * 创建新的聊天线程
     * @param platfromInfo
     */
    async function createThreadofChat(platfromInfo: ThreadCondition) {
      try {
        const { code, data } = await newThreadCreate(platfromInfo)
        if (code === 200) {
          updateThreads(data)
          updateCurrThread(data)
        }
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    /**
     * 与ai建立连接
     * @returns
     */
    async function startConnectAssistant() {
      if (currThread.value?.threadId) {
        return
      }
      try {
        const { code, data } = await connectAssistant({
          threadId: currThread.value?.threadId as string | number
        })
        if (code === 200) {
          updateChatRecords(data)
        }
      } catch (error) {
        console.log('error :>> ', error)
      }
    }

    return {
      authorizedPlatformList,
      supportedPlatFormsList,
      credentialFormModel,
      loading,
      isExpand,
      currPlatform,
      currThread,
      threads,
      chatRecords,
      updateCurrPlatform,
      getSupportedPlatforms,
      getCredentialFormModelofPlatform,
      testAuthofPlatform,
      createThreadofChat,
      setWindowExpandStatus,
      startConnectAssistant,
      getAuthorizedPlatforms
    }
  },
  {
    persist: true // 启用持久化
  }
)
