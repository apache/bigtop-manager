<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements.  See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership.  The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License.  You may obtain a copy of the License at
  ~
  ~   http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
-->
<script setup lang="ts">
  import { computed, ref, watchEffect } from 'vue'
  import { getSvgUrl } from '@/utils/tools'
  import { parseMDByHighlight } from '@/utils/render'
  import { ChatThreadHistoryItem } from '@/api/chatbot/types'

  interface Props {
    chatItem: ChatThreadHistoryItem
  }
  const props = defineProps<Props>()
  const emits = defineEmits(['updatedMsg'])
  const message = ref('')
  const isRight = computed(() => props.chatItem.sender === 'USER')

  watchEffect(() => {
    message.value = props.chatItem.message
    emits('updatedMsg')
  })
</script>

<template>
  <div class="chat-item" :class="[isRight ? 'chat-r' : '']">
    <section v-if="!isRight" class="chat-assistant">
      <img :src="getSvgUrl('robot', 'chatbot')" />
    </section>
    <section v-else class="chat-user">
      <img :src="getSvgUrl('user', 'chatbot')" />
    </section>
    <article class="chat-item-msg">
      <div class="markdown-body" v-html="parseMDByHighlight(message)"></div>
    </article>
  </div>
</template>

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
    margin-top: 44px;
    margin-bottom: 44px;
    &-msg {
      @include flexbox($justify: center, $align: center);
      background-color: #f7f7f7;
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
