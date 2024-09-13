<template>
  <div class="platform-select">
    <select-menu
      :select-data="platformSelects"
      @select="onSelect"
    ></select-menu>
  </div>
</template>

<script setup lang="ts">
  import { storeToRefs } from 'pinia'
  import SelectMenu from './select-menu.vue'
  import type { SelectData, Option } from './select-menu.vue'
  import { computed } from 'vue'
  import type { AuthorizedPlatform } from '@/api/chatbot/types'
  import useChatbot from './chatbot'

  interface PlatformSelectProps {
    currPage?: Option
  }

  defineProps<PlatformSelectProps>()
  const chatbot = useChatbot()
  const { authorizedPlatformList } = storeToRefs(chatbot)
  const emits = defineEmits(['update:currPage'])

  const formattedOptions = computed(() => {
    return authorizedPlatformList.value.map((platform: AuthorizedPlatform) => {
      return {
        id: platform.id,
        name: platform.name,
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
    emits('update:currPage', option)
  }
</script>

<style lang="scss" scoped></style>
