<template>
  <div class="platfrom-chat">
    <section class="chat-container">
      <chat-msg-item
        v-for="(chatItem, index) of chatThreadHistory"
        :key="index"
        :chat-item="chatItem"
      />
    </section>
    <footer>
      <div class="msg-wrp">
        <div
          ref="msgInputRef"
          class="msg-input"
          data-placeholder="message chat"
          :contenteditable="true"
          @input="onInput"
        ></div>
        <div class="msg-input-suffix">
          <a-button
            :disabled="!sendable"
            type="primary"
            class="msg-input-send"
            @click="sendMessage"
          >
            <img
              :src="getSvgUrl(sendable ? 'send' : 'send-disabled', 'chatbot')"
              alt="send"
            />
          </a-button>
        </div>
      </div>
      <section>
        {{ `${currPlatform?.platformName} ${currPlatform?.currModel}` }}
      </section>
    </footer>
  </div>
</template>

<script setup lang="ts">
  import useChatbot from './chatbot'
  import { storeToRefs } from 'pinia'
  import { getSvgUrl, scrollToBottom } from '@/utils/tools'
  import type { Option } from './select-menu.vue'
  import { message } from 'ant-design-vue/es/components'
  import { ref, watch, computed, watchEffect, toRefs, nextTick } from 'vue'
  import ChatMsgItem from './chat-msg-item.vue'
  import { useI18n } from 'vue-i18n'

  interface PlatfromChatPorps {
    currPage?: Option
    visible: boolean
  }

  const { t } = useI18n()
  const chatbot = useChatbot()
  const inputText = ref('')
  const isMessageFullyReceived = ref(true)
  const msgInputRef = ref<HTMLInputElement | null>(null)
  const props = defineProps<PlatfromChatPorps>()
  const { visible } = toRefs(props)
  const { currPlatform, chatThreadHistory, isExpand } = storeToRefs(chatbot)

  const sendable = computed(
    () => inputText.value != '' && isMessageFullyReceived.value
  )

  watchEffect(async () => {
    if (props.currPage?.action == 'PLATFORM_CHAT' && visible.value) {
      chatbot.fetchThreadChatHistory()
      await nextTick()
      scrollToBottom(document.querySelector('.chat-container') as HTMLElement)
    }
  })

  watch(isExpand, () => {
    scrollToBottom(document.querySelector('.chat-container') as HTMLElement)
  })

  const onInput = (e: Event) => {
    inputText.value = (e.target as Element)?.textContent || ''
  }

  const reciveMessage = async () => {
    try {
      const res = await chatbot.fetchSendChatMessage(inputText.value)
      isMessageFullyReceived.value = res as boolean
      inputText.value = ''
      scrollToBottom(document.querySelector('.chat-container') as HTMLElement)
    } catch (error) {
      console.log('recived message:>> ', error)
    } finally {
      isMessageFullyReceived.value = true
      scrollToBottom(document.querySelector('.chat-container') as HTMLElement)
    }
  }

  const clearUpInputContent = () => {
    msgInputRef.value!.innerHTML = ''
  }

  const sendMessage = () => {
    isMessageFullyReceived.value = false
    if (inputText.value === '') {
      message.warning(t('ai.empty_message'))
      return
    }
    chatbot.updateThreadChatHistory('USER', inputText.value as string)
    scrollToBottom(document.querySelector('.chat-container') as HTMLElement)
    clearUpInputContent()
    reciveMessage()
  }
</script>

<style lang="scss" scoped>
  .platfrom-chat {
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    position: relative;
    height: 100%;
    padding: 4px !important;

    section {
      flex: 1 1 0%;
      overflow: auto;
      padding: 0 14px;
    }

    .msg-wrp {
      display: flex;
      align-items: center;
      border: 1px solid #e5e7eb;
      border-radius: 16px;
      padding: 6px;
      overflow: hidden;
      .ant-input {
        border-color: transparent;
        &:focus {
          border-color: transparent;
          box-shadow: none;
        }
        &:hover {
          border-color: transparent;
        }
      }

      .msg-input {
        width: 100%;
        max-height: 100px;
        padding: 2px 8px;
        white-space: normal;
        transition: all 0.2s;
        overflow: auto;
        font-weight: 500;
        &::before {
          content: attr(data-placeholder); /* 模拟原生 input 的 placeholder */
          color: #a9a9a9;
          /* 字体溢出变为省略号 */
          display: block;
          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
        }
        &:not(:empty)::before {
          content: '';
        }
        &:focus-visible {
          outline: 0;
          border-color: none;
        }
        &-suffix {
          align-self: end;
          cursor: pointer;
        }
        &-send {
          width: 50px;
          height: 34px;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: 12px;
        }
      }
    }

    footer {
      width: 100%;
      section {
        text-align: center;
        font-size: 12px;
        padding: 2px;
        color: #a9a9a9;
      }
    }
  }
</style>
