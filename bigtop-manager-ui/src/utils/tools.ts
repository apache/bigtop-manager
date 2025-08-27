/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

export function copyText(text: string): Promise<any> {
  if (navigator.clipboard) {
    return navigator.clipboard.writeText(text)
  }
  return new Promise(async (resolve, reject) => {
    try {
      const { default: ClipboardJS } = await import('clipboard')
      if (!ClipboardJS.isSupported()) {
        reject(new Error('ClipboardJS not support!'))
        return
      }
      const btn = document.createElement('button')
      btn.innerText = text
      const clipboard = new ClipboardJS(btn, {
        text: () => text
      })
      clipboard.on('success', () => {
        resolve(true)
        clipboard.destroy()
      })
      clipboard.on('error', (err) => {
        reject(err)
        clipboard.destroy()
      })
      btn.click()
    } catch (error) {
      console.log('copytext :>> ', error)
    }
  })
}

export function usePngImage(imageName: string = 'logo'): string {
  return new URL(`../assets/images/${imageName}.png`, import.meta.url).href
}

export function scrollToBottom(container: HTMLElement | null) {
  if (!container) {
    return
  }
  requestAnimationFrame(() => {
    container.scrollTop = container.scrollHeight
  })
}

export function getRandomFromTimestamp(len: number = 6) {
  return Date.now().toString().slice(-len)
}

export function generateRandomId(length = 8) {
  return Math.random()
    .toString(36)
    .substring(2, length + 2)
}

export function pick<T extends object, K extends keyof T>(obj: T, keys: K[]): Pick<T, K> {
  return keys.reduce(
    (acc, key) => {
      if (obj.hasOwnProperty(key)) {
        acc[key] = obj[key]
      }
      return acc
    },
    {} as Pick<T, K>
  )
}

export function createKeyedItem<T extends object>(item: T): T & { __key: string } {
  return {
    ...item,
    __key: crypto.randomUUID?.() ?? `${Date.now()}-${Math.random()}`
  }
}
