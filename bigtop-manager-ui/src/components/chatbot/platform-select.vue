<template>
  <div class="platform-select">
    <select-menu
      :select-data="platformSelects"
      @select="onSelect"
      @remove="onRemove"
    ></select-menu>
  </div>
</template>

<script setup lang="ts">
  import { storeToRefs } from 'pinia'
  import SelectMenu from './select-menu.vue'
  import { computed } from 'vue'
  import useChatbot from './chatbot'
  import type { AuthorizedPlatform } from '@/api/chatbot/types'
  import type { SelectData, Option } from './select-menu.vue'

  interface PlatformSelectProps {
    currPage?: Option
  }

  defineProps<PlatformSelectProps>()
  const chatbot = useChatbot()
  const { authorizedPlatforms } = storeToRefs(chatbot)
  const emits = defineEmits(['update:currPage'])

  const formattedOptions = computed(() => {
    return authorizedPlatforms.value.map((platform: AuthorizedPlatform) => {
      return {
        id: platform.platformId,
        name: platform.platformName,
        supportModels: platform.supportModels,
        action: 'PLATFORM_MODEL'
      }
    })
  })

  const platformSelects = computed<SelectData[]>(() => [
    {
      subTitle: '请选择下列已授权的平台',
      emptyOptionsText: '暂无授权平台',
      hasDel: true,
      options: formattedOptions.value
    },
    {
      subTitle: '或者你可以',
      hasDel: false,
      options: [
        {
          action: 'PLATFORM_MANAGEMENT',
          name: '授权新平台'
        }
      ]
    }
  ])

  const onSelect = (option: Option) => {
    if (option.action === 'PLATFORM_MODEL') {
      const { id: platformId, name: platformName, supportModels } = option
      chatbot.updateCurrPlatform({ platformId, platformName, supportModels })
    }
    emits('update:currPage', option)
  }

  const onRemove = (option: Option) => {
    const { id: platformId } = option
    chatbot.fetchDelAuthorizedPlatform(platformId)
  }
</script>

<style lang="scss" scoped></style>
