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
    <div class="chat-item-avatar">
      <section v-if="!isRight" class="chat-assistant">
        <svg-icon name="robot" style="margin: 0" />
      </section>
      <section v-else class="chat-user">
        <svg-icon name="user" style="margin: 0" />
      </section>
    </div>
    <article class="chat-item-msg" :class="[isRight ? 'msg-r' : 'msg-l']">
      <div class="msg-wrp">{{ message }}</div>
    </article>
  </div>
</template>

<style lang="scss" scoped>
  .chat-head {
    @include flexbox($justify: center, $align: center);
    width: 32px;
    height: 32px;
    border-radius: 50%;
    border: 1px solid rgb(207, 207, 207);
    overflow: hidden;
  }

  .chat-item {
    flex: 1;
    margin: 22px 0;
    box-sizing: border-box;
    display: flex;
    &-avatar {
      flex: 0 0 44px;
      @include flexbox($justify: center);
    }
    &-msg {
      width: 100%;
      display: flex;
      box-sizing: border-box;
      .msg-wrp {
        height: auto;
        padding: 10px;
        border-radius: 8px;
        align-items: flex-start;
        background-color: #e9e9e9;
      }
    }
  }

  .msg-r {
    justify-content: flex-end;
    padding-left: 44px;
  }

  .msg-l {
    padding-right: 44px;
  }

  .chat-r {
    flex-direction: row-reverse;
  }

  .chat-assistant {
    @extend .chat-head;
  }
  .chat-user {
    @extend .chat-head;
  }
</style>
