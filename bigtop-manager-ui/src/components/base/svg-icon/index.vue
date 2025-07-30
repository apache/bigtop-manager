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
  const icons = {
    avatar: IconSvgAvatar,
    'bm-logo': IconSvgBmLogo,
    book: IconSvgBook,
    'bottom-activated': IconSvgBottomActivated,
    bottom: IconSvgBottom,
    canceled: IconSvgCanceled,
    'chat-avatar': IconSvgChatAvatar,
    chatbot: IconSvgChatbot,
    close: IconSvgClose,
    'clusters-activated': IconSvgClustersActivated,
    clusters: IconSvgClusters,
    communication: IconSvgCommunication,
    'components-activated': IconSvgComponentsActivated,
    components: IconSvgComponents,
    copy: IconSvgCopy,
    download: IconSvgDownload,
    error: IconSvgError,
    'exit-screen': IconSvgExitScreen,
    failed: IconSvgFailed,
    'filter-activated': IconSvgFilterActivated,
    filter: IconSvgFilter,
    'full-screen': IconSvgFullScreen,
    github: IconSvgGithub,
    history: IconSvgHistory,
    'hosts-activated': IconSvgHostsActivated,
    hosts: IconSvgHosts,
    'infrastructures-activated': IconSvgInfrastructuresActivated,
    infrastructures: IconSvgInfrastructures,
    installing: IconSvgInstalling,
    language: IconSvgLanguage,
    'llm-config-activated': IconSvgLlmConfigActivated,
    'llm-config': IconSvgLlmConfig,
    'more-line': IconSvgMoreLine,
    more: IconSvgMore,
    'plus-dark': IconSvgPlusDark,
    'plus-gray': IconSvgPlusGray,
    plus: IconSvgPlus,
    processing: IconSvgProcessing,
    question: IconSvgQuestion,
    remove: IconSvgRemove,
    restart: IconSvgRestart,
    retry: IconSvgRetry,
    'search-activated': IconSvgSearchActivated,
    search: IconSvgSearch,
    send: IconSvgSend,
    start: IconSvgStart,
    stop: IconSvgStop,
    success: IconSvgSuccess,
    unknown: IconSvgUnknown,
    'carbon-language': IconSvgCarbonLanguage,
    warn: IconSvgWarn
  }

  const props = defineProps<{ name: string | undefined }>()
  const isIcon = computed(() => props.name && icons[props.name])
</script>

<template>
  <component :is="isIcon" v-if="props.name" />
</template>
