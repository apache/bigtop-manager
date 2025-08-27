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
  import { Marked } from 'marked'
  import { markedHighlight } from 'marked-highlight'
  import { copyText } from '@/utils/tools'
  import { message } from 'ant-design-vue'
  import hljs from 'highlight.js'

  type Props = {
    markRaw: string
  }

  const props = withDefaults(defineProps<Props>(), {
    markRaw: ''
  })

  const { t } = useI18n()
  const { markRaw } = toRefs(props)
  const markdownContent = computed(() => parseMDByHighlight(markRaw.value))

  const copyCode = (code: string) => {
    copyText(code)
      .then(() => {
        message.success(`${t('common.copy_success')}`)
      })
      .catch((err: Error) => {
        message.error(`${t('common.copy_fail')}`)
        console.log('err :>> ', err)
      })
  }

  const upgradeCodeBlock = async (el: HTMLElement) => {
    await nextTick()
    const pres = el.querySelectorAll('pre')
    pres.forEach((pre) => {
      const code = pre.querySelector('code')
      const langTag = pre.querySelector('#language')
      const copyTag = pre.querySelector('#copy')

      langTag!.textContent = (code?.classList.value.replace('hljs language-', '') as string).toLowerCase()
      if (copyTag) {
        copyTag.removeEventListener('click', () => copyCode(code?.textContent || ''))
        copyTag.addEventListener('click', () => copyCode(code?.textContent || ''))
      }
    })
  }

  const upgradedCodeBlock = (parsedMarked: string) => {
    const upgradeParts = `<pre style="padding:0"><div class="code-block-head"><label id="language">Code</label><button id="copy">${t(
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
  <div v-upgradeCodeBlock v-dompurify-html="markdownContent" class="markdown-body"> </div>
</template>

<style lang="scss" scoped></style>
