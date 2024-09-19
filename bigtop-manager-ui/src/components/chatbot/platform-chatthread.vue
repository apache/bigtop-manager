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
        @remove="onRemove"
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
  import { useI18n } from 'vue-i18n'

  interface PreChatPorps {
    currPage?: Option
  }

  const { t } = useI18n()
  const chatbot = useChatbot()
  const props = defineProps<PreChatPorps>()
  const emits = defineEmits(['update:currPage'])
  const { currPage } = toRefs(props)
  const { currPlatform, chatThreads } = storeToRefs(chatbot)

  const PLATFORM_MODEL = computed<SelectData[]>(() => [
    {
      subTitle: t('ai.select_model'),
      options: currPlatform.value?.supportModels
        .split(',')
        .map((v) => ({ name: v, action: '' }))
    }
  ])

  const formattedOptions = computed<Option[]>(() => {
    return chatThreads.value.map((v, idx) => ({
      ...v,
      id: v.threadId,
      name: t('ai.thread_name', [idx]),
      action: 'SELSECT_THREAD_TO_CHAT'
    }))
  })

  const ChAT_THREAD_MANAGEMENT = computed<SelectData[]>(() => [
    {
      subTitle: t('ai.select_thread_to_chat'),
      hasDel: true,
      options: formattedOptions.value
    },
    {
      subTitle: t('ai.or_you_can'),
      hasDel: false,
      options: [
        {
          action: 'CREATE_THREAD_TO_CHAT',
          name: t('ai.create_new_thread')
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

  const onRemove = (option: Option) => {
    const { threadId, platformId } = option
    chatbot.fetchDelChatThread({ threadId, platformId })
  }
</script>

<style scoped></style>
