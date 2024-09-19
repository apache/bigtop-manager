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
  import { computed, h } from 'vue'
  import useChatbot from './chatbot'
  import type { AuthorizedPlatform } from '@/api/chatbot/types'
  import type { SelectData, Option } from './select-menu.vue'
  import { useI18n } from 'vue-i18n'
  import { Modal } from 'ant-design-vue/es/components'
  import { ExclamationCircleFilled } from '@ant-design/icons-vue/lib/icons'

  interface PlatformSelectProps {
    currPage?: Option
  }

  defineProps<PlatformSelectProps>()
  const { t } = useI18n()
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
      subTitle: t('ai.select_authorized_platform'),
      emptyOptionsText: t('ai.no_authorized_platform'),
      hasDel: true,
      options: formattedOptions.value
    },
    {
      subTitle: t('ai.or_you_can'),
      hasDel: false,
      options: [
        {
          action: 'PLATFORM_MANAGEMENT',
          name: t('ai.authorize_new_platform')
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
    Modal.confirm({
      title: t('common.delete_confirm_title'),
      icon: h(ExclamationCircleFilled),
      content: t('common.delete_confirm_content', [option.name]),
      onOk() {
        const { id: platformId } = option
        chatbot.fetchDelAuthorizedPlatform(platformId)
      }
    })
  }
</script>

<style lang="scss" scoped></style>
