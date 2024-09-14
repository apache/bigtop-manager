<template>
  <div class="chat-item" :class="[isRight ? 'chat-r' : '']">
    <section v-if="!isRight" class="chat-assistant">
      <img :src="getSvgUrl('robot', 'chatbot')" />
    </section>
    <section v-else class="chat-user">
      <img :src="getSvgUrl('user', 'chatbot')" />
    </section>
    <article class="chat-item-msg">
      <div>{{ message }}</div>
    </article>
  </div>
</template>

<script setup lang="ts">
  import { computed, ref } from 'vue'
  import { getSvgUrl } from '@/utils/tools'
  import { ChatThreadHistoryItem } from '@/api/chatbot/types'

  interface Props {
    chatItem: ChatThreadHistoryItem
  }
  const props = defineProps<Props>()
  const message = ref(props.chatItem.message)
  const isRight = computed(() => props.chatItem.sender === 'USER')
</script>

<style lang="scss" scoped>
  .chat-head {
    display: flex;
    flex-shrink: 0;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    border: 1px solid rgb(207, 207, 207);
    overflow: hidden;
    padding: 4px;
  }

  .chat-item {
    display: flex;
    width: 100%;
    margin-top: 44px;
    margin-bottom: 44px;
    &-msg {
      @include flexbox(null, null, center, center);
      background-color: #e9e9e9;
      border-radius: 8px;
      padding: 8px;
    }
  }

  .chat-r {
    flex-direction: row-reverse;
  }

  .chat-assistant {
    @extend .chat-head;
    margin-right: 8px;
  }
  .chat-user {
    @extend .chat-head;
    margin-left: 8px;
  }
</style>
