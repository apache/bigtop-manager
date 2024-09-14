<template>
  <div class="pre-chat">
    <template v-if="currPage?.action === 'PLATFORM_MODEL'">
      <select-menu
        :select-data="PLATFORM_MODEL"
        @select="onSelect('ChAT_THREAD_MANAGEMENT', $event)"
      />
    </template>
    <template v-if="currPage?.action === 'ChAT_THREAD_MANAGEMENT'">
      <select-menu
        :select-data="ChAT_THREAD_MANAGEMENT"
        @select="onSelect('PLATFORM_CHAT', $event)"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
  import SelectMenu from './select-menu.vue'
  import useChatbot from './chatbot'
  import { storeToRefs } from 'pinia'
  import { toRefs, computed } from 'vue'
  import type { SelectData, Option } from './select-menu.vue'
  import type {
    Platform,
    ChatThreadCondition,
    ChatThread
  } from '@/api/chatbot/types'

  interface PreChatPorps {
    currPage?: Option
  }

  const chatbot = useChatbot()
  const props = defineProps<PreChatPorps>()
  const emits = defineEmits(['update:currPage'])
  const { currPage } = toRefs(props)
  const { currPlatform, chatThreads } = storeToRefs(chatbot)

  const PLATFORM_MODEL = computed<SelectData[]>(() => [
    {
      subTitle: '请选择您要使用的模型',
      options: currPlatform.value?.supportModels
        .split(',')
        .map((v) => ({ name: v, action: '' }))
    }
  ])

  const formattedOptions = computed<Option[]>(() => {
    return chatThreads.value.map((v, idx) => ({
      ...v,
      id: v.threadId,
      name: `线程${idx}`,
      action: 'SELSECT_THREAD_TO_CHAT'
    }))
  })

  const ChAT_THREAD_MANAGEMENT = computed<SelectData[]>(() => [
    {
      subTitle: '请选择下面的线程进入聊天',
      hasDel: true,
      options: formattedOptions.value
    },
    {
      subTitle: '或者你可以',
      hasDel: false,
      options: [
        {
          action: 'CREATE_THREAD_TO_CHAT',
          name: '创建新线程'
        }
      ]
    }
  ])

  const onSelect = (type: string, option: Option) => {
    if (type == 'ChAT_THREAD_MANAGEMENT') {
      const newPlatFrom = {
        ...currPlatform.value,
        currModel: option.name
      } as Platform
      chatbot.updateCurrPlatform(newPlatFrom)
    }

    if (type == 'PLATFORM_CHAT') {
      if (option.action === 'SELSECT_THREAD_TO_CHAT') {
        const { id: threadId, createTime, updateTime } = option
        chatbot.updateCurrThread({
          threadName: option.name,
          threadId,
          createTime,
          updateTime
        } as ChatThread)
      }
      if (option.action === 'CREATE_THREAD_TO_CHAT') {
        const platformInfo = {
          platformId: currPlatform.value?.platformId,
          model: currPlatform.value?.currModel
        } as ChatThreadCondition
        chatbot.fetchCreateChatThread(platformInfo)
      }
    }

    emits('update:currPage', { ...currPage.value, action: type })
  }
</script>

<style scoped></style>
