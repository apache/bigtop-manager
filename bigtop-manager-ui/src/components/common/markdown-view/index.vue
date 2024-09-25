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
  import { computed, toRefs } from 'vue'
  import { Marked } from 'marked'
  import { markedHighlight } from 'marked-highlight'
  import { copyText } from '@/utils/tools'
  import { message } from 'ant-design-vue'
  import { useI18n } from 'vue-i18n'
  import hljs from 'highlight.js'

  interface MarkdownProps {
    markRaw: string
  }
  const props = withDefaults(defineProps<MarkdownProps>(), {
    markRaw: ''
  })
  const { t } = useI18n()
  const { markRaw } = toRefs(props)
  const markdownContent = computed(() => parseMDByHighlight(markRaw.value))

  const upgradeCodeBlock = (el: HTMLElement) => {
    const pres = el.querySelectorAll('pre')
    pres.forEach((pre) => {
      const code = pre.querySelector('code')

      pre.querySelector('#lang')!.textContent = (
        code?.classList.value.replace('hljs language-', '') as string
      ).toLowerCase()

      const copyTag = pre.querySelector('#copy') as HTMLElement
      if (copyTag) {
        copyTag.removeEventListener('click', () => {})
      }
      copyTag.addEventListener('click', () => {
        copyText(code?.textContent as string)
          .then(() => {
            message.success(`${t('common.copy_success')}`)
          })
          .catch((err: Error) => {
            message.error(`${t('common.copy_fail')}`)
            console.log('err :>> ', err)
          })
      })
    })
  }

  const upgradedCodeBlock = (parsedMarked: string) => {
    const upgradeParts = `<pre style="padding:0"><div style="background: #4c4d58; display:flex; justify-content: space-between;padding:2px 6px;align-items: center;"><span id="lang">Code</span><button id="copy">${t(
      'common.copy'
    )}</button></div>`
    return parsedMarked.replace(/<pre>/g, upgradeParts)
  }

  const getMarkedHighlightOps = () => {
    return markedHighlight({
      langPrefix: 'hljs language-',
      highlight(code, lang) {
        const language = hljs.getLanguage(lang) ? lang : 'plaintext'
        return hljs.highlight(code, { language }).value
      }
    })
  }

  const parseMDByHighlight = (content: string) => {
    const marked = new Marked(getMarkedHighlightOps())
    return upgradedCodeBlock(marked.parse(content) as string)
  }

  const vUpgradeCodeBlock = {
    updated: upgradeCodeBlock,
    mounted: upgradeCodeBlock
  }
</script>

<template>
  <div v-upgradeCodeBlock class="markdown-body" v-html="markdownContent"> </div>
</template>

<style lang="scss" scoped></style>
